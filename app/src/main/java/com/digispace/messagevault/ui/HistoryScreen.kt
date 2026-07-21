/*
 * ✒ Metadata
 *     - Title: History Screen (Message Vault Edition - v2.1)
 *     - File Name: HistoryScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/HistoryScreen.kt
 *     - Artifact Type: library
 *     - Version: 2.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 2.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Systemwide consistency pass. The head-plate's field rows were the only aggregate block in the app not actually on a plate; they are now MvFieldPlate, and its ON DISK figure prints the raw byte count through MvBytes like the roll-ups on Home and Settings. The record's LOCATION was labelled with an MvMono inside its plate where three other tabs labelled the same thing with a section label above it — MvLocationPlate now does it everywhere. The private ackClock() was a byte-for-byte duplicate of Settings' clockStamp(); both are MvClock(). Every acknowledgement is stamped (two were not) and all of them now expire on the shared MvAckHoldMs, where these used to stand until the screen was rebuilt. Style only.
 *     - 2.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, applying STYLE.md. History is now a register rather than a card feed: every run prints its accession slug, its register ordinal in the shared gutter, its on-disk location, and a stamped condition; the prose metric lines became a ruled field table whose values all align to one right edge, with every integer normalised through MvNum. The tinted, shadow-casting aggregate banner became a hairline VAULT INVENTORY head-plate, the three equal-weight rounded pills became a ruled MvCardFooter with the destructive action separated out, the confirmation became a field manifest, both Toasts became a timestamped in-card acknowledgement, the staggered rise became one opacity-only MvReveal, and the six ad-hoc alphas plus the light-scheme-collapsing tertiary hint were replaced by the MvInk scale and the INDEXED / NO INDEX stamp.
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Deletion tells the truth: RunHistory.delete's boolean was discarded, so a partial failure just re-listed the archive as if nothing had happened; the outcome is now reported. Deleting a 700MB run also takes real time — the dialog stays up with the buttons disabled and a "Deleting…" line instead of freezing on a dead Delete button, and it can no longer be dismissed (or re-fired) mid-delete. The confirmation now names the archive's message count alongside its size, since size alone doesn't convey what is being thrown away.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: shared UiKit tokens and card/label/empty-state primitives replace the private twins; the history read is guarded and gains a real error state; loading is labelled; the four states cross-fade and run cards fade-and-rise in on first paint; row actions clear the 48dp touch target and the tap target is described.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — Tap a run card to browse that archive in-app (when it has a SQLite db); a hint marks browsable runs and a toast explains the rest.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Delete a run from the list, guarded by a confirmation dialog; the header and list refresh after removal.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Aggregate header (archives · total size · total messages) above the run list; reloads on resume.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial History screen.
 *
 * ✒ Description:
 *     The run ledger: every export run found in the export folder, entered as a numbered
 *     record. Each entry carries its accession slug, its register ordinal, where it lives
 *     on disk, its measured figures as a ruled field table, and a stamped condition — plus
 *     Share / Copy / Delete and a tap to browse that archive without re-running the export.
 *
 * ✒ Key Features:
 *     - Register, not feed: a numbered ordinal in the shared 32dp gutter, the accession slug as the record's identity, the friendly date demoted to a secondary mono line.
 *     - Ruled field table: RECORDS / SMS / MMS / ATTACHMENTS / MEDIA / SPAN / ELAPSED / ON DISK, values right-aligned on one edge, every integer grouped through MvNum.
 *     - Metadata surfaced: the on-disk path Share and Copy act on is printed on a recessed plate rather than taken on trust.
 *     - Condition stamped on every entry: COMPLETE / PARTIAL and INDEXED / NO INDEX, worded so state never depends on colour and survives both schemes.
 *     - Guarded deletion: a field-manifest confirmation naming what is being destroyed, an in-flight lock, and the outcome recorded as a timestamped acknowledgement in the inventory head-plate.
 *     - Background load: reads run history off the main thread on Dispatchers.IO, wrapped in runCatching so an unreadable folder becomes an error state with a retry.
 *     - All four states covered — labelled measuring, NO RECORDS, EXPORT FOLDER UNREADABLE, and the ledger — cross-faded at MvMotion.Snap with one opacity-only reveal.
 *
 * ✒ Other Important Information:
 *     - Dependencies: com.digispace.messagevault.storage.RunHistory / RunSummary; ui/UiKit.kt primitives; Jetpack Compose Material3; kotlinx.coroutines.
 *     - Style authority: STYLE.md. No Toasts, no elevation, no stagger, no tertiary, no bullet-separated metric prose.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.storage.RunHistory
import com.digispace.messagevault.storage.RunSummary
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** What History is showing right now — one value so the four states can cross-fade. */
private sealed interface HistoryState {
    data object Loading : HistoryState
    data class Failed(val message: String) : HistoryState
    data class Loaded(val runs: List<RunSummary>) : HistoryState
}

// The private ackClock() that used to live here was a byte-for-byte duplicate of
// Settings' clockStamp(). Both are now MvClock().

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
    // Replaces both Toasts. Reported in-place, in the head-plate, with a timestamp.
    var ack by remember { mutableStateOf<String?>(null) }
    // An acknowledgement stands for the same interval it stands for on every other tab.
    // This one used to persist until the screen was rebuilt, so a line reporting a delete
    // was still sitting on the head-plate several visits later, reading as current.
    LaunchedEffect(ack) { if (ack != null) { delay(MvAckHoldMs); ack = null } }

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
        // the whole reveal — on each return to History.
        val phase = when (val s = state) {
            is HistoryState.Loading -> "loading"
            is HistoryState.Failed -> "error"
            is HistoryState.Loaded -> if (s.runs.isEmpty()) "empty" else "list"
        }
        Crossfade(targetState = phase, animationSpec = MvMotion.snap(), label = "history") { p ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
            ) {
                when (p) {
                    "loading" -> MvLoadingState("Reading ledger")
                    "error" -> MvErrorState(
                        title = "EXPORT FOLDER UNREADABLE",
                        message = (state as? HistoryState.Failed)?.message.orEmpty()
                    ) {
                        MvPrimaryButton("RETRY", onClick = { reload() })
                    }
                    "empty" -> MvEmptyState(
                        title = "NO RECORDS",
                        message = "Run an export to open this ledger."
                    )
                    else -> {
                        val runs = (state as? HistoryState.Loaded)?.runs.orEmpty()
                        // One entrance for the whole region, opacity only. Records do not
                        // arrive; they were already there.
                        MvReveal {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
                            ) {
                                InventoryPlate(runs, ack)
                                MvSectionLabel(
                                    "RUN LEDGER",
                                    ordinal = 2,
                                    rule = true,
                                    // Aligned to the same edge the cards stop at, so the
                                    // rubric sits over its register on the Fold's inner screen.
                                    modifier = Modifier.widthIn(max = MvContentWidth)
                                )
                                runs.forEachIndexed { index, run ->
                                    RunHistoryCard(
                                        run = run,
                                        // Newest first on screen, but the register numbers
                                        // from the oldest accession up, so an entry's
                                        // number never changes when a new run lands.
                                        ordinal = runs.size - index,
                                        onShareDir = onShareDir,
                                        onCopyDir = onCopyDir,
                                        onDelete = { pendingDelete = run },
                                        onOpen = {
                                            if (File(run.dir, "archive.db").exists()) {
                                                onBrowseDir(run.dir.absolutePath)
                                            } else {
                                                ack = "NO INDEX · ${run.name} · " +
                                                    "RE-EXPORT WITH SQLITE · ${MvClock()}"
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
    }

    pendingDelete?.let { target ->
        MvConfirmDialog(
            title = "DESTROY RECORD",
            fields = listOf(
                "RECORD" to target.name,
                "STARTED" to target.startedLabel,
                "MESSAGES" to MvNum(target.totalMessages),
                "ATTACHMENTS" to MvNum(target.attachmentCount),
                "ON DISK" to Format.bytes(target.sizeBytes)
            ),
            consequence = if (deleting) {
                "Removing files from this device."
            } else {
                "The archive above is permanently removed from this device. This cannot be undone."
            },
            confirmLabel = if (deleting) "DELETING" else "DELETE",
            busy = deleting,
            // Nothing dismisses this while the delete is in flight: a tap-outside used to
            // close the dialog over a still-running recursive delete, leaving the user
            // watching a list that silently changed under them.
            onDismiss = { if (!deleting) pendingDelete = null },
            onConfirm = {
                deleting = true
                scope.launch {
                    // The boolean was thrown away here once: a delete that failed part way
                    // (a file held open by another app, a folder gone read-only) simply
                    // re-listed the run as though nothing had been asked for.
                    val ok = withContext(Dispatchers.IO) {
                        runCatching { RunHistory.delete(target.dir) }.getOrDefault(false)
                    }
                    deleting = false
                    pendingDelete = null
                    ack = if (ok) {
                        "RECORD DESTROYED · ${target.name} · ${MvClock()}"
                    } else {
                        "DESTROY INCOMPLETE · ${target.name} · " +
                            "FILES STILL ON DEVICE · ${MvClock()}"
                    }
                    reload()
                }
            }
        )
    }
}

/**
 * The ledger's head-plate: the register's own rubric and its running totals, as labelled
 * field rows on the same right edge every record below uses. It was a tinted Material card
 * casting the default shadow, which is exactly the treatment MvCard exists to kill.
 */
@Composable
private fun InventoryPlate(runs: List<RunSummary>, ack: String?) {
    val totalMessages = runs.sumOf { it.totalMessages }
    val totalBytes = runs.sumOf { it.sizeBytes }
    MvCard(spacing = MvSpace.Item) {
        MvSectionLabel("VAULT INVENTORY", ordinal = 1, rule = true)
        // On a plate, like every other aggregate block in the app. These rows were loose
        // in the card here, so the one head-plate that sets the register's right edge was
        // the only field stack in the app not bounded by one. MvBytes for the same reason
        // Home and Settings use it: a measurement block prints the raw count too.
        MvFieldPlate {
            MvFieldRow("ARCHIVES", MvNum(runs.size))
            MvFieldRow("RECORDS", MvNum(totalMessages), valueStyle = MvType.MonoValue, accent = true)
            MvFieldRow("ON DISK", MvBytes(totalBytes), rule = false)
        }
        MvInlineAck(ack)
    }
}

@Composable
private fun RunHistoryCard(
    run: RunSummary,
    ordinal: Int,
    onShareDir: (String) -> Unit,
    onCopyDir: (String) -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val browsable = remember(run.dir) { File(run.dir, "archive.db").exists() }
    MvCard(
        onClick = onOpen,
        // The click label is the affordance. A card never prints "Tap to … →".
        contentDescription = if (browsable) "Browse archive ${run.name}" else "Archive ${run.name}"
    ) {
        // Identity: register ordinal in the shared gutter, accession slug as the record's
        // name, condition stamped on the same baseline. The friendly date is demoted —
        // a large humanised timestamp as a card's biggest text is a chat thread header.
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.width(MvGutterWidth)) {
                MvMono(MvOrdinal(ordinal), style = MvType.MonoSmall, color = MvInk.Faint)
            }
            Column(
                Modifier.weight(1f).padding(end = MvSpace.Inline),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MvMono(
                    run.name,
                    style = MvType.MonoValue,
                    maxLines = 1,
                    modifier = Modifier.semantics { heading() }
                )
                MvCatalogId(run.startedLabel)
            }
            MvStamp(
                if (run.complete) "COMPLETE" else "PARTIAL",
                tone = if (run.complete) MvTone.Neutral else MvTone.Flagged
            )
        }

        // Where the record physically lives — the value Share and Copy act on, printed
        // rather than taken on trust.
        MvLocationPlate(run.dir.absolutePath, singleLine = true)

        MvFieldPlate {
            if (run.complete) {
                MvFieldRow(
                    "RECORDS",
                    MvNum(run.totalMessages),
                    valueStyle = MvType.MonoValue,
                    accent = true
                )
                MvFieldRow("SMS", MvNum(run.smsCount))
                MvFieldRow("MMS", MvNum(run.mmsCount))
                MvFieldRow("ATTACHMENTS", MvNum(run.attachmentCount))
                MvFieldRow("MEDIA", run.attachmentBytesHuman)
                if (run.dateMin != null && run.dateMax != null) {
                    // Range notation, not an arrow: an arrow is a UI glyph implying
                    // movement, and nothing here moves.
                    MvFieldRow("SPAN", "${run.dateMin} .. ${run.dateMax}")
                }
                if (run.wallHuman.isNotEmpty()) MvFieldRow("ELAPSED", run.wallHuman)
            }
            // One record's size, so Format.bytes alone — MvBytes and its raw count are
            // for the measurement blocks, or every ledger row grows a parenthetical.
            MvFieldRow("ON DISK", Format.bytes(run.sizeBytes))
            // Browsability is a condition of the record, stamped in the same column
            // as every other condition — not a coloured "tap to browse" hint, which
            // collapsed to plain slate in the light scheme.
            MvFieldRow(
                "INDEX",
                "",
                rule = false,
                trailing = {
                    MvStamp(
                        if (browsable) "INDEXED" else "NO INDEX",
                        tone = if (browsable) MvTone.Neutral else MvTone.Flagged
                    )
                }
            )
        }

        if (!run.complete) {
            MvNote("metrics.json is absent — this run was cancelled or interrupted before it wrote its figures.")
        }

        // A document footer: one hairline, then text actions. Three equal-weight rounded
        // pills is a photo-app toolbar, and it gave permanent destruction the same shape
        // and weight as copying a string.
        MvCardFooter {
            MvTextAction("SHARE", onClick = { onShareDir(run.dir.absolutePath) })
            MvTextAction("COPY", onClick = { onCopyDir(run.dir.absolutePath) })
            Box(Modifier.weight(1f))
            MvTextAction("DELETE", onClick = onDelete, destructive = true)
        }
    }
}
