/*
 * ✒ Metadata
 *     - Title: Vault Preferences (Message Vault Edition - v1.0)
 *     - File Name: VaultPrefs.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/security/VaultPrefs.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial vault settings: app lock, screen privacy, and encrypt-on-export.
 *
 * ✒ Description:
 *     The three vault switches, persisted in the same SharedPreferences file the rest of
 *     the app uses. Deliberately holds only booleans — NO passphrase is ever stored here
 *     or anywhere else on the device; it is asked for at the moment of encryption, used,
 *     and cleared. A stored passphrase would reduce the vault to a locked door with the
 *     key taped to it.
 *
 * ✒ Key Features:
 *     - lockEnabled: require biometric / device credential before the app's contents are shown.
 *     - secureScreen: sets FLAG_SECURE, blocking screenshots, screen recording, and recents previews.
 *     - encryptExports: deliver runs as passphrase-encrypted .mvault instead of plain .zip.
 *     - Secrets-free by construction: the type system here cannot hold a passphrase.
 *
 * ✒ Other Important Information:
 *     - Dependencies: android.content.Context / SharedPreferences ("mv_prefs", shared with theme + export config).
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.security

import android.content.Context

data class VaultSettings(
    val lockEnabled: Boolean = false,
    val secureScreen: Boolean = false,
    val encryptExports: Boolean = false
)

object VaultPrefs {

    private const val FILE = "mv_prefs"
    private const val KEY_LOCK = "vault_lock"
    private const val KEY_SECURE = "vault_secure_screen"
    private const val KEY_ENCRYPT = "vault_encrypt_exports"

    fun load(context: Context): VaultSettings {
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return VaultSettings(
            lockEnabled = p.getBoolean(KEY_LOCK, false),
            secureScreen = p.getBoolean(KEY_SECURE, false),
            encryptExports = p.getBoolean(KEY_ENCRYPT, false)
        )
    }

    fun save(context: Context, settings: VaultSettings) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOCK, settings.lockEnabled)
            .putBoolean(KEY_SECURE, settings.secureScreen)
            .putBoolean(KEY_ENCRYPT, settings.encryptExports)
            .apply()
    }
}
