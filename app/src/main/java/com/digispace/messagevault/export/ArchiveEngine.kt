/*
 * ✒ Metadata
 *     - Title: Archive Engine (Message Vault Edition - v1.0)
 *     - File Name: ArchiveEngine.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/ArchiveEngine.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Call Exporter.markComplete() only after both source loops finish, so a cancelled/failed run no longer commits a truncated archive.db; carry the extractor's failure count and first error into RunMetrics (metrics.json + MANIFEST.md), which the service surfaces in the completion notification.
 *     - 1.0.2 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Write runs under ExportLocation.baseDir (public /sdcard/MessageVault when granted) instead of the app sandbox.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Instrument the streaming pass: wall + per-phase + per-sink timing, SMS/MMS counts, attachment count + bytes, date range. Emit metrics.json; expand MANIFEST; ArchiveResult now carries RunMetrics.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The conductor of the export. It opens the selected exporters, makes one
 *     streaming pass over the SMS then MMS sources, runs each message through
 *     optional attachment extraction, fans it out to every exporter, reports
 *     progress, and finally writes a run manifest. This is the orchestration
 *     layer between the UI (ViewModel calls run()) and the data + export layers —
 *     the only place that knows about all the moving parts at once.
 *
 * ✒ Key Features:
 *     - Single streaming pass: one `pump` lambda is shared by SMS and MMS forEach so both kinds run the same code path.
 *     - Background execution: withContext(Dispatchers.IO) moves the whole export off the UI thread.
 *     - Cooperative cancellation: a captured coroutineContext lets ctx.ensureActive() bail cleanly from non-suspend lambdas.
 *     - Conditional sinks: buildList { } assembles only the checked formats (JSONL, SQLite, Markdown) read-only.
 *     - Crash-safe close: exporters are closed in finally so files always flush, while markComplete() is reached only when both source loops finish — a cancelled or failed run therefore rolls the SQLite transaction back rather than publishing a partial archive.
 *     - Run metrics + manifest: emits metrics.json and a MANIFEST.md with wall, per-phase, and per-sink timing into a timestamped subfolder.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Context; kotlinx.coroutines; com.digispace.messagevault data sources (ContactResolver, SmsSource, MmsSource), storage.ExportLocation, util.Format, and the export sinks/extractor.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import android.content.Context
import com.digispace.messagevault.data.model.ExportProgress
import com.digispace.messagevault.data.source.ContactResolver
import com.digispace.messagevault.data.source.MmsSource
import com.digispace.messagevault.data.source.SmsSource
import com.digispace.messagevault.storage.ExportLocation
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.coroutineContext

data class ArchiveConfig(
    val includeSms: Boolean = true,
    val includeMms: Boolean = true,
    val jsonl: Boolean = true,
    val sqlite: Boolean = true,
    val markdown: Boolean = false,
    val extractAttachments: Boolean = true
)

data class ArchiveResult(
    val outputDir: File,
    val messageCount: Int,
    val attachmentCount: Int,
    val metrics: RunMetrics
)

class ArchiveEngine(private val context: Context) {

    suspend fun run(
        config: ArchiveConfig,
        onProgress: (ExportProgress) -> Unit
    ): ArchiveResult = withContext(Dispatchers.IO) {

        val contacts = ContactResolver(context)
        val sms = SmsSource(context, contacts)
        val mms = MmsSource(context, contacts)

        val runStartNanos = System.nanoTime()
        val startedAtMillis = System.currentTimeMillis()

        val runDir = File(
            ExportLocation.baseDir(context),
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        ).apply { mkdirs() }

        // --- assemble the selected sinks -------------------------------------
        val exporters = buildList {
            if (config.jsonl) add(JsonlExporter(File(runDir, "messages.jsonl")))
            if (config.sqlite) add(SqliteExporter(File(runDir, "archive.db")))
            if (config.markdown) add(MarkdownExporter(File(runDir, "conversations")))
        }
        val extractor = if (config.extractAttachments)
            AttachmentExtractor(context, File(runDir, "attachments")).also { it.prepare() }
        else null

        val total = (if (config.includeSms) sms.count() else 0) +
                    (if (config.includeMms) mms.count() else 0)

        var processed = 0
        var smsCount = 0
        var mmsCount = 0
        var minEpoch: Long? = null
        var maxEpoch: Long? = null
        var attachmentNanos = 0L
        var readSmsNanos = 0L
        var readMmsNanos = 0L
        val exporterNanos = LongArray(exporters.size)

        // Captured once in this suspend scope; ensureActive() on it still reads the
        // live Job state, so cancellation works from inside the non-suspend lambdas.
        val ctx = coroutineContext

        exporters.forEach { it.open() }
        try {
            val pump: (com.digispace.messagevault.data.model.Message) -> Unit = { msg ->
                // Attachment phase — timed as one cumulative bucket.
                if (extractor != null) {
                    val t = System.nanoTime()
                    extractor.extract(msg)
                    attachmentNanos += System.nanoTime() - t
                }
                // Fan-out — timed per sink so a slow exporter is visible.
                for (i in exporters.indices) {
                    val t = System.nanoTime()
                    exporters[i].write(msg)
                    exporterNanos[i] += System.nanoTime() - t
                }
                // Running tallies for the metrics snapshot.
                when (msg.kind) {
                    com.digispace.messagevault.data.model.Kind.SMS -> smsCount++
                    com.digispace.messagevault.data.model.Kind.MMS -> mmsCount++
                }
                val em = msg.epochMillis
                if (minEpoch == null || em < minEpoch!!) minEpoch = em
                if (maxEpoch == null || em > maxEpoch!!) maxEpoch = em

                processed++
                if (processed % 200 == 0 || processed == total) {
                    onProgress(ExportProgress(processed, total, "Writing"))
                }
            }

            if (config.includeSms) {
                onProgress(ExportProgress(processed, total, "Reading SMS"))
                val t = System.nanoTime()
                sms.forEach { ctx.ensureActive(); pump(it) }
                readSmsNanos = System.nanoTime() - t
            }
            if (config.includeMms) {
                onProgress(ExportProgress(processed, total, "Reading MMS"))
                val t = System.nanoTime()
                mms.forEach { ctx.ensureActive(); pump(it) }
                readMmsNanos = System.nanoTime() - t
            }
            // Both source loops returned normally — this is the ONLY place a run is
            // declared complete. Cancellation (ensureActive) or any throw skips it, so
            // a transactional sink rolls back instead of publishing a truncated archive.
            exporters.forEach { it.markComplete() }
        } finally {
            // Close every sink even if something threw mid-run.
            exporters.forEach { runCatching { it.close() } }
        }

        val metrics = RunMetrics(
            startedAtMillis = startedAtMillis,
            wallMillis = (System.nanoTime() - runStartNanos) / 1_000_000,
            smsCount = smsCount,
            mmsCount = mmsCount,
            attachmentCount = extractor?.filesExtracted ?: 0,
            attachmentBytes = extractor?.bytesExtracted ?: 0L,
            readSmsMillis = readSmsNanos / 1_000_000,
            readMmsMillis = readMmsNanos / 1_000_000,
            attachmentMillis = attachmentNanos / 1_000_000,
            exporterMillis = exporters.mapIndexed { i, e -> e.label to exporterNanos[i] / 1_000_000 },
            minEpochMillis = minEpoch,
            maxEpochMillis = maxEpoch,
            attachmentFailures = extractor?.failedExtractions ?: 0,
            firstAttachmentError = extractor?.firstFailure
        )

        File(runDir, "metrics.json").writeText(metrics.toJsonObject().toString(2))
        writeManifest(runDir, config, metrics)
        onProgress(ExportProgress(processed, total, "Done"))
        ArchiveResult(runDir, processed, metrics.attachmentCount, metrics)
    }

    private fun writeManifest(dir: File, config: ArchiveConfig, metrics: RunMetrics) {
        File(dir, "MANIFEST.md").writeText(
            buildString {
                appendLine("# Message Vault — Export Manifest")
                appendLine()
                appendLine("- Generated: ${Format.timestamp(System.currentTimeMillis())}")
                appendLine("- Sources: ${listOfNotNull(
                    if (config.includeSms) "SMS" else null,
                    if (config.includeMms) "MMS" else null
                ).joinToString(", ")}")
                appendLine()
                appendLine("## Run metrics")
                appendLine()
                metrics.manifestLines().forEach { appendLine(it) }
                appendLine()
                appendLine("## Outputs")
                metrics.exporterMillis.forEach { (label, _) -> appendLine("- $label") }
                if (config.extractAttachments) appendLine("- Attachments (attachments/)")
                appendLine()
                appendLine("Full machine-readable metrics: metrics.json")
                appendLine()
                appendLine("︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!")
            }
        )
    }
}
