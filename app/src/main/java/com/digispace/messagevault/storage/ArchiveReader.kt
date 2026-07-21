/*
 * ✒ Metadata
 *     - Title: Archive Reader (Message Vault Edition - v1.0)
 *     - File Name: ArchiveReader.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/storage/ArchiveReader.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — CRASH FIX: a run interrupted mid-export leaves a hot archive.db-journal; SQLite must roll it back to open the file, which is impossible read-only (SQLITE_READONLY_ROLLBACK) and threw straight through the coroutine, killing the process. Every read now degrades to an empty result instead of throwing, isReadable() probes an archive up front, and latestDb() skips archives it cannot open.
 *     - 1.0.0 (2026-06-22) [Anthropic - Claude Opus 4.8 (1M context)] — Initial read-only reader: conversations, thread, search.
 *
 * ✒ Description:
 *     Reads a finished archive.db back, READ-ONLY, for the Browse screen: the list
 *     of conversations, the messages within a thread, and a body search. The point
 *     of exporting a portable SQLite file is that it can be re-opened like any other
 *     database — this is the proof of that.
 *
 * ✒ Key Features:
 *     - Conversation list built in one ordered pass so it stays cheap and flat in memory.
 *     - Per-thread message retrieval with attachment counts joined in.
 *     - Body search with LIKE escaping and a bounded result limit.
 *     - Opens each db OPEN_READONLY and closes it per call (use {}); never writes.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android SQLiteDatabase / Cursor; com.digispace.messagevault.storage.ExportLocation.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.storage

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class Conversation(
    val threadId: Long,
    val who: String,
    val count: Int,
    val lastEpochMillis: Long,
    val lastSnippet: String,
    val hasMms: Boolean,
    /** Raw number — [who] may be a resolved name, but a contact photo lookup needs this. */
    val address: String? = null
)

data class ArchivedMessage(
    val id: Long,
    val kind: String,
    val direction: String,
    val epochMillis: Long,
    val body: String,
    val who: String,
    val attachmentCount: Int
)

data class SearchHit(
    val threadId: Long,
    val who: String,
    val snippet: String,
    val epochMillis: Long,
    val address: String? = null
)

object ArchiveReader {

    /** Newest run directory holding an archive.db we can actually open, or null. */
    fun latestDb(context: Context): File? {
        val base = ExportLocation.baseDir(context)
        val dirs = base.listFiles { f -> f.isDirectory } ?: return null
        return dirs.sortedByDescending { it.name }
            .map { File(it, "archive.db") }
            .firstOrNull { isReadable(it) }
    }

    /**
     * True when this archive can actually be opened read-only.
     *
     * An export interrupted mid-write leaves a hot `archive.db-journal`. SQLite has to
     * roll that journal back before it can open the file, which it cannot do through a
     * read-only handle — it raises SQLITE_READONLY_ROLLBACK. Probing here lets callers
     * skip or explain such an archive instead of crashing on it.
     */
    fun isReadable(dbFile: File): Boolean =
        dbFile.exists() && dbFile.length() > 0 && runCatching {
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { true }
        }.getOrDefault(false)

    fun conversations(dbFile: File): List<Conversation> = withDb(dbFile, emptyList()) { db ->
        val map = LinkedHashMap<Long, MutableConv>()
        db.rawQuery(
            "SELECT thread_id, contact_name, address, body, kind, epoch_millis " +
                "FROM messages ORDER BY epoch_millis DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val tid = c.getLong(0)
                val conv = map.getOrPut(tid) {
                    val who = c.strOrNull(1) ?: c.strOrNull(2) ?: "Thread $tid"
                    MutableConv(tid, who, c.getLong(5), snippet(c.strOrNull(3), c.getString(4)), c.strOrNull(2))
                }
                conv.count++
                if (c.getString(4) == "MMS") conv.hasMms = true
            }
        }
        map.values.map {
            Conversation(it.threadId, it.who, it.count, it.lastEpoch, it.snippet, it.hasMms, it.address)
        }
    }

    fun messages(dbFile: File, threadId: Long): List<ArchivedMessage> = withDb(dbFile, emptyList()) { db ->
        val attach = HashMap<Long, Int>()
        db.rawQuery("SELECT message_id, COUNT(*) FROM attachments GROUP BY message_id", null).use { c ->
            while (c.moveToNext()) attach[c.getLong(0)] = c.getInt(1)
        }
        val out = ArrayList<ArchivedMessage>()
        db.rawQuery(
            "SELECT id, kind, direction, epoch_millis, body, contact_name, address " +
                "FROM messages WHERE thread_id = ? ORDER BY epoch_millis ASC",
            arrayOf(threadId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                out.add(
                    ArchivedMessage(
                        id = id,
                        kind = c.getString(1),
                        direction = c.getString(2),
                        epochMillis = c.getLong(3),
                        body = c.strOrNull(4) ?: "",
                        who = c.strOrNull(5) ?: c.strOrNull(6) ?: "Unknown",
                        attachmentCount = attach[id] ?: 0
                    )
                )
            }
        }
        out
    }

    fun search(dbFile: File, query: String): List<SearchHit> = withDb(dbFile, emptyList()) { db ->
        val out = ArrayList<SearchHit>()
        db.rawQuery(
            "SELECT thread_id, contact_name, address, body, epoch_millis " +
                "FROM messages WHERE body LIKE ? ESCAPE '\\' ORDER BY epoch_millis DESC LIMIT 300",
            arrayOf("%" + escapeLike(query) + "%")
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    SearchHit(
                        threadId = c.getLong(0),
                        who = c.strOrNull(1) ?: c.strOrNull(2) ?: "Thread ${c.getLong(0)}",
                        snippet = snippet(c.strOrNull(3), "SMS"),
                        epochMillis = c.getLong(4),
                        address = c.strOrNull(2)
                    )
                )
            }
        }
        out
    }

    /**
     * Opens the archive read-only and runs [block], returning [fallback] if anything
     * goes wrong. A reader must NEVER take the app down over a bad file: an archive can
     * be half-written, journal-locked, truncated, or simply not a database, and all of
     * those surface as exceptions from open() or the first query.
     */
    private inline fun <T> withDb(dbFile: File, fallback: T, block: (SQLiteDatabase) -> T): T =
        try {
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use(block)
        } catch (e: Exception) {
            fallback
        }

    private fun escapeLike(s: String): String =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun snippet(body: String?, kind: String): String {
        val b = body?.replace('\n', ' ')?.trim().orEmpty()
        if (b.isEmpty()) return if (kind == "MMS") "[MMS]" else ""
        return if (b.length > 90) b.take(90) + "…" else b
    }

    private fun Cursor.strOrNull(i: Int): String? = if (isNull(i)) null else getString(i)

    private class MutableConv(
        val threadId: Long,
        val who: String,
        val lastEpoch: Long,
        val snippet: String,
        val address: String?
    ) {
        var count = 0
        var hasMms = false
    }
}
