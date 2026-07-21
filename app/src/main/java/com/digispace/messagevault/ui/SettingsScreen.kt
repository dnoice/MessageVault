/*
 * ✒ Metadata
 *     - Title: Settings Screen (Message Vault Edition - v1.0)
 *     - File Name: SettingsScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/SettingsScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.2.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Controls that can't produce a result are now disabled with a reason instead of accepting a tap and doing nothing: the app-lock switch is dead (not merely unchecked) with no screen lock enrolled, and "Clear cached exports" reports how much is actually cached and greys out at zero. The Default-export card warns when the saved defaults can't produce a run — that combination silently disables Run over on the Export screen with no clue as to why. "Clear cached exports" also confirms before deleting, since a share bundle a sync app has not yet uploaded is not recoverable from the cache.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: card geometry, padding and section labels come from the shared UiKit; the storage-usage read is guarded and now has labelled loading plus an error line; toggle rows are one merged 48dp control for TalkBack; the "no screen lock" advisory drops crimson (it is guidance, not a failure) and the About wordmark scales instead of sitting at a fixed 200dp.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — "Clear cached zips" swept only .zip, so with encryption on it reported nothing to clear while hundreds of MB of sealed .mvault bundles sat in the cache forever. Now clears both and is labelled "Clear cached exports".
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — The Vault and Notifications cards cached the exact system state their own buttons exist to change: a newly enrolled PIN stayed invisible and notifications still read "Disabled" until the process restarted. Both are now state refreshed in OnScreenResume.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Storage-usage card: archives / total size / total messages on device (reloaded on resume) plus a copy-path action; Appearance stays the canonical theme control.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial Settings screen.
 *
 * ✒ Description:
 *     The Settings destination: storage-access status and grant, storage usage,
 *     appearance (theme mode), and an About section. One place for the app-wide
 *     knobs that don't belong on the per-run Export screen.
 *
 * ✒ Key Features:
 *     - Storage access card: shows full vs. limited access and offers the grant button.
 *     - Storage usage card: archive count, total bytes, and total messages on device, with a copy-path action and a measured, confirmed cache-clear that disables itself when there is nothing to free.
 *     - Appearance card: segmented theme-mode selector reporting changes upward — the canonical theme control.
 *     - About card: app name, resolved package version, author, and signature.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose Material3; com.digispace.messagevault.ui.theme.ThemeMode. Theme + access state are hoisted to MainActivity; this screen just renders and reports changes.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.digispace.messagevault.R
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.core.app.NotificationManagerCompat
import android.content.Intent
import android.provider.Settings as AndroidSettings
import com.digispace.messagevault.export.ArchiveConfig
import com.digispace.messagevault.security.VaultCrypto
import com.digispace.messagevault.security.VaultSettings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.digispace.messagevault.storage.ExportLocation
import com.digispace.messagevault.storage.RunHistory
import com.digispace.messagevault.storage.StorageStats
import com.digispace.messagevault.ui.theme.ThemeMode
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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

    fun cachedExports(): List<java.io.File> =
        context.cacheDir.listFiles()
            ?.filter {
                it.isFile && (it.name.endsWith(".zip") ||
                    it.name.endsWith(".${VaultCrypto.EXTENSION}"))
            }
            .orEmpty()

    // Both of these describe system state that this screen's own buttons send the user off
    // to change. Cached in a keyless remember they never caught up: enrol a PIN and the
    // lock switch stayed refused, turn notifications on and the card still read "Disabled",
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
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.ScreenV),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Section)
    ) {
        // A headline only while there is something to do about it. Granted access is a
        // settled fact, not news, so it becomes a quiet line inside Storage usage below
        // rather than the first thing the screen says.
        if (!hasAllFilesAccess) {
            SettingsCard("Storage access") {
                Text(
                    "Limited — exports save to app-private storage, where the Files app " +
                        "and cloud sync can't reach them.",
                    style = MaterialTheme.typography.bodyMedium
                )
                MvPrimaryButton("Grant full access", onClick = onRequestAllFilesAccess)
            }
        }

        SettingsCard("Storage usage") {
            val s = stats
            val err = statsError
            if (err != null) {
                Text(
                    err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (s == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)
                ) {
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Measuring…", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                Text(
                    "${s.archives} archive${if (s.archives == 1) "" else "s"} · ${Format.bytes(s.totalBytes)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${"%,d".format(Locale.US, s.totalMessages)} messages archived",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Where they land, and whether we can reach the browsable location — the
            // demoted remains of the old "Storage access" headline.
            Text(
                locationLabel,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            Text(
                if (hasAllFilesAccess) "Full access granted"
                else "Limited access — app-private storage",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            // Share the width evenly so the longer label wraps inside its own button
            // instead of pushing the pair off a narrow screen.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MvSpace.Inline)
            ) {
                MvSecondaryButton(
                    text = "Copy path",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val path = ExportLocation.baseDir(context).absolutePath
                        clipboard.setText(AnnotatedString(path))
                        Toast.makeText(context, "Folder path copied", Toast.LENGTH_SHORT).show()
                    }
                )
                // Share/Copy leave a bundle of the whole run in the cache — with
                // attachments that can be hundreds of MB. Let the user reclaim it, but
                // only when there is something to reclaim: a button that always fires
                // and usually reports "nothing to clear" trains the user to distrust it.
                MvSecondaryButton(
                    text = if (cachedBytes > 0) "Clear ${Format.bytes(cachedBytes)} cache"
                        else "Cache is empty",
                    modifier = Modifier.weight(1f),
                    enabled = cachedBytes > 0,
                    onClick = { confirmClearCache = true }
                )
            }
            if (cachedBytes == 0L) {
                Text(
                    "Nothing is cached — sharing or copying an export leaves a bundle here " +
                        "that you can reclaim later.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        SettingsCard("Vault") {
            Text(
                "This archive is every message you've ever sent. The vault decides who can open it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            // Disabled, not merely unchecked: with no enrolled credential the switch had
            // nothing to enable, yet it still accepted the tap and stored lockEnabled=true
            // while continuing to render as off — a control quietly disagreeing with itself.
            SettingToggle(
                label = "Lock app (biometric / PIN)",
                checked = vault.lockEnabled && canLock,
                enabled = canLock
            ) { v -> onVaultChange(vault.copy(lockEnabled = v)) }
            if (!canLock) {
                // Guidance, not a failure — crimson stays reserved for genuine errors.
                Text(
                    "No biometric or screen lock is set up on this device — set one in system settings to enable the lock.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            SettingToggle("Screen privacy", vault.secureScreen) { v ->
                onVaultChange(vault.copy(secureScreen = v))
            }
            Text(
                "Blocks screenshots, screen recording, and the recents preview.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            SettingToggle("Encrypt shared exports", vault.encryptExports) { v ->
                onVaultChange(vault.copy(encryptExports = v))
            }
            Text(
                "Share and Copy produce a passphrase-locked .mvault instead of a plain .zip. " +
                    "The passphrase is asked for each time and never stored — if you lose it, " +
                    "that archive is gone. Files already on this device stay readable.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        SettingsCard("Default export") {
            Text(
                "What a new run archives by default. The Export screen starts from these.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            SettingToggle("SMS", config.includeSms) { v -> onConfigChange { it.copy(includeSms = v) } }
            SettingToggle("MMS & attachments", config.includeMms) { v -> onConfigChange { it.copy(includeMms = v) } }
            SettingToggle("JSONL", config.jsonl) { v -> onConfigChange { it.copy(jsonl = v) } }
            SettingToggle("SQLite (needed to Browse)", config.sqlite) { v -> onConfigChange { it.copy(sqlite = v) } }
            SettingToggle("Markdown transcripts", config.markdown) { v -> onConfigChange { it.copy(markdown = v) } }
            SettingToggle("Extract attachments", config.extractAttachments) { v -> onConfigChange { it.copy(extractAttachments = v) } }
            // These same switches are what the Export screen's Run button is guarded on.
            // Left in an unrunnable combination here, Run simply arrives disabled over
            // there and the reason is two screens away.
            val runnable = (config.includeSms || config.includeMms) &&
                (config.jsonl || config.sqlite || config.markdown)
            if (!runnable) {
                Text(
                    "These defaults can't produce an export — pick at least one source " +
                        "(SMS / MMS) and one output format, or Run stays disabled on the " +
                        "Export screen.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        SettingsCard("Notifications") {
            Text(
                if (notifsEnabled) "Enabled — you'll see progress while a run is zipped or exported."
                else "Disabled — long jobs will run silently with no status to check.",
                style = MaterialTheme.typography.bodyMedium
            )
            MvSecondaryButton(
                text = "Notification settings",
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    }
                }
            )
        }

        SettingsCard("Appearance") {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            val modes = ThemeMode.entries
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().heightIn(min = MvTouchTarget)) {
                modes.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = mode == themeMode,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(i, modes.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            activeContentColor = MaterialTheme.colorScheme.primary,
                            activeBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            inactiveBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                }
            }
        }

        SettingsCard("About") {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Cap rather than fix the size: on a narrow phone at a large system font
                // scale a hard 200dp square used to crowd the card.
                Image(
                    painter = painterResource(R.drawable.mv_wordmark),
                    contentDescription = "Message Vault logo",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
            Text("digiSpace · personal SMS/MMS archival", style = MaterialTheme.typography.bodyMedium)
            Text("Version $version", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Dennis 'dendogg' Smaltz", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.tertiary)
        }
    }

    // Deleting the cache is irreversible, and a bundle a sync app has queued but not yet
    // uploaded disappears with it — so it gets a confirmation like any other destruction.
    if (confirmClearCache) {
        AlertDialog(
            onDismissRequest = { confirmClearCache = false },
            title = { Text("Clear cached exports?") },
            text = {
                Text(
                    "${Format.bytes(cachedBytes)} of packaged share/copy bundles will be " +
                        "deleted. Your archives themselves are not touched — but anything " +
                        "still being uploaded by another app will be lost and has to be " +
                        "shared again."
                )
            },
            confirmButton = {
                TextButton(onClick = {
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
                        Toast.makeText(
                            context,
                            if (freed > 0) "Freed ${Format.bytes(freed)} of cached exports"
                            else "Nothing could be cleared — the files are in use.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearCache = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Compact label + switch row. The whole row is one merged toggleable control, so it
 * clears the 48dp touch target and a screen reader hears a single named switch.
 */
@Composable
private fun SettingToggle(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
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
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        )
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
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    MvCard {
        MvSectionLabel(title)
        content()
    }
}
