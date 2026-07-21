/*
 * ✒ Metadata
 *     - Title: Home Screen (Message Vault Edition - v1.0)
 *     - File Name: HomeScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/HomeScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.3.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.3.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Systemwide consistency pass. Home called the run's date range COVERAGE and set it a size smaller than the rows it shares a right edge with, while History called the identical field SPAN at the ordinary size — converged on SPAN. The storage roll-up said TOTAL SIZE where History said ON DISK and Settings said TOTAL BYTES, so all three now read ON DISK and print the raw byte count through MvBytes, as a measurement block should. Both hand-rolled MvPlate { Column(padding…) } stacks became MvFieldPlate and the LOCATION label + recessed plate became MvLocationPlate, so Home's ledger rows and path field land on the same margins as every other tab's. Style only.
 *     - 1.2.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, applying STYLE.md to the landing screen. Deleted both Forum speech-bubble glyphs (the largest graphic on the tab) and every other raw Icons.* lookup in favour of MvIcons. Replaced the two gradient MvStatPill lozenges with one ruled MvStatPlate. Led the hero with the accession slug and demoted the friendly date to a mono stamp field. Converted every middot-joined stat sentence into MvFieldRow manifests on plates, with the coverage span rendered as a `..` range. Dropped both "Tap to …→" instruction lines and the staleness nag, which becomes an AGE field plus a flagged STALE stamp beside the section label. Promoted the export path to a field of record on a recessed plate. Catalogue-numbered the sections, rewrote the copy in registry voice, purged colorScheme.tertiary, and shortened the phase crossfade to MvMotion.snap(). Storage now reconciles with the hero by printing complete/partial counts and the aggregate record total. Style only — no logic or behaviour changed.
 *     - 1.1.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header closed with the box-drawing rule reserved for XML (where "--" is illegal inside a comment). This is Kotlin: restored the standard nine-hyphen close. Comment only, no behaviour change.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: adopt the shared UiKit tokens and card/label/pill/button primitives so Home stops carrying private twins; the history read is now guarded and gets a real error state; loading is a labelled state and the three states cross-fade instead of snapping; icons and tappable cards carry accessible labels.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Show the last run's relative age with a gentle staleness nudge on the hero.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Reload on resume so a fresh export shows immediately; make the last-run hero tappable to open History.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial Home dashboard: last-run hero card with stat pills + throughput, quick-action shortcuts, and a storage/at-a-glance card. Becomes the app's landing destination.
 *
 * ✒ Description:
 *     The Home destination — the app's landing screen, and the vault's front plate. It
 *     states the condition of the holdings rather than greeting anyone: the latest
 *     accession by its catalogue slug, its measured figures as a ruled manifest, the
 *     operations available over the corpus, and the inventory roll-up with the on-disk
 *     location printed as a field of record. Reads only per-run metrics.json headlines
 *     via RunHistory, so it stays cheap regardless of corpus size and never loads a
 *     single message.
 *
 * ✒ Key Features:
 *     - Latest accession: leads with the yyyyMMdd_HHmmss slug, stamps the run's condition (COMPLETE / STALE), and prints started, age, SMS, MMS, elapsed, throughput and coverage as aligned field rows.
 *     - Ruled stat plate: MESSAGES and ATTACHMENTS share a baseline and a column edge on one flat hairline-divided plate — no gradient, no lozenge, no motif.
 *     - Operations: BEGIN RUN plus BROWSE / HISTORY, using only the archival glyph set; the messenger iconography is gone.
 *     - Inventory: archives held, partial runs, aggregate records and total size, so the roll-up reconciles with the hero instead of silently counting runs it never showed.
 *     - Location of record: the export path on a recessed plate at full ink, selectable, not a caption under a button.
 *     - Background load: RunHistory.list() runs on Dispatchers.IO, wrapped in runCatching; the read is covered by a labelled measuring state and a failure lands in MvErrorState with RETRY.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose Material3; com.digispace.messagevault.storage.RunHistory / RunSummary; com.digispace.messagevault.ui.UiKit primitives; com.digispace.messagevault.ui.Dest; kotlinx.coroutines.
 *     - Style authority: STYLE.md. This screen composes UiKit primitives only — no private twins, no raw dp, no raw alphas, no raw Icons.* lookups, and no colorScheme.tertiary.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/** Past this many days the latest accession carries a STALE flag. A flag, not a sentence. */
private const val STALE_AFTER_DAYS = 14L

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
        // Snap, not settle: this is a display refreshing, and it refreshes on every resume.
        Crossfade(targetState = phase, animationSpec = MvMotion.snap(), label = "home") { p ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
            ) {
                when (p) {
                    "loading" -> MvLoadingState("Reading index")
                    "error" -> MvErrorState(
                        title = "EXPORT FOLDER UNREADABLE",
                        message = (state as? HomeState.Failed)?.message.orEmpty()
                    ) {
                        MvPrimaryButton("RETRY", onClick = { reload() })
                    }
                    else -> {
                        val runs = (state as? HomeState.Loaded)?.runs.orEmpty()
                        AccessionCard(
                            last = runs.firstOrNull { it.complete },
                            onExport = { onGo(Dest.EXPORT) },
                            onOpenHistory = { onGo(Dest.HISTORY) }
                        )
                        OperationsCard(onGo)
                        InventoryCard(
                            locationLabel = locationLabel,
                            runs = runs,
                            onSettings = { onGo(Dest.SETTINGS) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * The latest accession, led by its catalogue slug. The friendly date is metadata, so it
 * is demoted to a mono STARTED field rather than set as a headline; staleness is an AGE
 * figure plus a flagged stamp, never a sentence asking for another backup.
 */
@Composable
private fun AccessionCard(last: RunSummary?, onExport: () -> Unit, onOpenHistory: () -> Unit) {
    if (last == null) {
        // The genuine empty state: no archive has ever completed on this device.
        MvEmptyState(
            title = "NO ACCESSIONS ON RECORD",
            message = "The export folder holds no completed archive. A run captures every " +
                "SMS and MMS, with attachments, into portable files held on this device."
        ) {
            MvPrimaryButton("BEGIN FIRST RUN", icon = MvIcons.Extract, onClick = onExport)
        }
        return
    }
    val now = System.currentTimeMillis()
    val hasAge = last.startedAtMillis > 0
    val ageDays = if (hasAge) (now - last.startedAtMillis) / 86_400_000 else 0
    val stale = hasAge && ageDays >= STALE_AFTER_DAYS

    MvCard(onClick = onOpenHistory, contentDescription = "Open export history") {
        MvSectionLabel(
            "LATEST ACCESSION",
            ordinal = 1,
            rule = true,
            trailing = {
                if (stale) MvStamp("STALE", tone = MvTone.Flagged) else MvStamp("COMPLETE")
            }
        )
        // The identity line: the yyyyMMdd_HHmmss directory slug the run is filed under.
        MvMono(
            last.name,
            style = MvType.MonoValue,
            modifier = Modifier.semantics { heading() }
        )

        MvStatPlate(
            cells = listOf(
                MvStatCell("MESSAGES", MvNum(last.totalMessages)),
                MvStatCell("ATTACHMENTS", MvNum(last.attachmentCount))
            ),
            accentIndex = 0
        )

        MvFieldPlate {
            MvFieldRow(
                "STARTED",
                if (hasAge) Format.timestamp(last.startedAtMillis) else last.startedLabel
            )
            if (hasAge) MvFieldRow("AGE", "${MvNum(ageDays)} d")
            MvFieldRow("SMS", MvNum(last.smsCount))
            MvFieldRow("MMS", MvNum(last.mmsCount))
            if (last.wallHuman.isNotEmpty()) MvFieldRow("ELAPSED", last.wallHuman)
            if (last.throughput > 0) {
                MvFieldRow(
                    "THROUGHPUT",
                    String.format(Locale.US, "%,.0f msg/s", last.throughput)
                )
            }
            // SPAN, at the ordinary value size, because History calls the identical field
            // SPAN and sets it at the ordinary value size. This card called it COVERAGE and
            // shrank it to MonoSmall, which broke the right edge it shares with the rows
            // above it. ".." is range notation; "→" is a UI glyph and implies movement.
            if (last.dateMin != null && last.dateMax != null) {
                MvFieldRow("SPAN", "${last.dateMin} .. ${last.dateMax}", rule = false)
            } else {
                MvFieldRow("SPAN", "—", rule = false)
            }
        }
    }
}

/**
 * The operations available over the corpus. Deliberately three marks and no more, all
 * from the approved glyph set — the speech-bubble that used to sit on "Browse" is the
 * exact signal this app must not give off.
 */
@Composable
private fun OperationsCard(onGo: (Dest) -> Unit) {
    MvCard {
        MvSectionLabel("OPERATIONS", ordinal = 2, rule = true)
        MvPrimaryButton("BEGIN RUN", icon = MvIcons.Extract, onClick = { onGo(Dest.EXPORT) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)) {
            MvSecondaryButton("BROWSE", Modifier.weight(1f), icon = MvIcons.Records) { onGo(Dest.BROWSE) }
            MvSecondaryButton("HISTORY", Modifier.weight(1f), icon = MvIcons.Index) { onGo(Dest.HISTORY) }
        }
    }
}

/**
 * The vault inventory. It prints complete and partial counts separately so the roll-up
 * reconciles with the accession plate above — the previous card weighed interrupted runs
 * into a total the reader had no way to account for. Whole-card tap goes to Settings; the
 * click label carries that, so the card needs no instruction line and ends on data.
 */
@Composable
private fun InventoryCard(
    locationLabel: String,
    runs: List<RunSummary>,
    onSettings: () -> Unit
) {
    val complete = runs.count { it.complete }
    val partial = runs.size - complete
    MvCard(onClick = onSettings, contentDescription = "Open settings") {
        MvSectionLabel("STORAGE", ordinal = 3, rule = true)
        // ARCHIVES / RECORDS / ON DISK, in that vocabulary, because History's inventory
        // and Settings' storage block report the same three aggregates and had been
        // calling them RECORDS/MESSAGES and ON DISK/TOTAL SIZE/TOTAL BYTES between them.
        // MvBytes prints the raw count too, as a measurement block should.
        MvFieldPlate {
            MvFieldRow("ARCHIVES", MvNum(complete))
            MvFieldRow("PARTIAL", MvNum(partial))
            MvFieldRow("RECORDS", MvNum(runs.sumOf { it.totalMessages }))
            MvFieldRow("ON DISK", MvBytes(runs.sumOf { it.sizeBytes }), rule = false)
        }
        MvLocationPlate(locationLabel)
    }
}
