/*
 * ✒ Metadata
 *     - Title: Export Screen (Message Vault Edition - v2.1)
 *     - File Name: ArchiveScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/ArchiveScreen.kt
 *     - Artifact Type: library
 *     - Version: 2.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 2.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Systemwide consistency pass. This screen's three private treatments were each a twin of something another tab had also invented, so all three are deleted in favour of the shared primitives: FieldPlate (which padded its rows at a raw 2dp, a margin no other tab used) becomes MvFieldPlate, DestinationField becomes MvLocationPlate, and FailureNote becomes MvFailureNote — which also gives Settings a sanctioned in-card crimson instead of the bare crimson sentence it was using. The full-access button is renamed GRANT FULL ACCESS to match the identical operation on Settings, which called it something else in a different casing. Style only.
 *     - 2.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, applying STYLE.md. The six floating option cards collapse into two ruled manifest plates with a catalogue gutter; the Material Switch, the 44dp icon chips and the Sms/PermMedia/Forum messaging glyphs are gone (sources now carry typographic SMS/MMS marks, formats keep the authored ic_tech_* marks bare in the gutter); every produced artefact is named in monospace in its own column; the capsule bar, gold shimmer, breathing dot and rolling counter are replaced by MvMeter and shown figures; the done card is a hairline receipt with the destination on a recessed plate; partial success drops colorScheme.tertiary for a PARTIAL stamp and a note; phase becomes a stamp in the section header; motion is opacity-only on the two MvMotion clocks; copy moves to operational register.
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Quality-of-life pass: the done card no longer reports an unqualified success when attachments failed to extract — it names the count and says what to do; "Share .zip" is now "Share export", because with Encrypt shared exports on the button hands over a .mvault and the old label promised a file type it did not produce.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: card padding, radii, gaps, widths and the stat-pill / primary-button / section-label treatments now come from the shared UiKit instead of private twins; toggle rows clear the 48dp touch target and expose their state to TalkBack as one labelled control; the progress bar is announced.
 *     - 1.0.5 (2026-07-20) [Anthropic - Claude Opus 4.8] — Guard the run: disable the Run / Run again / Try again buttons with a hint until at least one source and one output format are selected, so an empty run can't be started.
 *     - 1.0.4 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Visual & motion overhaul: branded cards, switches, animated phase transitions, animated progress/counter, stat-pill results panel, width-capped centered column.
 *     - 1.0.3 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Done card gains Share .zip / Copy to folder actions.
 *     - 1.0.2 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Add AccessCard nudging the user to grant All-files access so exports land in a browsable folder.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Show the run-metrics throughput summary on the done card (primary color, not error).
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The run screen, in Jetpack Compose. It stays "dumb" — receiving state plus
 *     callbacks, drawing accordingly, and reporting taps — but it now presents that
 *     state the way a records system does: what will be archived is a numbered manifest
 *     of ruled rows, each naming the artefact it produces; what a run is doing is an
 *     instrument readout; what a run produced is a receipt with the destination path as
 *     a field of record. Its public API was untouched by the style pass.
 *
 * ✒ Key Features:
 *     - Two manifest plates (SOURCES, OUTPUT FORMATS): one bordered plate per group, hairline-divided rows, a shared gutter, and the produced artefact named in monospace under each entry.
 *     - Inclusion is stated, not switched: MvStateToggle prints INCLUDED / OMITTED in a fixed column, so the chosen set reads as text and never depends on colour.
 *     - Condition is stamped: IDLE / RUNNING / COMPLETE / PARTIAL / FAILED rides the run card's section rule in every phase, not only on failure.
 *     - Instrument readout: MvMeter's squared, tick-marked bar plus shown (never eased) figures for progress and processed count.
 *     - The receipt: MESSAGES / ATTACHMENTS / MISSING / THROUGHPUT as right-aligned monospace field rows, with the destination path selectable on a recessed plate.
 *     - Warning without hue: a partial run carries a PARTIAL stamp and an MvNote, so it reads identically in both schemes and crimson stays reserved for a genuine failure.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (Material 3, animation); com.digispace.messagevault.export.ArchiveConfig; ui/UiKit.kt.
 *     - Charter: implements STYLE.md. Icons come from MvIcons or the authored ic_tech_* drawables — never from Material's messaging set.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.R
import com.digispace.messagevault.export.ArchiveConfig
import java.util.Locale

@Composable
fun ArchiveScreen(
    state: UiState,
    hasSmsPermission: Boolean,
    hasAllFilesAccess: Boolean,
    onRequestPermission: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onShareRun: () -> Unit,
    onCopyRun: () -> Unit,
    onConfigChange: ((ArchiveConfig) -> ArchiveConfig) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    val running = state.phase == RunPhase.RUNNING
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.ScreenV),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MvReveal(Modifier.widthIn(max = MvContentWidth).fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
                    ) {
                        if (!hasSmsPermission) {
                            PermissionCard(onRequestPermission)
                        } else {
                            if (!hasAllFilesAccess) {
                                AccessCard(enabled = !running, onEnable = onRequestAllFilesAccess)
                            }
                            ConfigCard(state.config, enabled = !running, onConfigChange)
                            val canRun = with(state.config) {
                                (includeSms || includeMms) && (jsonl || sqlite || markdown)
                            }
                            ActionCard(state, canRun, onStart, onCancel, onShareRun, onCopyRun)
                        }
                    }
                }
            }
        }
    }
}

// ── Preconditions ────────────────────────────────────────────────────────────

/**
 * A precondition, stated rather than pitched. The condition is a stamp on the section
 * rule and the headline is the condition named in monospace; only the explanation is
 * prose, because only the explanation is language.
 */
@Composable
private fun PermissionCard(onRequest: () -> Unit) {
    MvCard {
        MvSectionLabel(
            "ACCESS",
            rule = true,
            trailing = { MvStamp("REQUIRED", tone = MvTone.Flagged) }
        )
        Text(
            "SMS PERMISSION REQUIRED",
            style = MvType.MonoValue,
            color = MvInk.Data,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            "Reading the message store requires the SMS permission. It is granted " +
                "on-device, to this sideloaded build, and is never sent anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = MvInk.Body
        )
        MvPrimaryButton("GRANT SMS ACCESS", onClick = onRequest)
    }
}

@Composable
private fun AccessCard(enabled: Boolean, onEnable: () -> Unit) {
    MvCard {
        MvSectionLabel(
            "DESTINATION",
            rule = true,
            trailing = { MvStamp("RESTRICTED", tone = MvTone.Flagged) }
        )
        Text(
            "EXPORT FOLDER NOT BROWSABLE",
            style = MvType.MonoValue,
            color = MvInk.Data,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            "Runs currently save inside the app's private storage (Android/data), which " +
                "the Files app and OneDrive cannot browse. Full access writes them to a " +
                "visible /sdcard/MessageVault/ folder instead.",
            style = MaterialTheme.typography.bodyMedium,
            color = MvInk.Body
        )
        // Named identically to the same operation on Settings, which called it
        // "Grant full access". One operation, one name, one casing.
        MvPrimaryButton("GRANT FULL ACCESS", enabled = enabled, onClick = onEnable)
    }
}

// ── The manifest ─────────────────────────────────────────────────────────────

/**
 * What the run will archive, as two numbered manifests rather than six floating cards.
 * Six independently bordered soft rects in a scroll is the app-store idiom; one bordered
 * plate of hairline-divided rows forces the marks, names and produced artefacts into
 * columns, which is what makes a selection set read as a record instead of a settings pane.
 */
@Composable
private fun ConfigCard(
    config: ArchiveConfig,
    enabled: Boolean,
    onChange: ((ArchiveConfig) -> ArchiveConfig) -> Unit
) {
    val sources = listOf(config.includeSms, config.includeMms).count { it }
    val formats = listOf(config.jsonl, config.sqlite, config.markdown, config.extractAttachments)
        .count { it }

    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Section)) {
        ManifestCard("SOURCES", ordinal = 1, selected = sources, total = 2) {
            ManifestRow(
                mark = { TypographicMark("SMS") },
                title = "Text messages",
                output = null,
                checked = config.includeSms, enabled = enabled
            ) { v -> onChange { it.copy(includeSms = v) } }
            ManifestRow(
                mark = { TypographicMark("MMS") },
                title = "Picture and group messages",
                output = null,
                checked = config.includeMms, enabled = enabled, rule = false
            ) { v -> onChange { it.copy(includeMms = v) } }
        }

        ManifestCard("OUTPUT FORMATS", ordinal = 2, selected = formats, total = 4) {
            ManifestRow(
                mark = { DrawnMark(R.drawable.ic_tech_jsonl) },
                title = "JSONL",
                output = "messages.jsonl",
                checked = config.jsonl, enabled = enabled
            ) { v -> onChange { it.copy(jsonl = v) } }
            ManifestRow(
                mark = { DrawnMark(R.drawable.ic_tech_sqlite) },
                title = "SQLite",
                output = "archive.db",
                checked = config.sqlite, enabled = enabled
            ) { v -> onChange { it.copy(sqlite = v) } }
            ManifestRow(
                mark = { DrawnMark(R.drawable.ic_tech_markdown) },
                title = "Markdown",
                output = "threads/*.md",
                checked = config.markdown, enabled = enabled
            ) { v -> onChange { it.copy(markdown = v) } }
            ManifestRow(
                mark = {
                    Icon(
                        MvIcons.Attachment,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MvInk.Body
                    )
                },
                title = "Attachments",
                output = "media/",
                checked = config.extractAttachments, enabled = enabled, rule = false
            ) { v -> onChange { it.copy(extractAttachments = v) } }
        }
    }
}

/** One manifest group: a labelled, counted section rule over a single ruled plate. */
@Composable
private fun ManifestCard(
    label: String,
    ordinal: Int,
    selected: Int,
    total: Int,
    rows: @Composable ColumnScope.() -> Unit
) {
    MvCard {
        MvSectionLabel(
            label,
            ordinal = ordinal,
            rule = true,
            trailing = { MvCatalogId("${MvOrdinal(selected, 2)} / ${MvOrdinal(total, 2)}") }
        )
        MvPlate(content = rows)
    }
}

/**
 * One manifest line: mark in the shared gutter, name, the artefact it produces in
 * monospace, and the inclusion state as a word in a fixed column. The row keeps
 * Role.Switch, the merged semantics and the 48dp floor it had as a card — the state cell
 * is decorative to the semantics tree, exactly as the Switch it replaces was.
 */
@Composable
private fun ManifestRow(
    mark: @Composable () -> Unit,
    title: String,
    output: String?,
    checked: Boolean,
    enabled: Boolean,
    rule: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        MvStateToggle(
            label = title,
            checked = checked,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(horizontal = MvSpace.Plate),
            enabled = enabled,
            supporting = output,
            leading = mark,
            onText = "INCLUDED",
            offText = "OMITTED"
        )
        // Inset to the text column so the gutter runs uninterrupted down the plate.
        if (rule) MvRule(inset = MvSpace.Plate + MvGutterWidth)
    }
}

/** The literal string as the mark. A glyph is spent only where no word will do. */
@Composable
private fun TypographicMark(text: String) {
    MvMono(text, style = MvType.MonoSmall, color = MvInk.Faint)
}

/** One of the authored technology marks, bare in the gutter — never on a tinted tile. */
@Composable
private fun DrawnMark(resId: Int) {
    Icon(
        painterResource(resId),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = MvInk.Body
    )
}

// ── The run ──────────────────────────────────────────────────────────────────

@Composable
private fun ActionCard(
    state: UiState,
    canRun: Boolean,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onShareRun: () -> Unit,
    onCopyRun: () -> Unit
) {
    val partial = state.phase == RunPhase.DONE && state.attachmentFailures > 0
    val (word, tone) = when {
        state.phase == RunPhase.RUNNING -> "RUNNING" to MvTone.Active
        state.phase == RunPhase.ERROR -> "FAILED" to MvTone.Failed
        partial -> "PARTIAL" to MvTone.Flagged
        state.phase == RunPhase.DONE -> "COMPLETE" to MvTone.Neutral
        else -> "IDLE" to MvTone.Neutral
    }

    MvCard {
        MvSectionLabel(
            "RUN",
            ordinal = 3,
            rule = true,
            trailing = { MvStamp(word, tone = tone) }
        )
        // Opacity only: a ledger changes state, it does not travel into place.
        AnimatedContent(
            targetState = state.phase,
            transitionSpec = { fadeIn(MvMotion.settle()).togetherWith(fadeOut(MvMotion.snap())) },
            label = "phase"
        ) { phase ->
            when (phase) {
                RunPhase.RUNNING -> RunningBody(state, onCancel)
                RunPhase.DONE -> DoneBody(state, canRun, onStart, onShareRun, onCopyRun)
                RunPhase.ERROR -> ErrorBody(state, canRun, onStart)
                RunPhase.IDLE -> IdleBody(state, canRun, onStart)
            }
        }
    }
}

@Composable
private fun IdleBody(state: UiState, canRun: Boolean, onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        MvFieldPlate {
            MvFieldRow("STATUS", state.statusLine, rule = false)
        }
        // Metadata made visible before the run, not revealed only on success.
        state.resultPath?.let { MvLocationPlate(it, label = "LAST LOCATION") }
        if (!canRun) RunHint()
        MvPrimaryButton("BEGIN RUN", enabled = canRun, onClick = onStart)
    }
}

/** Why the run is unavailable, as a marginal note rather than a tinted sentence. */
@Composable
private fun RunHint() {
    MvNote("Select at least one source and at least one output format.")
}

@Composable
private fun RunningBody(state: UiState, onCancel: () -> Unit) {
    val measured = state.total > 0
    val percent = (state.fraction.coerceIn(0f, 1f) * 100).toInt()
    val stage = state.statusLine.substringBefore(":").trim().uppercase(Locale.US)

    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        MvMeter(
            progress = if (measured) state.fraction else null,
            contentDescription = if (measured) {
                "Export progress: $percent percent"
            } else {
                "Export in progress"
            }
        )
        MvFieldPlate {
            // Stages change mid-run (Reading SMS, Reading MMS, Writing) — a crossfade
            // reads as a display updating rather than a flicker.
            Crossfade(targetState = stage, animationSpec = MvMotion.snap(), label = "stage") { s ->
                MvFieldRow("STAGE", s)
            }
            if (measured) {
                MvFieldRow("PROGRESS", "$percent%", valueStyle = MvType.MonoValue, accent = true)
                // Shown, not eased: a figure that animates toward its true value briefly
                // misreports the run on a tool whose whole promise is accuracy.
                MvFieldRow(
                    "PROCESSED",
                    "${MvNum(state.processed)} / ${MvNum(state.total)}",
                    rule = false
                )
            } else {
                MvFieldRow("PROCESSED", MvNum(state.processed), rule = false)
            }
        }
        MvSecondaryButton("CANCEL", Modifier.fillMaxWidth(), onClick = onCancel)
    }
}

/**
 * A finished run as a receipt: hairline-ruled field rows with the figures aligned to one
 * right edge, the condition already stamped on the section rule above, and the
 * destination — the most archival fact the app knows — as a selectable field of record.
 */
@Composable
private fun DoneBody(
    state: UiState,
    canRun: Boolean,
    onStart: () -> Unit,
    onShareRun: () -> Unit,
    onCopyRun: () -> Unit
) {
    val partial = state.attachmentFailures > 0
    val throughput = state.doneSummary
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        MvFieldPlate {
            MvFieldRow(
                "MESSAGES",
                MvNum(state.processed),
                valueStyle = MvType.MonoValue,
                accent = true
            )
            MvFieldRow(
                "ATTACHMENTS",
                MvNum(state.attachmentCount),
                valueStyle = MvType.MonoValue,
                rule = partial || throughput != null
            )
            if (partial) {
                MvFieldRow(
                    "MISSING",
                    MvNum(state.attachmentFailures),
                    valueStyle = MvType.MonoValue,
                    rule = throughput != null
                )
            }
            throughput?.let { MvFieldRow("THROUGHPUT", it, rule = false) }
        }
        if (partial) {
            // No hue: the PARTIAL stamp and this rule carry the condition in both schemes,
            // which colorScheme.tertiary could not — it is Gold in dark and Slate in light.
            MvNote(
                "${state.attachmentFailures} attachment${if (state.attachmentFailures == 1) "" else "s"} " +
                    "could not be read out of the MMS store and ${if (state.attachmentFailures == 1) "is" else "are"} " +
                    "missing from this archive. The messages themselves are complete. " +
                    "Running the export again often recovers them; if it does not, the " +
                    "original media is no longer on this device.",
                label = "PARTIAL"
            )
        }
        state.resultPath?.let { MvLocationPlate(it) }
        MvCardFooter {
            // Not "Share .zip": with Encrypt shared exports on this hands over a sealed
            // .mvault, and a label naming a file type it does not produce is a lie.
            MvTextAction("SHARE EXPORT", onShareRun)
            MvTextAction("COPY TO FOLDER", onCopyRun)
        }
        if (!canRun) RunHint()
        MvPrimaryButton("RUN AGAIN", enabled = canRun, onClick = onStart)
    }
}

@Composable
private fun ErrorBody(state: UiState, canRun: Boolean, onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        MvFailureNote(state.errorMessage ?: "The run stopped before it produced an archive.")
        if (!canRun) RunHint()
        MvPrimaryButton("RETRY", enabled = canRun, onClick = onStart)
    }
}

// The three private treatments that used to close this file — FieldPlate, DestinationField
// and FailureNote — are gone. Every other tab had grown its own copy of each, at its own
// padding and with its own label spelling, so all three now live in UiKit as MvFieldPlate,
// MvLocationPlate and MvFailureNote and this screen composes them like everyone else.
