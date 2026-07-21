/*
 * ✒ Metadata
 *     - Title: Screen Lifecycle Helpers (Message Vault Edition - v1.0)
 *     - File Name: Lifecycle.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/Lifecycle.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial OnScreenResume helper: run an action every time a destination resumes, so read-only screens refresh after a new export.
 *
 * ✒ Description:
 *     A tiny Compose helper for the read-only destinations (Home, History, Browse).
 *     Each loads its data from disk, and because the nav graph saves/restores state,
 *     a plain one-shot load can go stale after a new export completes on another
 *     screen. OnScreenResume re-runs the loader on every ON_RESUME of the current
 *     destination (its NavBackStackEntry lifecycle), so returning to a screen always
 *     reflects the latest run.
 *
 * ✒ Key Features:
 *     - Observes the current LifecycleOwner (the NavBackStackEntry inside a NavHost) for ON_RESUME.
 *     - rememberUpdatedState keeps the latest callback without re-subscribing.
 *     - DisposableEffect removes the observer cleanly when the screen leaves composition.
 *
 * ✒ Other Important Information:
 *     - Dependencies: androidx.lifecycle (Lifecycle, LifecycleEventObserver) + lifecycle-compose (LocalLifecycleOwner); Jetpack Compose runtime.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ─────────
 */
package com.digispace.messagevault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Runs [onResume] once now and again every time this destination resumes. */
@Composable
fun OnScreenResume(onResume: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val current by rememberUpdatedState(onResume)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) current()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
