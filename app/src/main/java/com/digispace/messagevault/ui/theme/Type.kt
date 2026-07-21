/*
 * ✒ Metadata
 *     - Title: Typography (Message Vault Edition - v1.0)
 *     - File Name: Type.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/theme/Type.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-06-22
 *     - Update: Monday, June 22, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Fuller type scale (headlineLarge/Small, titleLarge, bodySmall, labelMedium) for the visual overhaul.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Defines the app's text styles in one Typography object so headings, body,
 *     and mono labels stay consistent everywhere. Lives in the ui/theme layer and
 *     is passed into MaterialTheme alongside the color scheme. Composables
 *     reference styles by role (e.g. MaterialTheme.typography.headlineMedium)
 *     rather than setting font size/weight ad hoc.
 *
 * ✒ Key Features:
 *     - Named Material 3 roles (headlineMedium, titleMedium, bodyMedium, labelSmall, ...): define once, reuse by name.
 *     - TextStyle bundles font family, weight, and size into one value per role.
 *     - Monospace for the status line and slug-like labels so counts and paths line up; SansSerif for everything else.
 *     - Sizes use .sp so text respects the user's system font-size accessibility setting.
 *     - Drop-in system fonts with a documented swap point for a bundled Biome Light asset (full brand fidelity).
 *
 * ✒ Other Important Information:
 *     - Dependencies: androidx.compose.material3.Typography; androidx.compose.ui.text (TextStyle, FontFamily, FontWeight); androidx.compose.ui.unit.sp.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System families keep the project drop-in. Swap to your Biome Light asset if desired.
val MessageVaultTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp
    )
)
