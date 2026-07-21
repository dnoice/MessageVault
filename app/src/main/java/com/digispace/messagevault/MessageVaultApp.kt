/*
 * ✒ Metadata
 *     - Title: Application Object (Message Vault Edition - v1.0)
 *     - File Name: MessageVaultApp.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/MessageVaultApp.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Register the notification channel at process start, before any job can try to post progress.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The single Application instance for the whole running process, registered in AndroidManifest.xml via android:name=".MessageVaultApp" and the canonical home for any app-lifetime setup (one-time initialization, dependency wiring, logging) that must run before the first Activity.
 *
 * ✒ Key Features:
 *     - Creates the notification channel once at startup; a channel must exist before any notification referencing it is posted.
 * ---------
 */
package com.digispace.messagevault

import android.app.Application
import com.digispace.messagevault.util.Notifications

class MessageVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannels(this)
    }
}
