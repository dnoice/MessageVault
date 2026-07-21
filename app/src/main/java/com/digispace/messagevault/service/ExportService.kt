/*
 * ✒ Metadata
 *     - Title: Export Service (Message Vault Edition - v1.0)
 *     - File Name: ExportService.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/service/ExportService.kt
 *     - Artifact Type: library
 *     - Version: 1.1.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — A Cancel intent arriving with no active run (a stale notification action, or a cancel racing the final write) started the service and then left it running forever with no job and no notification. It now stops itself when there is nothing to cancel.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Carry the run's attachment failure count into ExportStatus.Snapshot and qualify the completion notification, so a run that could not extract files no longer reports an unqualified success.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial foreground service: runs the archive engine outside the UI so a long export survives leaving the app, with a live progress notification, a Cancel action, and a shared status flow the UI observes.
 *
 * ✒ Description:
 *     Runs an export as a FOREGROUND SERVICE. Previously the run lived in the
 *     ViewModel's coroutine scope, which is fine while the user watches it and fragile
 *     the moment they leave — Android is free to kill a backgrounded process, taking a
 *     half-finished archive with it (exactly how a hot journal gets left behind). A
 *     foreground service with an ongoing notification tells the system this work is
 *     user-visible and must keep running, and it gives the user somewhere to watch
 *     progress and cancel from.
 *
 * ✒ Key Features:
 *     - startForeground with a live progress notification: the OS keeps the process alive for the whole run.
 *     - ExportStatus: a process-wide StateFlow the ViewModel collects, so the UI reflects a run it did not start and survives rotation or a return from the launcher.
 *     - Cancel action: the notification's Cancel routes back in as an intent and cancels the job cooperatively (the engine already honors ensureActive()).
 *     - Throttled notification updates: the engine reports every 200 messages; posting that often would spam the shade, so updates are rate-limited.
 *     - Terminal states always land: success, failure, and cancellation each settle the status flow and the notification in a finally block.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Service/Notification APIs; kotlinx.coroutines; com.digispace.messagevault.export (ArchiveEngine, ArchiveConfig); util.Notifications; util.Format.
 *     - Manifest: declared with android:foregroundServiceType="dataSync"; needs FOREGROUND_SERVICE and FOREGROUND_SERVICE_DATA_SYNC (API 34+).
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.digispace.messagevault.export.ArchiveConfig
import com.digispace.messagevault.export.ArchiveEngine
import com.digispace.messagevault.util.Format
import com.digispace.messagevault.util.Notifications
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Process-wide export status. The service owns the run; the UI only observes, so it
 * can attach to a run already in flight instead of assuming it started it.
 */
object ExportStatus {

    data class Snapshot(
        val running: Boolean = false,
        val processed: Int = 0,
        val total: Int = 0,
        val phase: String = "",
        val finishedPath: String? = null,
        val messageCount: Int = 0,
        val attachmentCount: Int = 0,
        /** Attachments the run could not extract; > 0 means the archive is missing files. */
        val attachmentFailures: Int = 0,
        val summary: String? = null,
        val error: String? = null,
        val cancelled: Boolean = false
    )

    private val _state = MutableStateFlow(Snapshot())
    val state: StateFlow<Snapshot> = _state.asStateFlow()

    internal fun update(transform: (Snapshot) -> Snapshot) = _state.update(transform)
    internal fun set(snapshot: Snapshot) { _state.value = snapshot }
}

class ExportService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var runJob: Job? = null
    private var lastNotifyAt = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                val job = runJob
                if (job?.isActive == true) {
                    job.cancel()
                } else {
                    // A Cancel that arrives with nothing running — a stale notification
                    // action, or a cancel racing the final write — used to start this
                    // service and then leave it sitting there alive with no work and no
                    // notification. Nothing to stop means nothing to stay up for.
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            else -> start(configFrom(intent))
        }
        return START_NOT_STICKY
    }

    private fun start(config: ArchiveConfig) {
        if (runJob?.isActive == true) return

        startForeground(
            Notifications.ID_EXPORT,
            Notifications.exportProgress(this, "Starting", 0, 0, cancelPendingIntent())
        )
        ExportStatus.set(ExportStatus.Snapshot(running = true, phase = "Starting"))

        runJob = scope.launch {
            // Held until after stopForeground: the progress notification IS this
            // service's foreground notification, and tearing that down removes it.
            var terminal: Pair<String, String>? = null
            try {
                val result = ArchiveEngine(applicationContext).run(config) { p ->
                    ExportStatus.update {
                        it.copy(processed = p.processed, total = p.total, phase = p.phase)
                    }
                    maybeNotify(p.phase, p.processed, p.total)
                }
                val m = result.metrics
                val summary = "${result.messageCount} msgs in ${Format.duration(m.wallMillis)} · " +
                    String.format(Locale.US, "%,.0f msgs/sec", m.throughputPerSec) +
                    if (m.attachmentFailures > 0) " · ${m.attachmentFailures} attachment(s) FAILED" else ""
                ExportStatus.set(
                    ExportStatus.Snapshot(
                        running = false,
                        processed = result.messageCount,
                        total = result.messageCount,
                        finishedPath = result.outputDir.absolutePath,
                        messageCount = result.messageCount,
                        attachmentCount = result.attachmentCount,
                        attachmentFailures = m.attachmentFailures,
                        summary = summary
                    )
                )
                // An unqualified "complete" would hide the fact that files are missing.
                terminal = (if (m.attachmentFailures > 0)
                    "Export complete — ${m.attachmentFailures} attachment(s) failed"
                else "Export complete") to summary
            } catch (ce: CancellationException) {
                // A user-initiated cancel needs no alert — just clear the progress entry.
                ExportStatus.set(ExportStatus.Snapshot(running = false, cancelled = true))
            } catch (t: Throwable) {
                val msg = t.message ?: t.javaClass.simpleName
                ExportStatus.set(ExportStatus.Snapshot(running = false, error = msg))
                terminal = "Export failed" to msg
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                terminal?.let { (title, text) ->
                    Notifications.exportComplete(this@ExportService, title, text)
                }
                stopSelf()
            }
        }
    }

    /**
     * The engine reports every 200 messages; posting that often would hammer the shade,
     * so updates are limited to a few per second (phase changes still land promptly
     * because the total/processed pair moves with them).
     */
    private fun maybeNotify(phase: String, processed: Int, total: Int) {
        val now = System.currentTimeMillis()
        if (now - lastNotifyAt < 500) return
        lastNotifyAt = now
        runCatching {
            androidx.core.app.NotificationManagerCompat.from(this).notify(
                Notifications.ID_EXPORT,
                Notifications.exportProgress(this, phase, processed, total, cancelPendingIntent())
            )
        }
    }

    private fun cancelPendingIntent(): PendingIntent =
        PendingIntent.getService(
            this, 1,
            Intent(this, ExportService::class.java).setAction(ACTION_CANCEL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val ACTION_CANCEL = "com.digispace.messagevault.action.CANCEL_EXPORT"

        private const val EX_SMS = "sms"
        private const val EX_MMS = "mms"
        private const val EX_JSONL = "jsonl"
        private const val EX_SQLITE = "sqlite"
        private const val EX_MD = "markdown"
        private const val EX_ATTACH = "attachments"

        fun startIntent(context: Context, c: ArchiveConfig): Intent =
            Intent(context, ExportService::class.java)
                .putExtra(EX_SMS, c.includeSms)
                .putExtra(EX_MMS, c.includeMms)
                .putExtra(EX_JSONL, c.jsonl)
                .putExtra(EX_SQLITE, c.sqlite)
                .putExtra(EX_MD, c.markdown)
                .putExtra(EX_ATTACH, c.extractAttachments)

        fun cancelIntent(context: Context): Intent =
            Intent(context, ExportService::class.java).setAction(ACTION_CANCEL)

        private fun configFrom(intent: Intent?): ArchiveConfig {
            val d = ArchiveConfig()
            if (intent == null) return d
            return ArchiveConfig(
                includeSms = intent.getBooleanExtra(EX_SMS, d.includeSms),
                includeMms = intent.getBooleanExtra(EX_MMS, d.includeMms),
                jsonl = intent.getBooleanExtra(EX_JSONL, d.jsonl),
                sqlite = intent.getBooleanExtra(EX_SQLITE, d.sqlite),
                markdown = intent.getBooleanExtra(EX_MD, d.markdown),
                extractAttachments = intent.getBooleanExtra(EX_ATTACH, d.extractAttachments)
            )
        }
    }
}
