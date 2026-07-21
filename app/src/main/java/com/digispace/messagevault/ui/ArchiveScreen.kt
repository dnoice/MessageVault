/*
 * ✒ Metadata
 *     - Title: Export Screen (Message Vault Edition - v1.0)
 *     - File Name: ArchiveScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/ArchiveScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.2.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
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
 *     The entire user interface, in Jetpack Compose. It stays "dumb" — receiving
 *     state plus callbacks, drawing accordingly, and reporting taps — but is built
 *     for feel: branded Material 3 surfaces with animated transitions between the
 *     idle / running / done / error phases. Use it as the ui layer that MainActivity
 *     feeds; its public API was untouched by the visual overhaul.
 *
 * ✒ Key Features:
 *     - AnimatedContent: cross-fades/slides between phase bodies so the card morphs instead of snapping when a run starts, finishes, or fails.
 *     - animateFloatAsState / animateIntAsState: the progress bar eases toward its target and the processed counter rolls rather than jumping.
 *     - widthIn(max = …) + CenterHorizontally: one centered, capped column that reads intentionally on the Fold's wide inner screen.
 *     - Explicit Card/Surface colors: the palette (parchment surfaces, navy/gold accents) is applied on purpose, with crimson reserved for genuine errors.
 *     - Delivery actions: stat-pill results panel plus Share export / Copy to folder / grant-access controls.
 *     - Honest completion: a run that lost attachments says so in the headline and explains the recovery step, rather than reporting a clean success.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (Material 3, animation); com.digispace.messagevault.export.ArchiveConfig.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.export.ArchiveConfig

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
                Column(
                    Modifier.widthIn(max = MvContentWidth).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
                ) {
                    if (!hasSmsPermission) {
                        PermissionCard(onRequestPermission)
                    } else {
                        if (!hasAllFilesAccess) AccessCard(enabled = !running, onEnable = onRequestAllFilesAccess)
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

/** A branded surface card with a title and content. */
@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        shape = MvShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize(tween(220))
    ) {
        Column(Modifier.padding(MvSpace.Card)) { content() }
    }
}

@Composable
private fun PermissionCard(onRequest: () -> Unit) {
    SectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
            CardTitle("Permission needed")
            Text(
                "Reading messages requires the SMS permission. You grant it here, " +
                    "on-device — this is your own sideloaded build.",
                style = MaterialTheme.typography.bodyMedium
            )
            MvPrimaryButton("Grant SMS access", onClick = onRequest)
        }
    }
}

@Composable
private fun AccessCard(enabled: Boolean, onEnable: () -> Unit) {
    SectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
            CardTitle("Make exports reachable")
            Text(
                "Runs currently save inside the app's private storage (Android/data), " +
                    "which the Files app and OneDrive can't browse. Grant full access to " +
                    "save them to a visible /sdcard/MessageVault/ folder instead.",
                style = MaterialTheme.typography.bodyMedium
            )
            MvPrimaryButton("Enable full access", enabled = enabled, onClick = onEnable)
        }
    }
}

@Composable
private fun ConfigCard(
    config: ArchiveConfig,
    enabled: Boolean,
    onChange: ((ArchiveConfig) -> ArchiveConfig) -> Unit
) {
    SectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            GroupLabel("SOURCES")
            ToggleRow("SMS", "Text messages", config.includeSms, enabled) { v -> onChange { it.copy(includeSms = v) } }
            ToggleRow("MMS", "Picture & group messages, with attachments", config.includeMms, enabled) { v -> onChange { it.copy(includeMms = v) } }

            Spacer(Modifier.height(14.dp))
            GroupLabel("OUTPUT FORMATS")
            ToggleRow("JSONL", "messages.jsonl — one object per line", config.jsonl, enabled) { v -> onChange { it.copy(jsonl = v) } }
            ToggleRow("SQLite", "archive.db — queryable database", config.sqlite, enabled) { v -> onChange { it.copy(sqlite = v) } }
            ToggleRow("Markdown", "One readable transcript per conversation", config.markdown, enabled) { v -> onChange { it.copy(markdown = v) } }
            ToggleRow("Extract attachments", "Decode MMS media to real files", config.extractAttachments, enabled) { v -> onChange { it.copy(extractAttachments = v) } }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    MvSectionLabel(text, Modifier.padding(bottom = 6.dp))
}

/** Card headings are real headings — TalkBack can jump between them. */
@Composable
private fun CardTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() }
    )
}

/**
 * Label + subtitle + switch. The row is one merged, toggleable control: the whole row
 * is the tap target (comfortably past 48dp) and a screen reader hears one switch named
 * by its title rather than three unrelated fragments.
 */
@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MvTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onToggle
            )
            .semantics(mergeDescendants = true) { }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        // null: the row owns the click, so the switch isn't a second focusable target.
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ActionCard(
    state: UiState,
    canRun: Boolean,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onShareRun: () -> Unit,
    onCopyRun: () -> Unit
) {
    SectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Section)) {
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 8 })
                        .togetherWith(fadeOut(tween(140)))
                },
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
}

@Composable
private fun IdleBody(state: UiState, canRun: Boolean, onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        StatusLine(state.statusLine)
        if (!canRun) RunHint()
        MvPrimaryButton("Run export", enabled = canRun, onClick = onStart)
    }
}

/** Shown when the config can't produce anything, explaining the disabled Run button. */
@Composable
private fun RunHint() {
    Text(
        "Pick at least one source (SMS / MMS) and one output format.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
private fun RunningBody(state: UiState, onCancel: () -> Unit) {
    val target = if (state.total > 0) state.fraction else 0f
    val fraction by animateFloatAsState(target, tween(450), label = "frac")
    val processed by animateIntAsState(state.processed, tween(450), label = "count")
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Section)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
            CircularProgressIndicator(
                Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                state.statusLine.substringBefore(":").trim(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            if (state.total > 0) {
                Text(
                    "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (state.total > 0) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth().height(8.dp).clip(CircleShape)
                    .semantics {
                        contentDescription = "Export progress: ${(fraction * 100).toInt()} percent"
                    }
            )
            Text(
                "$processed / ${state.total} messages",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth().height(8.dp).clip(CircleShape)
                    .semantics { contentDescription = "Export in progress" }
            )
        }
        MvSecondaryButton("Cancel", Modifier.fillMaxWidth(), onClick = onCancel)
    }
}

@Composable
private fun DoneBody(state: UiState, canRun: Boolean, onStart: () -> Unit, onShareRun: () -> Unit, onCopyRun: () -> Unit) {
    val partial = state.attachmentFailures > 0
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Section)) {
        Text(
            // A run that could not extract media is not a clean success, and saying so
            // only in the monospace summary line buried it. Name it in the headline.
            if (partial) "Export complete — with missing files" else "Export complete",
            style = MaterialTheme.typography.titleLarge,
            color = if (partial) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        if (partial) {
            Text(
                "${state.attachmentFailures} attachment${if (state.attachmentFailures == 1) "" else "s"} " +
                    "could not be read out of the MMS store, so ${if (state.attachmentFailures == 1) "it is" else "they are"} " +
                    "missing from this archive. The messages themselves are complete. " +
                    "Running the export again often recovers them; if it doesn't, the " +
                    "original media is no longer on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
        state.doneSummary?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            MvStatPill("MESSAGES", "${state.processed}", Modifier.weight(1f))
            MvStatPill("ATTACHMENTS", "${state.attachmentCount}", Modifier.weight(1f))
        }
        state.resultPath?.let {
            Text(
                "Saved to",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            // Not "Share .zip": with Encrypt shared exports on this hands over a sealed
            // .mvault, and a button naming a file type it doesn't produce is a lie.
            MvSecondaryButton("Share export", Modifier.weight(1f), onClick = onShareRun)
            MvSecondaryButton("Copy to folder", Modifier.weight(1f), onClick = onCopyRun)
        }
        if (!canRun) RunHint()
        MvPrimaryButton("Run again", enabled = canRun, onClick = onStart)
    }
}

@Composable
private fun ErrorBody(state: UiState, canRun: Boolean, onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Item)) {
        Text(
            "Export failed",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { heading() }
        )
        // The message is the error; the body stays on-surface so crimson marks the
        // failure rather than shouting a whole paragraph of it.
        Text(
            state.errorMessage ?: "Something went wrong.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        if (!canRun) RunHint()
        MvPrimaryButton("Try again", enabled = canRun, onClick = onStart)
    }
}

@Composable
private fun StatusLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
