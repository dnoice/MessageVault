/*
 * ✒ Metadata
 *     - Title: Run History (Message Vault Edition - v1.0)
 *     - File Name: RunHistory.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/storage/RunHistory.kt
 *     - Artifact Type: library
 *     - Version: 1.0.2
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Carry each run's start epoch (for relative age on Home) and add delete() to remove a run directory.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Carry each run's on-disk size, and add aggregate() rolling up archive count, total messages, and total bytes for the History header and Settings storage card.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial: list past runs from metrics.json.
 *
 * ✒ Description:
 *     Reads the export folder back into a list of past runs for the History screen.
 *     Each run directory carries a metrics.json (written by the engine) that is parsed
 *     into a lightweight RunSummary. No message data is loaded — only the per-run
 *     headline numbers — so this stays cheap regardless of corpus size.
 *
 * ✒ Key Features:
 *     - Newest-first listing: enumerates the export base dir and sorts runs by name.
 *     - Lightweight parse: pulls only headline metrics from metrics.json, never messages.
 *     - Resilient: a run missing its metrics.json (e.g. cancelled mid-pass) is surfaced
 *       as an incomplete entry rather than hidden.
 *     - Read-only storage-layer helper; HistoryScreen calls list() on a background thread.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Context; org.json.JSONObject; com.digispace.messagevault.storage.ExportLocation.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.storage

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class RunSummary(
    val dir: File,
    val name: String,
    val startedLabel: String,
    val totalMessages: Int,
    val smsCount: Int,
    val mmsCount: Int,
    val attachmentCount: Int,
    val attachmentBytesHuman: String,
    val wallHuman: String,
    val throughput: Double,
    val dateMin: String?,
    val dateMax: String?,
    val complete: Boolean,
    val sizeBytes: Long,
    val startedAtMillis: Long
)

/** Roll-up across every archive on device, for the History header and Settings. */
data class StorageStats(
    val archives: Int,
    val totalMessages: Int,
    val totalBytes: Long
)

object RunHistory {

    private val FOLDER_FMT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val LABEL_FMT = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US)

    /** Newest first. Empty if the export folder has no runs yet. */
    fun list(context: Context): List<RunSummary> {
        val base = ExportLocation.baseDir(context)
        val dirs = base.listFiles { f -> f.isDirectory } ?: return emptyList()
        return dirs.sortedByDescending { it.name }.map { dir -> summarize(dir) }
    }

    /** Roll-up across all runs. Reuses list() so a folder is walked once per call. */
    fun aggregate(context: Context): StorageStats {
        val runs = list(context)
        return StorageStats(
            archives = runs.size,
            totalMessages = runs.sumOf { it.totalMessages },
            totalBytes = runs.sumOf { it.sizeBytes }
        )
    }

    /** Delete a run directory and everything under it. Returns true on success. */
    fun delete(dir: File): Boolean = dir.deleteRecursively()

    /** Sum of every file's length under a run directory (metadata only — never reads bytes). */
    private fun folderSize(dir: File): Long =
        dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    private fun summarize(dir: File): RunSummary {
        val startedMs = runCatching { FOLDER_FMT.parse(dir.name)!!.time }.getOrDefault(0L)
        val label = runCatching { LABEL_FMT.format(FOLDER_FMT.parse(dir.name)!!) }.getOrDefault(dir.name)
        val size = folderSize(dir)
        val mf = File(dir, "metrics.json")
        if (!mf.exists()) return incomplete(dir, label, size, startedMs)
        return runCatching {
            val j = JSONObject(mf.readText())
            val dr = j.optJSONObject("date_range")
            RunSummary(
                dir = dir,
                name = dir.name,
                startedLabel = label,
                totalMessages = j.optInt("total_messages"),
                smsCount = j.optInt("sms_count"),
                mmsCount = j.optInt("mms_count"),
                attachmentCount = j.optInt("attachment_count"),
                attachmentBytesHuman = j.optString("attachment_bytes_human", "0 B"),
                wallHuman = j.optString("wall_human", ""),
                throughput = j.optDouble("throughput_msgs_per_sec", 0.0),
                dateMin = dr?.optString("min")?.cleanup(),
                dateMax = dr?.optString("max")?.cleanup(),
                complete = true,
                sizeBytes = size,
                startedAtMillis = startedMs
            )
        }.getOrElse { incomplete(dir, label, size, startedMs) }
    }

    private fun incomplete(dir: File, label: String, size: Long, startedMs: Long) = RunSummary(
        dir = dir, name = dir.name, startedLabel = label,
        totalMessages = 0, smsCount = 0, mmsCount = 0, attachmentCount = 0,
        attachmentBytesHuman = "0 B", wallHuman = "", throughput = 0.0,
        dateMin = null, dateMax = null, complete = false, sizeBytes = size,
        startedAtMillis = startedMs
    )

    private fun String.cleanup(): String? = takeIf { it.isNotEmpty() && it != "null" }
}
