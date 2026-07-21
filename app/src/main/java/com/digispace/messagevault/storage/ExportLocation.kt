/*
 * ✒ Metadata
 *     - Title: Export Location (Message Vault Edition - v1.0)
 *     - File Name: ExportLocation.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/storage/ExportLocation.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-06-22
 *     - Update: Monday, June 22, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial: public-vs-sandbox base dir + grant intent.
 *
 * ✒ Description:
 *     Decides where an export is written and whether a browsable, OneDrive-syncable
 *     location is reachable at all. The old app-private home under Android/data is
 *     hidden by Android 11+ scoped storage, so this helper routes exports to public
 *     storage when "All files access" is granted and falls back to the sandbox
 *     otherwise, letting a personal archival tool actually hand the data back.
 *
 * ✒ Key Features:
 *     - baseDir(): returns /sdcard/MessageVault/exports when granted, else the app-private sandbox.
 *     - hasAllFilesAccess() / isSandboxed(): let the UI decide whether to nudge for the grant.
 *     - manageAccessIntent(): opens the system All-files-access grant screen for this app.
 *     - locationLabel(): human-readable label for where exports currently land.
 *     - Granted path uses ordinary public storage: visible in Files, syncable, adb-pullable.
 *     - MANAGE_EXTERNAL_STORAGE chosen over SAF: a single ~900 MB nested tree is slow through SAF.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Context, Intent, Uri, Build, Environment, Settings; java.io.File.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.io.File

object ExportLocation {

    /** The public folder name on shared storage when access is granted. */
    private const val PUBLIC_DIR = "MessageVault/exports"

    /**
     * True when we may write freely to shared storage ("All files access").
     * Only meaningful on API 30+; on 29 it is always false and we use the
     * app-private fallback.
     */
    fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

    /**
     * Parent folder for run directories. Public + browsable when granted, else
     * the app-private sandbox. Always returns an existing directory.
     */
    fun baseDir(context: Context): File {
        val dir = if (hasAllFilesAccess())
            File(Environment.getExternalStorageDirectory(), PUBLIC_DIR)
        else
            context.getExternalFilesDir("exports") ?: File(context.filesDir, "exports")
        dir.mkdirs()
        return dir
    }

    /** True while exports still land in the hard-to-reach app sandbox. */
    fun isSandboxed(): Boolean = !hasAllFilesAccess()

    /** A human label for where exports currently go, for the UI. */
    fun locationLabel(context: Context): String =
        if (hasAllFilesAccess()) "/sdcard/MessageVault/exports"
        else "app storage (Android/data — hard to reach)"

    /** Opens the system "All files access" screen for this app. */
    fun manageAccessIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
}
