/*
 * ✒ Metadata
 *     - Title: Theme (Message Vault Edition - v1.0)
 *     - File Name: Theme.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/theme/Theme.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-06-22
 *     - Update: Monday, June 22, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Set tertiary accent on-palette (gold dark / slate light) so it isn't Material's default purple.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Defines the app's colors and wraps the UI in a Material theme, encoding the
 *     locked digiSpace palette (navy / gold / slate / parchment) and reserving
 *     crimson for genuine critical states (errors) ONLY — never decoration. Used
 *     in the ui/theme layer: MainActivity wraps the whole screen in
 *     MessageVaultTheme so any Composable can read MaterialTheme.colorScheme.*
 *     without hardcoding hex values.
 *
 * ✒ Key Features:
 *     - Locked palette: named Color vals (Navy/Gold/Slate/Parchment/Crimson) keep all hex in one place.
 *     - Material 3 ColorScheme mapping: palette mapped once onto primary/background/surface/error and matching on* roles.
 *     - Dual schemes: light and dark provided; isSystemInDarkTheme() picks per the phone's setting, same palette reassigned.
 *     - On-palette tertiary accent: gold in dark, slate in light, avoiding Material's default purple.
 *     - ThemeMode enum: SYSTEM / LIGHT / DARK preference surfaced in Settings.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (foundation, material3, runtime, ui.graphics); MessageVaultTypography.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy      = Color(0xFF1B2A4A)
val Gold      = Color(0xFFC9A84C)
val Slate     = Color(0xFF3D4F5F)
val Parchment = Color(0xFFF4EDD8)
val Crimson   = Color(0xFF9B2C2C)   // critical states ONLY

private val DarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = Slate,
    onSecondary = Parchment,
    tertiary = Gold,            // accent (group labels, brand dot)
    onTertiary = Navy,
    background = Navy,
    onBackground = Parchment,
    surface = Slate,
    onSurface = Parchment,
    error = Crimson,
    onError = Parchment
)

private val LightScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Parchment,
    secondary = Slate,
    onSecondary = Parchment,
    tertiary = Slate,           // readable accent on parchment (gold is too low-contrast here)
    onTertiary = Parchment,
    background = Parchment,
    onBackground = Navy,
    surface = Color(0xFFFBF7EC),
    onSurface = Navy,
    error = Crimson,
    onError = Parchment
)

/** User-selectable theme preference, surfaced in Settings. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun MessageVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = MessageVaultTypography,
        content = content
    )
}
