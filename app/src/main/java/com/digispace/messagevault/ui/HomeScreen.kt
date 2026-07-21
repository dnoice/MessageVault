/*
 * ✒ Metadata
 *     - Title: Home Screen (Message Vault Edition - v1.0)
 *     - File Name: HomeScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/HomeScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.1.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header closed with the box-drawing rule reserved for XML (where "--" is illegal inside a comment). This is Kotlin: restored the standard nine-hyphen close. Comment only, no behaviour change.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: adopt the shared UiKit tokens and card/label/pill/button primitives so Home stops carrying private twins; the history read is now guarded and gets a real error state; loading is a labelled state and the three states cross-fade instead of snapping; icons and tappable cards carry accessible labels.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Show the last run's relative age with a gentle staleness nudge on the hero.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Reload on resume so a fresh export shows immediately; make the last-run hero tappable to open History.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial Home dashboard: last-run hero card with stat pills + throughput, quick-action shortcuts, and a storage/at-a-glance card. Becomes the app's landing destination.
 *
 * ✒ Description:
 *     The Home destination — the app's landing screen. Instead of dropping the user
 *     straight onto the Export form, Home greets them with the state of their vault:
 *     the last archive's headline numbers (messages, attachments, throughput, date
 *     span), one-tap shortcuts into Export / Browse / History, and where exports
 *     currently land. Reads only per-run metrics.json headlines via RunHistory, so it
 *     stays cheap regardless of corpus size and never loads a single message.
 *
 * ✒ Key Features:
 *     - Last-run hero: reads the newest RunSummary and shows message/attachment stat pills plus a throughput line; falls back to a welcome CTA when no complete run exists.
 *     - Quick actions: primary "New export" plus Browse / History shortcuts that route through the shared navigation callback.
 *     - Storage card: surfaces the live export location and archive count, with a jump to Settings.
 *     - Background load: RunHistory.list() runs on Dispatchers.IO, wrapped in runCatching; a labelled spinner covers the read and a failure lands in MvErrorState with a Retry.
 *     - Cross-faded states: loading / error / content swap through a Crossfade so a resume-time refresh never flickers the layout.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose Material3; com.digispace.messagevault.storage.RunHistory / RunSummary; com.digispace.messagevault.ui.Dest; kotlinx.coroutines.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.digispace.messagevault.storage.RunHistory
import com.digispace.messagevault.storage.RunSummary
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** What Home is showing right now — one value so the three states can cross-fade. */
private sealed interface HomeState {
    data object Loading : HomeState
    data class Failed(val message: String) : HomeState
    data class Loaded(val runs: List<RunSummary>) : HomeState
}

@Composable
fun HomeScreen(
    locationLabel: String,
    onGo: (Dest) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<HomeState>(HomeState.Loading) }

    // The export folder can be gone, unreadable, or half-written by an interrupted run.
    // Home is a read-only dashboard: it reports that instead of throwing into the UI.
    fun reload() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { RunHistory.list(context) } }
            state = result.fold(
                onSuccess = { HomeState.Loaded(it) },
                onFailure = { HomeState.Failed(it.message ?: "The export folder could not be read.") }
            )
        }
    }
    // Reload every time Home resumes, so a fresh export shows without an app restart.
    OnScreenResume { reload() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.ScreenV),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
    ) {
        // Cross-fade on the PHASE, not on the state object: a resume-time reload produces
        // a brand-new Loaded instance every time, and keying on that would re-run the
        // whole fade on each return to Home.
        val phase = when (state) {
            is HomeState.Loading -> "loading"
            is HomeState.Failed -> "error"
            is HomeState.Loaded -> "content"
        }
        Crossfade(targetState = phase, animationSpec = tween(260), label = "home") { p ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
            ) {
                when (p) {
                    "loading" -> MvLoadingState("Reading your archives…")
                    "error" -> MvErrorState(
                        title = "Couldn't read your archives",
                        message = (state as? HomeState.Failed)?.message.orEmpty()
                    ) {
                        MvPrimaryButton("Try again", onClick = { reload() })
                    }
                    else -> {
                        val runs = (state as? HomeState.Loaded)?.runs.orEmpty()
                        HeroCard(
                            last = runs.firstOrNull { it.complete },
                            onExport = { onGo(Dest.EXPORT) },
                            onOpenHistory = { onGo(Dest.HISTORY) }
                        )
                        QuickActionsCard(onGo)
                        StorageCard(
                            locationLabel = locationLabel,
                            archiveCount = runs.size,
                            totalBytes = runs.sumOf { it.sizeBytes },
                            onSettings = { onGo(Dest.SETTINGS) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(last: RunSummary?, onExport: () -> Unit, onOpenHistory: () -> Unit) {
    if (last == null) {
        // The genuine empty state: no archive has ever completed on this device.
        MvEmptyState(
            title = "Welcome to Message Vault",
            message = "No archives yet. Run your first export to capture every SMS & MMS, " +
                "with attachments, into portable files you own."
        ) {
            MvPrimaryButton("Run your first export", icon = Icons.Outlined.Backup, onClick = onExport)
        }
        return
    }
    val now = System.currentTimeMillis()
    val hasAge = last.startedAtMillis > 0
    val ageDays = if (hasAge) (now - last.startedAtMillis) / 86_400_000 else 0
    MvCard(onClick = onOpenHistory, contentDescription = "Open export history") {
        MvSectionLabel("LAST EXPORT")
        Text(
            last.startedLabel,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        if (hasAge) {
            Text(
                Format.relativeAge(last.startedAtMillis, now),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            MvStatPill("MESSAGES", "%,d".format(Locale.US, last.totalMessages), Modifier.weight(1f))
            MvStatPill("ATTACHMENTS", "%,d".format(Locale.US, last.attachmentCount), Modifier.weight(1f))
        }
        val wall = if (last.wallHuman.isNotEmpty()) " · ${last.wallHuman}" else ""
        Text(
            "SMS ${last.smsCount} · MMS ${last.mmsCount}$wall",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (last.throughput > 0) {
            Text(
                String.format(Locale.US, "%,.0f msgs/sec", last.throughput),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        if (last.dateMin != null && last.dateMax != null) {
            Text(
                "${last.dateMin} → ${last.dateMax}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (hasAge && ageDays >= 14) {
            Text(
                "It's been ${ageDays / 7} weeks — consider a fresh backup.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Text(
            "Tap to view history →",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun QuickActionsCard(onGo: (Dest) -> Unit) {
    MvCard {
        MvSectionLabel("QUICK ACTIONS")
        MvPrimaryButton("New export", icon = Icons.Outlined.Backup, onClick = { onGo(Dest.EXPORT) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            MvSecondaryButton("Browse", Modifier.weight(1f), icon = Icons.Outlined.Forum) { onGo(Dest.BROWSE) }
            MvSecondaryButton("History", Modifier.weight(1f), icon = Icons.Outlined.History) { onGo(Dest.HISTORY) }
        }
    }
}

/**
 * Whole-card tap goes to Settings. The old "Storage settings" button promised
 * storage controls and only changed tabs — the label now says what it does.
 */
@Composable
private fun StorageCard(
    locationLabel: String,
    archiveCount: Int,
    totalBytes: Long,
    onSettings: () -> Unit
) {
    MvCard(onClick = onSettings, contentDescription = "Open settings") {
        MvSectionLabel("STORAGE")
        Text(
            "$archiveCount archive${if (archiveCount == 1) "" else "s"} · ${Format.bytes(totalBytes)}",
            style = MaterialTheme.typography.titleMedium
        )
        Text("Exports save to", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary)
        Text(
            locationLabel,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
        Text(
            "Manage in Settings →",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
