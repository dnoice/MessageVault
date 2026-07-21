/*
 * ✒ Metadata
 *     - Title: SQLite Exporter (Message Vault Edition - v1.0)
 *     - File Name: SqliteExporter.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/SqliteExporter.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Make the promised rollback safety real. close() ran from the engine's finally block and committed unconditionally, so a cancelled or failed run published a fully committed, journal-free archive.db holding only the messages written so far — which ArchiveReader then served as the newest archive with nothing marking it truncated. Commit now happens only after markComplete(); otherwise endTransaction() rolls back and the half-written file (and its journal) is deleted.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Compile the INSERT statements ONCE in open() and rebind per row, instead of recompiling per message (and per attachment). Explicit column lists added.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Writes the archive into a standalone SQLite database file (archive.db) with
 *     indexed tables. Reach for this format when you want to QUERY the corpus later
 *     — "all messages from X", "everything in 2024", "messages with attachments" —
 *     instantly, with plain SQL, in any tool that opens SQLite. It is one concrete
 *     Exporter, selected via the SQLite checkbox.
 *
 * ✒ Key Features:
 *     - Raw SQLiteDatabase, not Room: emits a portable, dependency-free .db that outlives the app, readable by any SQLite tool.
 *     - Single-transaction writes: beginTransaction()/setTransactionSuccessful()/endTransaction() batch thousands of inserts into one commit for speed.
 *     - All-or-nothing publication: setTransactionSuccessful() fires only after the engine's markComplete(); a cancelled or failed run rolls back and the partial .db is deleted, so a truncated archive is never left where ArchiveReader can find it.
 *     - Compiled statements with ? placeholders: bound per row instead of building SQL strings — faster and immune to quoting/injection bugs.
 *     - Indices on thread_id / address / epoch_millis make those WHERE clauses instant instead of full-table scans.
 *     - Composite PRIMARY KEY(id, kind) keeps an SMS and an MMS that share a numeric id distinct.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android SQLiteDatabase / SQLiteStatement; com.digispace.messagevault.data.model.Message.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.digispace.messagevault.data.model.Message
import java.io.File

class SqliteExporter(private val dbFile: File) : Exporter {
    override val label = "SQLite (${dbFile.name})"
    private lateinit var db: SQLiteDatabase
    // Compiled ONCE in open(), rebound per row in write(), closed in close().
    private lateinit var msgStmt: SQLiteStatement
    private lateinit var attStmt: SQLiteStatement
    /** False until the engine calls markComplete(); gates the commit in close(). */
    private var completed = false

    override fun open() {
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()   // fresh archive each run
        db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL(
            """
            CREATE TABLE messages(
                id INTEGER, kind TEXT, thread_id INTEGER,
                address TEXT, contact_name TEXT, direction TEXT,
                epoch_millis INTEGER, body TEXT, read INTEGER,
                PRIMARY KEY(id, kind)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE attachments(
                part_id INTEGER PRIMARY KEY, message_id INTEGER,
                content_type TEXT, file_name TEXT, export_name TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_msg_thread ON messages(thread_id)")
        db.execSQL("CREATE INDEX idx_msg_addr   ON messages(address)")
        db.execSQL("CREATE INDEX idx_msg_date   ON messages(epoch_millis)")

        // Compile the inserts ONCE. Explicit column lists so a future schema tweak
        // can't silently shift positional bindings. write() only rebinds + executes.
        msgStmt = db.compileStatement(
            "INSERT INTO messages(id, kind, thread_id, address, contact_name, " +
                "direction, epoch_millis, body, read) VALUES(?,?,?,?,?,?,?,?,?)"
        )
        attStmt = db.compileStatement(
            "INSERT OR REPLACE INTO attachments(part_id, message_id, content_type, " +
                "file_name, export_name) VALUES(?,?,?,?,?)"
        )
        db.beginTransaction()
    }

    override fun write(message: Message) {
        msgStmt.run {
            clearBindings()
            bindLong(1, message.id)
            bindString(2, message.kind.name)
            bindLong(3, message.threadId)
            bindNullableString(this, 4, message.address)
            bindNullableString(this, 5, message.contactName)
            bindString(6, message.direction.name)
            bindLong(7, message.epochMillis)
            bindString(8, message.body)
            bindLong(9, if (message.read) 1 else 0)
            executeInsert()
        }
        for (a in message.attachments) {
            attStmt.run {
                clearBindings()
                bindLong(1, a.partId)
                bindLong(2, message.id)
                bindString(3, a.contentType)
                bindNullableString(this, 4, a.fileName)
                bindNullableString(this, 5, a.exportName)
                executeInsert()
            }
        }
    }

    /**
     * The engine calls this only after BOTH source loops return normally. Until it
     * does, the run is presumed incomplete — see close().
     */
    override fun markComplete() { completed = true }

    /**
     * Commits ONLY a run that reached markComplete(). The engine closes exporters from
     * a finally block, so this also runs on cancellation (ensureActive() throwing) and
     * on any mid-run exception; committing unconditionally there would publish a
     * silently truncated archive.db that ArchiveReader would happily pick as the newest
     * archive. On the incomplete path we skip setTransactionSuccessful() so
     * endTransaction() rolls back, then delete the file outright so a partial database
     * can never be mistaken for a finished one.
     */
    override fun close() {
        if (::db.isInitialized) {
            if (completed) db.setTransactionSuccessful()
            runCatching { db.endTransaction() }
            if (::msgStmt.isInitialized) msgStmt.close()
            if (::attStmt.isInitialized) attStmt.close()
            db.close()
            if (!completed) runCatching {
                dbFile.delete()
                File(dbFile.parentFile, "${dbFile.name}-journal").delete()
            }
        }
    }

    private fun bindNullableString(
        s: SQLiteStatement, idx: Int, v: String?
    ) { if (v == null) s.bindNull(idx) else s.bindString(idx, v) }
}
