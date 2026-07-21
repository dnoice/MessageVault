/*
 * ✒ Metadata
 *     - Title: Archive ViewModel (Message Vault Edition - v1.0)
 *     - File Name: ArchiveViewModel.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/ArchiveViewModel.kt
 *     - Artifact Type: library
 *     - Version: 1.0.4
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.4 (2026-07-20) [Anthropic - Claude Opus 4.8] — Carry the run's attachmentFailures into UiState. The service has reported it since 1.1.0 and the done card was dropping it on the floor, so an export that silently failed to extract media still presented as an unqualified success on screen.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — Persist the export config: load the last-used sources/formats from SharedPreferences on init and save on every change, so the usual setup survives a relaunch.
 *     - 1.0.2 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Surface a run-metrics throughput summary (doneSummary) on completion for the done screen.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Cancel no longer reported as a failure: handle CancellationException explicitly so a user cancel settles on the Cancelled state instead of flashing a crimson "Failed." (runCatching swallowed it).
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Holds the screen's state and drives the export, publishing progress as
 *     observable state that the dumb UI (ArchiveScreen) draws while forwarding
 *     user taps back. Lives in the ui layer between the screen and the
 *     ArchiveEngine, owning the engine and launching it on a coroutine so
 *     rotating the phone mid-export does not restart the run.
 *
 * ✒ Key Features:
 *     - AndroidViewModel survives configuration changes and holds the Application context to reach content providers.
 *     - viewModelScope auto-cancels launched work when the ViewModel clears, so no background job leaks.
 *     - StateFlow/MutableStateFlow with private _state vs public read-only state drives recomposition safely.
 *     - Unidirectional data flow: state down (ViewModel -> UI), events up (UI taps -> ViewModel functions).
 *     - Immutable UiState updated via update { it.copy(...) }; a Job handle lets cancel() stop the run.
 *
 * ✒ Other Important Information:
 *     - Dependencies: AndroidX Lifecycle (AndroidViewModel, viewModelScope); kotlinx.coroutines (Flow, Job); com.digispace.messagevault.export.ArchiveEngine/ArchiveConfig; com.digispace.messagevault.util.Format.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digispace.messagevault.export.ArchiveConfig
import com.digispace.messagevault.service.ExportService
import com.digispace.messagevault.service.ExportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RunPhase { IDLE, RUNNING, DONE, ERROR }

data class UiState(
    val config: ArchiveConfig = ArchiveConfig(),
    val phase: RunPhase = RunPhase.IDLE,
    val processed: Int = 0,
    val total: Int = 0,
    val statusLine: String = "Ready.",
    val resultPath: String? = null,
    val attachmentCount: Int = 0,
    /** Attachments the finished run could not extract; > 0 means the archive is incomplete. */
    val attachmentFailures: Int = 0,
    val doneSummary: String? = null,
    val errorMessage: String? = null
) {
    val fraction: Float
        get() = if (total > 0) processed.toFloat() / total else 0f
}

class ArchiveViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("mv_prefs", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(UiState(config = loadConfig()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // The service owns the run; the UI only mirrors it. Collecting here means the
        // screen re-attaches to an export already in flight (rotation, or coming back
        // from the launcher) instead of assuming it started the run itself.
        viewModelScope.launch {
            ExportStatus.state.collect { s -> _state.update { ui -> ui.merge(s) } }
        }
    }

    private fun UiState.merge(s: ExportStatus.Snapshot): UiState = when {
        s.running -> copy(
            phase = RunPhase.RUNNING,
            processed = s.processed,
            total = s.total,
            statusLine = "${s.phase}: ${s.processed}/${s.total}",
            errorMessage = null
        )
        s.error != null -> copy(
            phase = RunPhase.ERROR,
            statusLine = "Failed.",
            errorMessage = s.error
        )
        s.cancelled -> copy(phase = RunPhase.IDLE, statusLine = "Cancelled.")
        s.finishedPath != null -> copy(
            phase = RunPhase.DONE,
            processed = s.messageCount,
            statusLine = "Done — ${s.messageCount} messages.",
            resultPath = s.finishedPath,
            attachmentCount = s.attachmentCount,
            attachmentFailures = s.attachmentFailures,
            doneSummary = s.summary
        )
        else -> this
    }

    fun updateConfig(transform: (ArchiveConfig) -> ArchiveConfig) {
        _state.update { it.copy(config = transform(it.config)) }
        saveConfig(_state.value.config)
    }

    /** Restore the last-used sources/formats, defaulting to ArchiveConfig()'s values. */
    private fun loadConfig(): ArchiveConfig {
        val d = ArchiveConfig()
        return ArchiveConfig(
            includeSms = prefs.getBoolean("cfg_sms", d.includeSms),
            includeMms = prefs.getBoolean("cfg_mms", d.includeMms),
            jsonl = prefs.getBoolean("cfg_jsonl", d.jsonl),
            sqlite = prefs.getBoolean("cfg_sqlite", d.sqlite),
            markdown = prefs.getBoolean("cfg_md", d.markdown),
            extractAttachments = prefs.getBoolean("cfg_attach", d.extractAttachments)
        )
    }

    private fun saveConfig(c: ArchiveConfig) {
        prefs.edit()
            .putBoolean("cfg_sms", c.includeSms)
            .putBoolean("cfg_mms", c.includeMms)
            .putBoolean("cfg_jsonl", c.jsonl)
            .putBoolean("cfg_sqlite", c.sqlite)
            .putBoolean("cfg_md", c.markdown)
            .putBoolean("cfg_attach", c.extractAttachments)
            .apply()
    }

    /** Hands the run to the foreground service so it survives leaving the app. */
    fun start() {
        if (_state.value.phase == RunPhase.RUNNING) return
        _state.update {
            it.copy(
                phase = RunPhase.RUNNING, processed = 0, total = 0,
                statusLine = "Starting…", resultPath = null,
                // Clear the previous run's outcome too: a stale failure count or
                // attachment tally bleeding into the next run's done card would be a lie.
                attachmentCount = 0, attachmentFailures = 0,
                doneSummary = null, errorMessage = null
            )
        }
        val app = getApplication<Application>()
        ContextCompat.startForegroundService(
            app, ExportService.startIntent(app, _state.value.config)
        )
    }

    fun cancel() {
        val app = getApplication<Application>()
        runCatching { app.startService(ExportService.cancelIntent(app)) }
        _state.update { it.copy(phase = RunPhase.IDLE, statusLine = "Cancelled.") }
    }
}
