/*
 * ✒ Metadata
 *     - Title: Run Metrics (Message Vault Edition - v1.0)
 *     - File Name: RunMetrics.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/RunMetrics.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Carry attachmentFailures + firstAttachmentError so metrics.json and MANIFEST.md report attachments that could not be extracted instead of quietly under-counting a run that lost data.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial metrics model: JSON + manifest serialization.
 *
 * ✒ Description:
 *     An immutable snapshot of ONE export run's instrumentation. The engine fills
 *     it in as the single streaming pass completes, then serializes it two ways:
 *     to metrics.json (machine-readable) and as a block of lines in MANIFEST.md
 *     (human-readable). The ViewModel also reads it to show a throughput line on
 *     the done screen.
 *
 * ✒ Key Features:
 *     - Dual serialization: toJsonObject() for metrics.json, manifestLines() for MANIFEST.md.
 *     - wallMillis: total run wall-clock from start of pass to the manifest write.
 *     - readSmsMillis / readMmsMillis: WALL time per source phase, inclusive of the
 *       per-message extraction + exporter writes inside that phase (not disjoint from components).
 *     - attachmentMillis: cumulative time spent inside attachment extraction.
 *     - exporterMillis: cumulative time inside each sink's write(), shown separately to
 *       spot a slow sink — they are components OF the phase wall-time, not additive to wallMillis.
 *     - throughputPerSec: totalMessages / wall seconds; holds only plain numbers, never message data.
 *
 * ✒ Other Important Information:
 *     - Dependencies: org.json (bundled); com.digispace.messagevault.util.Format. Produced by ArchiveEngine in the export layer.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import com.digispace.messagevault.util.Format
import org.json.JSONObject
import java.util.Locale

data class RunMetrics(
    val startedAtMillis: Long,
    val wallMillis: Long,
    val smsCount: Int,
    val mmsCount: Int,
    val attachmentCount: Int,
    val attachmentBytes: Long,
    val readSmsMillis: Long,
    val readMmsMillis: Long,
    val attachmentMillis: Long,
    /** (sink label -> cumulative write millis), in engine order. */
    val exporterMillis: List<Pair<String, Long>>,
    val minEpochMillis: Long?,
    val maxEpochMillis: Long?,
    /** Attachments the extractor could not write (provider error, disk full). 0 on a clean run. */
    val attachmentFailures: Int = 0,
    /** First extraction error message, if any — usually the cause of all the rest. */
    val firstAttachmentError: String? = null
) {
    val totalMessages: Int get() = smsCount + mmsCount

    val throughputPerSec: Double
        get() = if (wallMillis > 0) totalMessages * 1000.0 / wallMillis else 0.0

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("started_at", Format.timestamp(startedAtMillis))
        put("started_at_epoch_millis", startedAtMillis)
        put("wall_millis", wallMillis)
        put("wall_human", Format.duration(wallMillis))
        put("total_messages", totalMessages)
        put("sms_count", smsCount)
        put("mms_count", mmsCount)
        put("throughput_msgs_per_sec", Math.round(throughputPerSec * 10.0) / 10.0)
        put("attachment_count", attachmentCount)
        put("attachment_bytes", attachmentBytes)
        put("attachment_bytes_human", Format.bytes(attachmentBytes))
        put("attachment_failures", attachmentFailures)
        put("attachment_first_error", firstAttachmentError ?: JSONObject.NULL)
        put("phase_millis", JSONObject().apply {
            put("read_sms", readSmsMillis)
            put("read_mms", readMmsMillis)
            put("attachments_cumulative", attachmentMillis)
        })
        put("exporter_millis", JSONObject().apply {
            exporterMillis.forEach { (label, ms) -> put(label, ms) }
        })
        put("date_range", JSONObject().apply {
            put("min_epoch_millis", minEpochMillis ?: JSONObject.NULL)
            put("max_epoch_millis", maxEpochMillis ?: JSONObject.NULL)
            put("min", minEpochMillis?.let { Format.timestamp(it) } ?: JSONObject.NULL)
            put("max", maxEpochMillis?.let { Format.timestamp(it) } ?: JSONObject.NULL)
        })
    }

    /** Lines appended under a "Run metrics" heading in MANIFEST.md. */
    fun manifestLines(): List<String> = buildList {
        add("- Wall clock: ${Format.duration(wallMillis)}")
        add("- Throughput: ${String.format(Locale.US, "%,.0f", throughputPerSec)} msgs/sec")
        add("- Messages: $totalMessages (SMS $smsCount · MMS $mmsCount)")
        add("- Attachments: $attachmentCount file(s), ${Format.bytes(attachmentBytes)}")
        if (attachmentFailures > 0) {
            add("- Attachments FAILED: $attachmentFailures file(s) could not be extracted" +
                (firstAttachmentError?.let { " — first error: $it" } ?: ""))
        }
        if (minEpochMillis != null && maxEpochMillis != null) {
            add("- Date range: ${Format.timestamp(minEpochMillis)} → ${Format.timestamp(maxEpochMillis)}")
        }
        add("- Phase wall-time — SMS ${Format.duration(readSmsMillis)}, MMS ${Format.duration(readMmsMillis)}")
        add("- Attachment fetch (cumulative): ${Format.duration(attachmentMillis)}")
        exporterMillis.forEach { (label, ms) -> add("- Sink time — $label: ${Format.duration(ms)}") }
    }
}
