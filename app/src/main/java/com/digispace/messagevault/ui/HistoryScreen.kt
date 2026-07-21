/*
 * ✒ Metadata
 *     - Title: History Screen (Message Vault Edition - v1.0)
 *     - File Name: HistoryScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/HistoryScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.2.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Deletion tells the truth: RunHistory.delete's boolean was discarded, so a partial failure just re-listed the archive as if nothing had happened; the outcome is now reported. Deleting a 700MB run also takes real time — the dialog stays up with the buttons disabled and a "Deleting…" line instead of freezing on a dead Delete button, and it can no longer be dismissed (or re-fired) mid-delete. The confirmation now names the archive's message count alongside its size, since size alone doesn't convey what is being thrown away.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: shared UiKit tokens and card/label/empty-state primitives replace the private twins; the history read is guarded and gains a real error state; loading is labelled; the four states cross-fade and run cards fade-and-rise in on first paint; row actions clear the 48dp touch target and the tap target is described.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — Tap a run card to browse that archive in-app (when it has a SQLite db); a hint marks browsable runs and a toast explains the rest.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Delete a run from the list, guarded by a confirmation dialog; the header and list refresh after removal.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Aggregate header (archives · total size · total messages) above the run list; reloads on resume.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial History screen.
 *
 * ✒ Description:
 *     The History destination: a list of every past export run found in the
 *     export folder, each shown with its headline metrics (read from that run's
 *     metrics.json) plus Share / Copy actions. Lets you revisit and re-deliver an
 *     archive without re-running the export.
 *
 * ✒ Key Features:
 *     - Run listing: enumerates past export runs from the export folder via RunHistory.
 *     - Headline metrics: surfaces each run's message/attachment counts read from metrics.json.
 *     - Re-delivery: Share / Copy actions route paths back up to MainActivity through callbacks.
 *     - Guarded deletion: a confirmation naming what is being destroyed, an in-flight "Deleting…" state that locks the dialog, and a reported outcome — success or a partial failure with a next step.
 *     - Background load: reads run history off the main thread on Dispatchers.IO, wrapped in runCatching so an unreadable folder becomes an error state with a Retry.
 *     - All four states covered: labelled loading, intentional empty, crimson-titled error, and the run list — cross-faded, with each card easing in.
 *
 * ✒ Other Important Information:
 *     - Dependencies: com.digispace.messagevault.storage.RunHistory / RunSummary; Jetpack Compose Material3; kotlinx.coroutines.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.digispace.messagevault.storage.RunHistory
import com.digispace.messagevault.storage.RunSummary
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** What History is showing right now — one value so the four states can cross-fade. */
private sealed interface HistoryState {
    data object Loading : HistoryState
    data class Failed(val message: String) : HistoryState
    data class Loaded(val runs: List<RunSummary>) : HistoryState
}

@Composable
fun HistoryScreen(
    onShareDir: (String) -> Unit,
    onCopyDir: (String) -> Unit,
    onBrowseDir: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<HistoryState>(HistoryState.Loading) }
    var pendingDelete by remember { mutableStateOf<RunSummary?>(null) }
    var deleting by remember { mutableStateOf(false) }

    // History is read-only: an export folder that has gone missing or become unreadable
    // is reported in-place, never thrown into the UI.
    fun reload() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { RunHistory.list(context) } }
            state = result.fold(
                onSuccess = { HistoryState.Loaded(it) },
                onFailure = { HistoryState.Failed(it.message ?: "The export folder could not be read.") }
            )
        }
    }
    // Reload on every resume so a new export appears without an app restart.
    OnScreenResume { reload() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.ScreenV),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
    ) {
        // Cross-fade on the PHASE, not on the state object: a resume-time reload builds a
        // new Loaded instance every time, and keying on that would replay the fade — and
        // every card's entrance — on each return to History.
        val phase = when (val s = state) {
            is HistoryState.Loading -> "loading"
            is HistoryState.Failed -> "error"
            is HistoryState.Loaded -> if (s.runs.isEmpty()) "empty" else "list"
        }
        Crossfade(targetState = phase, animationSpec = tween(260), label = "history") { p ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
            ) {
                when (p) {
                    "loading" -> MvLoadingState("Reading your archives…")
                    "error" -> MvErrorState(
                        title = "Couldn't read your archives",
                        message = (state as? HistoryState.Failed)?.message.orEmpty()
                    ) {
                        MvPrimaryButton("Try again", onClick = { reload() })
                    }
                    "empty" -> MvEmptyState(
                        title = "No exports yet",
                        message = "Run an export from the Export screen and it will appear here."
                    )
                    else -> {
                        val runs = (state as? HistoryState.Loaded)?.runs.orEmpty()
                        HistoryHeader(runs)
                        runs.forEachIndexed { index, run ->
                            RiseIn(index) {
                                RunHistoryCard(
                                    run = run,
                                    onShareDir = onShareDir,
                                    onCopyDir = onCopyDir,
                                    onDelete = { pendingDelete = run },
                                    onOpen = {
                                        if (File(run.dir, "archive.db").exists()) {
                                            onBrowseDir(run.dir.absolutePath)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "This run has no queryable database — re-export with SQLite enabled to browse it.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            // Nothing dismisses this while the delete is in flight: a tap-outside used to
            // close the dialog over a still-running recursive delete, leaving the user
            // watching a list that silently changed under them.
            onDismissRequest = { if (!deleting) pendingDelete = null },
            title = { Text("Delete this archive?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
                    Text(
                        "${target.startedLabel} — " +
                            "${"%,d".format(Locale.US, target.totalMessages)} messages, " +
                            "${target.attachmentCount} attachments, " +
                            "${Format.bytes(target.sizeBytes)} — will be permanently removed " +
                            "from this device. This can't be undone."
                    )
                    if (deleting) {
                        // Wiping hundreds of MB of attachments is not instant; without this
                        // the dialog just sat there looking like the button hadn't worked.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Deleting…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        deleting = true
                        scope.launch {
                            // The boolean was thrown away here: a delete that failed part
                            // way (a file held open by another app, a folder gone
                            // read-only) simply re-listed the run as though nothing had
                            // been asked for.
                            val ok = withContext(Dispatchers.IO) {
                                runCatching { RunHistory.delete(target.dir) }.getOrDefault(false)
                            }
                            deleting = false
                            pendingDelete = null
                            Toast.makeText(
                                context,
                                if (ok) "Archive deleted."
                                else "Couldn't fully delete this archive — some files are still " +
                                    "on the device. Close any app reading them and try again.",
                                if (ok) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                            ).show()
                            reload()
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(enabled = !deleting, onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

/**
 * A card eases up into place the first time it is composed, lightly staggered down the
 * list. It runs once — a resume-time reload must not replay the whole animation.
 */
@Composable
private fun RiseIn(index: Int, content: @Composable () -> Unit) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    val delayMillis = (index.coerceAtMost(6)) * 40
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(260, delayMillis)) +
            slideInVertically(tween(260, delayMillis)) { it / 10 }
    ) { content() }
}

@Composable
private fun HistoryHeader(runs: List<RunSummary>) {
    val totalMessages = runs.sumOf { it.totalMessages }
    val totalBytes = runs.sumOf { it.sizeBytes }
    Card(
        shape = MvShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxWidth().widthCap()
    ) {
        Column(Modifier.padding(MvSpace.Card), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${runs.size} archive${if (runs.size == 1) "" else "s"} · ${Format.bytes(totalBytes)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                "${"%,d".format(Locale.US, totalMessages)} messages archived on device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun RunHistoryCard(
    run: RunSummary,
    onShareDir: (String) -> Unit,
    onCopyDir: (String) -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val browsable = remember(run.dir) { File(run.dir, "archive.db").exists() }
    MvCard(
        onClick = onOpen,
        contentDescription = if (browsable) "Browse this archive" else "Archive details"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            Text(
                run.startedLabel,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            if (run.complete) {
                Text(
                    "${"%,d".format(Locale.US, run.totalMessages)} messages  ·  " +
                        "SMS ${run.smsCount} / MMS ${run.mmsCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${run.attachmentCount} attachments · ${run.attachmentBytesHuman}" +
                        if (run.wallHuman.isNotEmpty()) " · ${run.wallHuman}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (run.dateMin != null && run.dateMax != null) {
                    Text(
                        "${run.dateMin} → ${run.dateMax}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Text(
                    "Incomplete run (no metrics) — likely cancelled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                if (browsable) "Tap to browse this archive →" else "Not browsable (no SQLite db)",
                style = MaterialTheme.typography.labelSmall,
                color = if (browsable) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            // Three abbreviated labels need tight horizontal padding to fit a narrow
            // phone, but the rows must still clear the 48dp touch-target floor.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tight = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                val action = Modifier.weight(1f).heightIn(min = MvTouchTarget)
                OutlinedButton(
                    onClick = { onShareDir(run.dir.absolutePath) },
                    modifier = action, contentPadding = tight, shape = MvShape.Control
                ) { Text("Share", maxLines = 1) }
                OutlinedButton(
                    onClick = { onCopyDir(run.dir.absolutePath) },
                    modifier = action, contentPadding = tight, shape = MvShape.Control
                ) { Text("Copy", maxLines = 1) }
                // Deletion is destructive and permanent — the one crimson control here.
                OutlinedButton(
                    onClick = onDelete,
                    modifier = action,
                    contentPadding = tight,
                    shape = MvShape.Control,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", maxLines = 1) }
            }
        }
    }
}

/** Keeps cards from stretching too wide on the Fold's inner screen. */
private fun Modifier.widthCap(): Modifier = this.widthIn(max = MvContentWidth)
