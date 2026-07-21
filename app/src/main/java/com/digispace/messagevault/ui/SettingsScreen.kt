/*
 * ✒ Metadata
 *     - Title: Settings Screen (Message Vault Edition - v2.1)
 *     - File Name: SettingsScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/SettingsScreen.kt
 *     - Artifact Type: library
 *     - Version: 2.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 2.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Systemwide consistency pass. Three private helpers this file declared were things other tabs had also invented, so they move to UiKit and this screen consumes them: SettingsPlate becomes MvFieldPlate, byteField becomes MvBytes, clockStamp and ACK_HOLD_MS become MvClock and MvAckHoldMs. The storage block reported the same three aggregates as Home and History under different names — MESSAGES and TOTAL BYTES are now RECORDS and ON DISK, the vocabulary all three share. A measurement failure was a bare crimson sentence, a fourth treatment of a condition the other tabs mark with a rule; it is now MvFailureNote. The notification permission is stamped rather than printed as a value, so every condition in the app rides the right edge as a stamp. The path field becomes MvLocationPlate, the full-access button and its stamp are named exactly as Export names the identical operation and state. Style only.
 *     - 2.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, applying STYLE.md. The nine Material Switches are gone: every toggle is now an MvStateToggle stating ENABLED/DISABLED (REFUSED where no credential is enrolled) or INCLUDED/OMITTED on the export manifest, on ruled plates instead of loose stacks. Sections are catalogue-numbered (01 STORAGE … 06 COLOPHON). Storage usage is a measurement block of right-aligned monospace field rows carrying both the humanised and the raw byte count, the export path is promoted to a selectable recessed plate, and the spinner becomes a hairline MvMeasuring sweep. All three Toasts become timestamped in-card MvInlineAck lines; the cache dialog becomes an MvConfirmDialog field manifest and the cache button stops renaming itself into a status sentence. The segmented theme control becomes an MvModeStrip; both advisories become MvNote; About becomes a colophon of aligned fields with the wordmark de-rounded and Share demoted to a footer text action. Every ad-hoc alpha and every colorScheme.tertiary reference is replaced by the MvInk scale.
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Controls that can't produce a result are now disabled with a reason instead of accepting a tap and doing nothing: the app-lock switch is dead (not merely unchecked) with no screen lock enrolled, and "Clear cached exports" reports how much is actually cached and greys out at zero. The Default-export card warns when the saved defaults can't produce a run — that combination silently disables Run over on the Export screen with no clue as to why. "Clear cached exports" also confirms before deleting, since a share bundle a sync app has not yet uploaded is not recoverable from the cache.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: card geometry, padding and section labels come from the shared UiKit; the storage-usage read is guarded and now has labelled loading plus an error line; toggle rows are one merged 48dp control for TalkBack; the "no screen lock" advisory drops crimson (it is guidance, not a failure) and the About wordmark scales instead of sitting at a fixed 200dp.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — "Clear cached zips" swept only .zip, so with encryption on it reported nothing to clear while hundreds of MB of sealed .mvault bundles sat in the cache forever. Now clears both and is labelled "Clear cached exports".
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — The Vault and Notifications cards cached the exact system state their own buttons exist to change: a newly enrolled PIN stayed invisible and notifications still read "Disabled" until the process restarted. Both are now state refreshed in OnScreenResume.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Storage-usage card: archives / total size / total messages on device (reloaded on resume) plus a copy-path action; Appearance stays the canonical theme control.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial Settings screen.
 *
 * ✒ Description:
 *     The Settings destination, rendered as a control panel rather than a settings menu:
 *     six catalogue-numbered sections carrying storage measurement, vault condition, the
 *     default export manifest, notification state, appearance, and the colophon. One place
 *     for the app-wide knobs that don't belong on the per-run Export screen.
 *
 * ✒ Key Features:
 *     - 01 STORAGE: a measurement block of monospace field rows (archives, messages, total bytes with the raw count, cached, access), the export path on a selectable recessed plate, and a footer strip carrying copy and a fixed-label cache clear.
 *     - 02 VAULT: lock / screen privacy / encryption as state rows on a ruled plate, stating ENABLED, DISABLED or REFUSED rather than offering a switch.
 *     - 03 DEFAULT EXPORT: a manifest — each row names the artefact it writes (messages.jsonl, archive.db, media/) and reads INCLUDED or OMITTED.
 *     - 04 NOTIFICATIONS / 05 APPEARANCE: notification condition as a field; theme as a hairline mode strip with a solid active cell.
 *     - 06 COLOPHON: package, version, author and standard as aligned fields; share demoted to a footer action that names its object.
 *     - Confirmations are timestamped in-card acknowledgement lines, never Toasts; the one destructive confirm is a field manifest.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose Material3; ui/UiKit.kt for every token and primitive; com.digispace.messagevault.ui.theme.ThemeMode. Theme + access state are hoisted to MainActivity; this screen just renders and reports changes.
 *     - Style: implements STYLE.md. No colorScheme.tertiary, no elevation, no Toast, no Switch, no ad-hoc alpha — hierarchy comes from MvInk, geometry from MvShape.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.digispace.messagevault.R
import com.digispace.messagevault.export.ArchiveConfig
import com.digispace.messagevault.security.VaultCrypto
import com.digispace.messagevault.security.VaultSettings
import com.digispace.messagevault.storage.ExportLocation
import com.digispace.messagevault.storage.RunHistory
import com.digispace.messagevault.storage.StorageStats
import com.digispace.messagevault.ui.theme.ThemeMode
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// The hold interval and the clock stamp that used to be declared here are now MvAckHoldMs
// and MvClock() in UiKit. History had grown a byte-identical copy of the second one and
// Browse a third that printed the whole date, so an acknowledgement did not read the same
// on any two tabs.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    hasAllFilesAccess: Boolean,
    locationLabel: String,
    onRequestAllFilesAccess: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    config: ArchiveConfig,
    onConfigChange: ((ArchiveConfig) -> ArchiveConfig) -> Unit,
    vault: VaultSettings,
    onVaultChange: (VaultSettings) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }
    var stats by remember { mutableStateOf<StorageStats?>(null) }
    var statsError by remember { mutableStateOf<String?>(null) }
    // Measured, not guessed: the clear-cache button must be able to say how much it
    // would free and switch itself off when the answer is nothing.
    var cachedBytes by remember { mutableStateOf(0L) }
    var confirmClearCache by remember { mutableStateOf(false) }
    // Resolved off the main thread with the rest of the measurement — baseDir() touches
    // the filesystem, and the path is now shown, not just copied.
    var exportPath by remember { mutableStateOf<String?>(null) }

    // The Toast replacement. A vault records rather than flashes, so a confirmation is a
    // stamped line on the card that produced it. Two of them, because the storage card and
    // the colophon are far apart and an acknowledgement belongs beside its own control.
    var storageAck by remember { mutableStateOf<String?>(null) }
    var colophonAck by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(storageAck) {
        if (storageAck != null) { delay(MvAckHoldMs); storageAck = null }
    }
    LaunchedEffect(colophonAck) {
        if (colophonAck != null) { delay(MvAckHoldMs); colophonAck = null }
    }

    fun cachedExports(): List<java.io.File> =
        context.cacheDir.listFiles()
            ?.filter {
                it.isFile && (it.name.endsWith(".zip") ||
                    it.name.endsWith(".${VaultCrypto.EXTENSION}"))
            }
            .orEmpty()

    // Both of these describe system state that this screen's own buttons send the user off
    // to change. Cached in a keyless remember they never caught up: enrol a PIN and the
    // lock row stayed REFUSED, turn notifications on and the card still read DISABLED,
    // until the process restarted. Re-read them on every resume instead.
    var canLock by remember { mutableStateOf(canAuthenticate(context)) }
    var notifsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    OnScreenResume {
        canLock = canAuthenticate(context)
        notifsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        scope.launch {
            // Measuring walks the export folder; a missing or unreadable folder must
            // report itself here rather than throwing out of a settings screen.
            val result = withContext(Dispatchers.IO) { runCatching { RunHistory.aggregate(context) } }
            result.onSuccess { stats = it; statsError = null }
                .onFailure { statsError = it.message ?: "The export folder could not be measured." }
            cachedBytes = withContext(Dispatchers.IO) {
                runCatching { cachedExports().sumOf { f -> f.length() } }.getOrDefault(0L)
            }
            exportPath = withContext(Dispatchers.IO) {
                runCatching { ExportLocation.baseDir(context).absolutePath }.getOrNull()
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.ScreenV),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
    ) {
        // A headline only while there is something to do about it, and deliberately
        // unnumbered so the six standing sections keep stable ordinals whether or not
        // this card is present. Granted access is a settled fact, not news — it becomes
        // the ACCESS field inside 01 STORAGE instead.
        if (!hasAllFilesAccess) {
            // RESTRICTED, the word Export stamps on the identical condition. Two words for
            // one state is how the same fact ends up reading as two different facts.
            SettingsCard("STORAGE ACCESS", trailing = { MvStamp("RESTRICTED", tone = MvTone.Flagged) }) {
                MvNote(
                    "Exports land in $locationLabel, where the Files app and cloud sync " +
                        "cannot reach them."
                )
                MvPrimaryButton("GRANT FULL ACCESS", onClick = onRequestAllFilesAccess)
            }
        }

        SettingsCard("STORAGE", ordinal = 1) {
            val s = stats
            val err = statsError
            when {
                // A marked failure, not a crimson sentence. Home and History report an
                // unreadable export folder through MvErrorState and Export through the
                // same 2dp error rule; a bare tinted paragraph was a fourth treatment
                // for the identical condition, and the one with no structure to it.
                err != null -> MvFailureNote(err, label = "NOT MEASURED")
                // A hairline sweep reads as an instrument taking a reading; a rotating
                // ring reads as an app loading.
                s == null -> MvMeasuring("Measuring")
                else -> MvFieldPlate {
                    MvFieldRow("ARCHIVES", MvNum(s.archives))
                    MvFieldRow("RECORDS", MvNum(s.totalMessages))
                    MvFieldRow("ON DISK", MvBytes(s.totalBytes))
                    MvFieldRow("CACHED", MvBytes(cachedBytes))
                    MvFieldRow(
                        "ACCESS",
                        if (hasAllFilesAccess) "FULL" else "APP-PRIVATE",
                        rule = false
                    )
                }
            }

            // The storage locator is the most archival fact on the screen. Engraved
            // plate, full ink, selectable — verifiable rather than taken on trust.
            MvLocationPlate(exportPath ?: locationLabel)

            if (cachedBytes == 0L) {
                MvNote(
                    "Nothing is cached. Sharing or copying an export leaves a bundle here " +
                        "that can be reclaimed later."
                )
            }

            MvInlineAck(storageAck)

            // Copy and a fixed-label clear. The measurement lives in the CACHED field
            // above; a control that renames itself into a status sentence is app-speak.
            MvCardFooter {
                MvTextAction("Copy path", onClick = {
                    val path = exportPath ?: ExportLocation.baseDir(context).absolutePath
                    clipboard.setText(AnnotatedString(path))
                    storageAck = "PATH COPIED · ${MvClock()}"
                })
                // Share/Copy leave a bundle of the whole run in the cache — with
                // attachments that can be hundreds of MB. Let the user reclaim it, but
                // only when there is something to reclaim.
                MvTextAction(
                    "Clear cache",
                    enabled = cachedBytes > 0,
                    destructive = true,
                    onClick = { confirmClearCache = true }
                )
            }
        }

        SettingsCard("VAULT", ordinal = 2) {
            Text(
                "The vault decides who can open this archive.",
                style = MaterialTheme.typography.bodySmall,
                color = MvInk.Body
            )
            MvFieldPlate {
                // Disabled, not merely unchecked: with no enrolled credential the control
                // had nothing to enable, yet it still accepted the tap and stored
                // lockEnabled=true while continuing to render as off. REFUSED states the
                // condition instead of leaving a dead control to be interpreted.
                MvStateToggle(
                    label = "Lock app (biometric / PIN)",
                    checked = vault.lockEnabled && canLock,
                    onCheckedChange = { v -> onVaultChange(vault.copy(lockEnabled = v)) },
                    enabled = canLock,
                    unavailableText = "REFUSED"
                )
                MvRule()
                MvStateToggle(
                    label = "Screen privacy",
                    checked = vault.secureScreen,
                    onCheckedChange = { v -> onVaultChange(vault.copy(secureScreen = v)) }
                )
                RowNote("Blocks screenshots, screen recording, and the recents preview.")
                MvRule()
                MvStateToggle(
                    label = "Encrypt shared exports",
                    checked = vault.encryptExports,
                    onCheckedChange = { v -> onVaultChange(vault.copy(encryptExports = v)) }
                )
                RowNote(
                    "Share and Copy produce a passphrase-locked .mvault instead of a plain " +
                        ".zip. The passphrase is asked for each time and never stored — if it " +
                        "is lost, that archive is gone. Files already on this device stay readable."
                )
            }
            if (!canLock) {
                // One advisory treatment, gold left rule. Never a colour: in the light
                // scheme tertiary equals secondary equals the section-label grey, so a
                // tinted warning was indistinguishable from a heading.
                MvNote(
                    "No biometric or screen lock is enrolled on this device. Set one in " +
                        "system settings to enable the lock."
                )
            }
        }

        SettingsCard("DEFAULT EXPORT", ordinal = 3) {
            Text(
                "What a new run archives by default. The Export screen starts from these.",
                style = MaterialTheme.typography.bodySmall,
                color = MvInk.Body
            )
            MvFieldPlate {
                // A manifest, not an options list: every row names the artefact it writes.
                ManifestRow("SMS", null, config.includeSms) { v ->
                    onConfigChange { it.copy(includeSms = v) }
                }
                MvRule()
                ManifestRow("MMS & attachments", null, config.includeMms) { v ->
                    onConfigChange { it.copy(includeMms = v) }
                }
                MvRule()
                ManifestRow("JSONL", "messages.jsonl", config.jsonl) { v ->
                    onConfigChange { it.copy(jsonl = v) }
                }
                MvRule()
                ManifestRow("SQLite", "archive.db", config.sqlite) { v ->
                    onConfigChange { it.copy(sqlite = v) }
                }
                RowNote("Required by Browse.")
                MvRule()
                ManifestRow("Markdown transcripts", "transcripts/*.md", config.markdown) { v ->
                    onConfigChange { it.copy(markdown = v) }
                }
                MvRule()
                ManifestRow("Extract attachments", "media/", config.extractAttachments) { v ->
                    onConfigChange { it.copy(extractAttachments = v) }
                }
            }
            // These same rows are what the Export screen's Run button is guarded on. Left
            // in an unrunnable combination here, Run simply arrives disabled over there
            // and the reason is two screens away.
            val runnable = (config.includeSms || config.includeMms) &&
                (config.jsonl || config.sqlite || config.markdown)
            if (!runnable) {
                MvNote(
                    "These defaults cannot produce an export. Include at least one source " +
                        "(SMS / MMS) and one output format, or Run stays disabled on the " +
                        "Export screen."
                )
            }
        }

        SettingsCard("NOTIFICATIONS", ordinal = 4) {
            MvFieldPlate {
                // Stamped, not printed as a value. Every other condition in the app —
                // COMPLETE, PARTIAL, INDEXED, NO INDEX, RUNNING — rides the right edge as
                // an MvStamp, and the ENABLED/DISABLED words in this card's own toggle
                // rows are state cells, so a plain string here was a third rendering of
                // "the condition of a thing".
                MvFieldRow(
                    "SYSTEM PERMISSION",
                    "",
                    rule = false,
                    trailing = {
                        MvStamp(
                            if (notifsEnabled) "ENABLED" else "DISABLED",
                            tone = if (notifsEnabled) MvTone.Neutral else MvTone.Flagged
                        )
                    }
                )
            }
            Text(
                if (notifsEnabled) "Run progress is posted while a run is zipped or exported."
                else "Long jobs run silently, with no status to check.",
                style = MaterialTheme.typography.bodySmall,
                color = MvInk.Body
            )
            MvCardFooter {
                MvTextAction("System notification settings", onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    }
                })
            }
        }

        SettingsCard("APPEARANCE", ordinal = 5) {
            // A hard-edged strip with one solid cell reads as a selector switch on an
            // instrument; Material's pill-capped row with a 16% wash reads as a photo app,
            // and that wash behaves differently in each scheme.
            val modes = ThemeMode.entries
            MvSectionLabel("THEME")
            MvModeStrip(
                options = modes.map { it.name },
                selectedIndex = modes.indexOf(themeMode).coerceAtLeast(0),
                onSelect = { i -> onThemeModeChange(modes[i]) }
            )
        }

        SettingsCard("COLOPHON", ordinal = 6) {
            // Square-ish corners: a rounded-photo clip on a wordmark is a social-app tell.
            Image(
                painter = painterResource(R.drawable.mv_wordmark),
                contentDescription = "Message Vault logo",
                modifier = Modifier
                    .fillMaxWidth(0.44f)
                    .sizeIn(maxWidth = 140.dp, maxHeight = 140.dp)
                    .clip(MvShape.Mark)
            )
            MvFieldPlate {
                MvFieldRow("PACKAGE", context.packageName)
                MvFieldRow("VERSION", version)
                MvFieldRow("AUTHOR", "Dennis 'dendogg' Smaltz")
                MvFieldRow("STANDARD", "digiSpace", rule = false)
            }
            MvMono(
                "︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!",
                style = MvType.MonoSmall,
                color = MvInk.Faint
            )
            MvInlineAck(colophonAck)
            // Demoted from a full-width button and named for its object. A prominent
            // full-width Share is the messenger affordance this app must never suggest,
            // even when what it shares is a link to the project.
            MvCardFooter {
                MvTextAction("Share a link to this app", onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Message Vault")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Message Vault — a personal SMS/MMS archiver for Android. " +
                                "Exports every message you own to JSONL, SQLite and Markdown, " +
                                "with attachments, entirely on-device.\n\n" +
                                "https://github.com/dnoice/MessageVault"
                        )
                    }
                    runCatching { context.startActivity(Intent.createChooser(send, "Share Message Vault")) }
                        .onFailure { colophonAck = "NO TARGET · ${MvClock()}" }
                })
            }
        }
    }

    // Deleting the cache is irreversible, and a bundle a sync app has queued but not yet
    // uploaded disappears with it — so it gets a confirmation like any other destruction.
    // The amount is a field of the manifest rather than a clause in a sentence: an
    // itemised statement of what is about to be destroyed reads as a receipt.
    if (confirmClearCache) {
        MvConfirmDialog(
            title = "CLEAR CACHED EXPORTS",
            fields = listOf(
                "CACHED" to MvBytes(cachedBytes),
                "TARGET" to "CACHE ONLY"
            ),
            consequence = "Archives themselves are not touched. Anything still being " +
                "uploaded by another app is lost and has to be shared again.",
            confirmLabel = "CLEAR",
            onDismiss = { confirmClearCache = false },
            onConfirm = {
                confirmClearCache = false
                scope.launch {
                    val freed = withContext(Dispatchers.IO) {
                        runCatching {
                            cachedExports().sumOf { f ->
                                val n = f.length(); if (f.delete()) n else 0L
                            }
                        }.getOrDefault(0L)
                    }
                    cachedBytes = withContext(Dispatchers.IO) {
                        runCatching { cachedExports().sumOf { f -> f.length() } }.getOrDefault(0L)
                    }
                    storageAck = if (freed > 0) {
                        "FREED ${Format.bytes(freed)} · ${MvClock()}"
                    } else {
                        "NOTHING FREED · FILES IN USE · ${MvClock()}"
                    }
                }
            }
        )
    }
}

// The private byteField() this file used to declare is now MvBytes() in UiKit, so Home's
// and History's aggregate roll-ups can report a byte figure the same way this one does.

/**
 * One row of the default-export manifest. [artefact] is the file the row actually writes,
 * which is a filename and therefore monospace — a specification of deliverables rather
 * than a list of feature checkboxes.
 */
@Composable
private fun ManifestRow(
    label: String,
    artefact: String?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    MvStateToggle(
        label = label,
        checked = checked,
        onCheckedChange = onToggle,
        supporting = artefact,
        onText = "INCLUDED",
        offText = "OMITTED"
    )
}

/**
 * The explanatory line that belongs to the row above it — inside the plate, under the
 * control it qualifies, and deliberately not ruled off from it. Prose, so it is sans.
 */
@Composable
private fun RowNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MvInk.Body,
        modifier = Modifier.padding(bottom = MvSpace.Row)
    )
}

// SettingsPlate is gone: it was this screen's name for what Home, History and Export had
// each also written out by hand, at three different insets. MvFieldPlate carries the one
// inset, so every ruled row in the app lands on the same two margins.

/**
 * A catalogue-numbered section. Numbering is what separates a register from a settings
 * menu, and it gives every section a stable name to refer to.
 */
@Composable
private fun SettingsCard(
    title: String,
    ordinal: Int? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    MvCard {
        MvSectionLabel(title, ordinal = ordinal, rule = true, trailing = trailing)
        content()
    }
}
