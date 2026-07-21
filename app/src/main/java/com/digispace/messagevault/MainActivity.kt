/*
 * ✒ Metadata
 *     - Title: Main Activity (Message Vault Edition - v1.0)
 *     - File Name: MainActivity.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/MainActivity.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Delivery failures now explain themselves: a failed copy reports the actual cause (out of space, revoked folder grant, provider refusal) and names the next step instead of a bare "Copy failed."; share failures do the same and no longer report only in the shade; and Share/Copy on a run whose path has gone missing says so rather than silently doing nothing.
 *     - 1.0.6 (2026-07-20) [Anthropic - Claude Opus 4.8] — Source is plain text again: the passphrase scrub in prepareBundle held a literal U+0000 control character inside its char literal, which made grep, ripgrep, and every diff tool classify this file as binary and silently skip it during reviews and header sweeps. Replaced with the explicit Unicode escape form of the same code point; the scrub behaviour is byte-identical.
 *     - 1.0.5 (2026-07-20) [Anthropic - Claude Opus 4.8] — Three delivery/lock fixes: the passphrase dialog moved inside VaultGate (as a sibling it rendered over the locked face and could seal an export while the vault was locked, and it is now cleared when the door closes); Share no longer drops the chooser when the user leaves during zipping — a background activity start is blocked on Android 10+, so an unresumed finish parks the chooser in a tappable notification instead of cancelling the ongoing entry and going silent; the pending copy destination is rememberSaveable so a recreation in the SAF picker no longer makes the copy vanish without a word.
 *     - 1.0.4 (2026-07-20) [Anthropic - Claude Opus 4.8] — Report long delivery jobs: an ongoing notification while a run is zipped for Share/Copy, a final result entry, and a one-time POST_NOTIFICATIONS request on API 33+. Zip failures are caught instead of vanishing.
 *     - 1.0.3 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Host the AppRoot drawer shell instead of a lone screen; add persisted theme mode + path-based share/copy for History.
 *     - 1.0.2 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Done-screen delivery: Share a run as .zip via FileProvider, and Copy it into a SAF-picked folder.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Broker All-files access: track grant state and launch the system access screen, re-checking on return.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The app's single screen host and launcher entry point. It sets up the
 *     Compose UI, connects it to the ArchiveViewModel's state, and brokers the
 *     runtime SMS/contacts permission request, All-files access, and run delivery
 *     (share/copy). Declared as the LAUNCHER activity in the manifest.
 *
 * ✒ Key Features:
 *     - Compose host: setContent { } bridges the Activity into the AppRoot drawer shell, themed by MessageVaultTheme.
 *     - Lifecycle-aware state: collectAsStateWithLifecycle() subscribes the UI to the ViewModel only while the screen is visible.
 *     - Runtime permissions: RequestMultiplePermissions shows the system dialog for READ_SMS / READ_CONTACTS.
 *     - All-files access broker: launches the system Settings screen and re-checks the grant on return.
 *     - Run delivery: zips a finished run for share via FileProvider, or copies it into a SAF-picked folder tree.
 *     - Persisted theme mode: SYSTEM/LIGHT/DARK stored in SharedPreferences and applied on launch.
 *
 * ✒ Other Important Information:
 *     - Dependencies: AndroidX Activity/Compose/Lifecycle, ContextCompat, FileProvider; com.digispace.messagevault ui.AppRoot, ui.ArchiveViewModel, ui.theme.*, storage.ExportLocation, storage.RunDelivery; kotlinx.coroutines.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digispace.messagevault.storage.ExportLocation
import com.digispace.messagevault.storage.RunDelivery
import androidx.fragment.app.FragmentActivity
import com.digispace.messagevault.security.VaultCrypto
import com.digispace.messagevault.security.VaultPrefs
import com.digispace.messagevault.ui.AppRoot
import com.digispace.messagevault.ui.ArchiveViewModel
import com.digispace.messagevault.ui.PassphraseDialog
import com.digispace.messagevault.ui.VaultGate
import com.digispace.messagevault.ui.theme.MessageVaultTheme
import com.digispace.messagevault.ui.theme.ThemeMode
import com.digispace.messagevault.util.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * FragmentActivity (not ComponentActivity) because BiometricPrompt requires a
 * FragmentActivity host for the vault's unlock prompt.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ArchiveViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            var hasSms by remember { mutableStateOf(hasSmsPermission()) }
            var hasAllFiles by remember { mutableStateOf(ExportLocation.hasAllFilesAccess()) }
            var themeMode by remember { mutableStateOf(loadThemeMode()) }
            var vault by remember { mutableStateOf(VaultPrefs.load(this@MainActivity)) }

            // FLAG_SECURE blocks screenshots, screen recording, and recents thumbnails.
            LaunchedEffect(vault.secureScreen) { applySecureFlag(vault.secureScreen) }

            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                hasSms = result[Manifest.permission.READ_SMS] == true || hasSmsPermission()
            }

            // "All files access" is granted on a system Settings screen, not via a
            // runtime dialog; re-check the state when the user navigates back.
            val allFilesLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { hasAllFiles = ExportLocation.hasAllFilesAccess() }

            // Ask once for notification posting (API 33+). Denial is harmless — the
            // delivery jobs still run, they just lose their status entry in the shade.
            val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // When the vault seals exports we need a passphrase first; the pending
            // delivery is held as a continuation and resumed once it's entered.
            var passphraseRequest by remember { mutableStateOf<((CharArray) -> Unit)?>(null) }
            fun deliver(action: (CharArray?) -> Unit) {
                if (vault.encryptExports) passphraseRequest = { pass -> action(pass) }
                else action(null)
            }

            /**
             * Resolves a run directory for delivery, or explains why it can't. Share and
             * Copy used to be plain no-ops when the path was null or the folder had been
             * deleted from under them (removed on another screen, or the export folder
             * moved when All-files access changed) — a tap that does nothing at all reads
             * as a broken button.
             */
            fun runDirOrExplain(path: String?): File? {
                if (path.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        "No finished export to deliver — run one first.",
                        Toast.LENGTH_LONG
                    ).show()
                    return null
                }
                val dir = File(path)
                if (!dir.isDirectory) {
                    Toast.makeText(
                        this@MainActivity,
                        "That archive is no longer on this device. Open History to see what's left.",
                        Toast.LENGTH_LONG
                    ).show()
                    return null
                }
                return dir
            }

            // "Copy to folder": pick a destination tree, then zip + write into it.
            //
            // Saved as a path string, not a plain remembered File: DocumentsUI is a separate
            // process and this app's cache holds multi-hundred-MB zips, so the activity can
            // easily be recreated — or the process killed — while the picker is open. A lost
            // destination used to make the whole copy vanish with no feedback at all.
            var pendingDirPath by rememberSaveable { mutableStateOf<String?>(null) }
            val saveTreeLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocumentTree()
            ) { treeUri ->
                val dir = pendingDirPath?.let(::File)
                pendingDirPath = null
                if (treeUri != null && dir != null) {
                    deliver { pass -> copyRunIntoTree(dir, treeUri, pass) }
                } else if (treeUri != null) {
                    // Destination survived, source didn't: say so rather than doing nothing.
                    Toast.makeText(
                        this@MainActivity,
                        "Lost track of which export to copy — pick it again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MessageVaultTheme(darkTheme = dark) {
                // The passphrase dialog lives INSIDE the gate's content lambda. As a sibling
                // it composed over the locked face — fully interactive, competing with the
                // BiometricPrompt for the window — which let a user seal and deliver an
                // export while the vault was supposedly locked. onLocked also drops the
                // pending request, so nothing sensitive waits behind a closed door.
                VaultGate(
                    enabled = vault.lockEnabled,
                    onLocked = { passphraseRequest = null }
                ) {
                AppRoot(
                    state = state,
                    hasSmsPermission = hasSms,
                    hasAllFilesAccess = hasAllFiles,
                    locationLabel = ExportLocation.locationLabel(this@MainActivity),
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it; saveThemeMode(it) },
                    onRequestPermission = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.READ_CONTACTS
                            )
                        )
                    },
                    onRequestAllFilesAccess = {
                        allFilesLauncher.launch(ExportLocation.manageAccessIntent(this@MainActivity))
                    },
                    onShareCurrent = {
                        runDirOrExplain(state.resultPath)?.let { d -> deliver { pass -> shareRun(d, pass) } }
                    },
                    onCopyCurrent = {
                        runDirOrExplain(state.resultPath)?.let { d ->
                            pendingDirPath = d.absolutePath
                            saveTreeLauncher.launch(null)
                        }
                    },
                    onShareDir = { path ->
                        runDirOrExplain(path)?.let { d -> deliver { pass -> shareRun(d, pass) } }
                    },
                    onCopyDir = { path ->
                        runDirOrExplain(path)?.let { d ->
                            pendingDirPath = d.absolutePath
                            saveTreeLauncher.launch(null)
                        }
                    },
                    onConfigChange = { vm.updateConfig(it) },
                    onStart = vm::start,
                    onCancel = vm::cancel,
                    vault = vault,
                    onVaultChange = { updated ->
                        vault = updated
                        VaultPrefs.save(this@MainActivity, updated)
                    }
                )
                passphraseRequest?.let { resume ->
                    PassphraseDialog(
                        onDismiss = { passphraseRequest = null },
                        onConfirm = { pass ->
                            passphraseRequest = null
                            resume(pass)
                        }
                    )
                }
                }
            }
        }
    }

    /** Screen privacy: keeps message content out of screenshots and recents previews. */
    private fun applySecureFlag(secure: Boolean) {
        if (secure) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private fun loadThemeMode(): ThemeMode {
        val name = getSharedPreferences("mv_prefs", MODE_PRIVATE)
            .getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    private fun saveThemeMode(mode: ThemeMode) {
        getSharedPreferences("mv_prefs", MODE_PRIVATE).edit()
            .putString("theme_mode", mode.name).apply()
    }

    /**
     * Zip a finished run and hand it to any app via the system share sheet.
     *
     * Zipping hundreds of attachments is slow, and nothing appears on screen until the
     * chooser opens — so an ongoing notification carries the status, surviving the user
     * leaving the app to go looking for it.
     */
    private fun shareRun(dir: File, passphrase: CharArray?) {
        Toast.makeText(this, "Zipping ${dir.name}…", Toast.LENGTH_SHORT).show()
        Notifications.ongoing(
            this, Notifications.ID_DELIVERY,
            "Preparing export",
            if (passphrase != null) "Zipping and encrypting ${dir.name}…" else "Zipping ${dir.name}…"
        )
        lifecycleScope.launch {
            val bundle = runCatching {
                withContext(Dispatchers.IO) { prepareBundle(dir, passphrase) }
            }.getOrElse {
                val reason = deliveryFailureReason(it)
                Notifications.complete(
                    this@MainActivity, Notifications.ID_DELIVERY,
                    "Couldn't prepare export", reason
                )
                // The shade alone isn't enough when the user is still looking at the
                // screen waiting for a share sheet that is never going to arrive.
                Toast.makeText(this@MainActivity, "Couldn't prepare export. $reason", Toast.LENGTH_LONG).show()
                return@launch
            }
            val uri = FileProvider.getUriForFile(
                this@MainActivity, "$packageName.fileprovider", bundle
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = if (bundle.extension == VaultCrypto.EXTENSION)
                    "application/octet-stream" else "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, "Share export")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Zipping is exactly the moment a user wanders off, and Android 10+ blocks a
            // background activity start — the chooser would simply never appear, with the
            // ongoing notification already cancelled and a finished zip sitting unused.
            // Only launch it directly when we are actually resumed; otherwise leave the
            // chooser in the shade for them to tap. The ongoing entry is not cancelled
            // until it has been replaced, so the status never just evaporates.
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Notifications.cancel(this@MainActivity, Notifications.ID_DELIVERY)
                startActivity(chooser)
            } else {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val pending = PendingIntent.getActivity(
                    this@MainActivity, 0, chooser,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                Notifications.complete(
                    this@MainActivity, Notifications.ID_DELIVERY,
                    "Export ready to share",
                    "${dir.name} is packaged — tap to choose where to send it.",
                    pending
                )
            }
        }
    }

    /**
     * Zips the run and, when a passphrase is supplied, seals it into a .mvault envelope.
     * The plain zip is deleted afterwards so an unencrypted copy never lingers in cache,
     * and the passphrase is wiped as soon as the key is derived.
     */
    private fun prepareBundle(dir: File, passphrase: CharArray?): File {
        val zip = RunDelivery.zip(dir, cacheDir)
        if (passphrase == null) return zip
        val sealed = File(cacheDir, "${dir.name}.${VaultCrypto.EXTENSION}")
        try {
            VaultCrypto.encrypt(zip, sealed, passphrase)
        } finally {
            passphrase.fill('\u0000')
            zip.delete()
        }
        return sealed
    }

    /** Zip a finished run and write it into a user-picked folder (e.g. OneDrive). */
    private fun copyRunIntoTree(dir: File, treeUri: Uri, passphrase: CharArray?) {
        Toast.makeText(this, "Copying ${dir.name}…", Toast.LENGTH_SHORT).show()
        Notifications.ongoing(
            this, Notifications.ID_DELIVERY,
            "Copying export",
            if (passphrase != null) "Zipping and encrypting ${dir.name}…" else "Zipping ${dir.name}…"
        )
        lifecycleScope.launch {
            // A copy of a multi-hundred-MB archive fails for ordinary, fixable reasons —
            // the card filled up, the folder grant was revoked, the destination is
            // read-only. Collapsing all of them into "Copy failed." left the user with
            // nothing to act on, so carry the real reason through to the message.
            val reason: String? = runCatching {
                val bundle = withContext(Dispatchers.IO) { prepareBundle(dir, passphrase) }
                val created = withContext(Dispatchers.IO) {
                    RunDelivery.copyIntoTree(this@MainActivity, bundle, treeUri)
                }
                if (created) null
                else "The folder wouldn't accept a new file. Pick a different destination."
            }.getOrElse { deliveryFailureReason(it) }

            val ok = reason == null
            Notifications.complete(
                this@MainActivity, Notifications.ID_DELIVERY,
                if (ok) "Export copied" else "Copy failed",
                if (ok) "${dir.name} saved to the folder you picked."
                else "Couldn't write ${dir.name}. $reason"
            )
            Toast.makeText(
                this@MainActivity,
                if (ok) "Copied to folder." else "Copy failed. $reason",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Turns a delivery exception into something the user can act on. The stock message
     * on these is either empty or a raw path, so the common, fixable causes are named
     * explicitly and anything unrecognised falls back to the exception's own text.
     */
    private fun deliveryFailureReason(t: Throwable): String {
        val raw = t.message.orEmpty()
        val hay = "${t.javaClass.simpleName} $raw".lowercase()
        return when {
            "enospc" in hay || "no space" in hay ->
                "This device is out of storage. Free some space and try again."
            "security" in hay || "permission" in hay ->
                "Access to that folder was withdrawn. Pick the folder again."
            "eacces" in hay || "read-only" in hay || "erofs" in hay ->
                "That folder is read-only. Pick a different destination."
            raw.isNotBlank() -> raw
            else -> "The copy stopped partway (${t.javaClass.simpleName}). Try again."
        }
    }
}
