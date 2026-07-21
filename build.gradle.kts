/*
 * ✒ Metadata
 *     - Title: Root Build Script (Message Vault Edition - v1.0)
 *     - File Name: build.gradle.kts
 *     - Relative Path: build.gradle.kts
 *     - Artifact Type: config
 *     - Version: 1.0.0
 *     - Date: 2026-06-22
 *     - Update: Monday, June 22, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The top-level Gradle build script for the multi-module project. Its main job
 *     is to declare which plugins exist for the whole build without applying them,
 *     so each module can opt in to the ones it needs. Use it as the shared build
 *     setup and single point of plugin declaration for the root project.
 *
 * ✒ Key Features:
 *     - Plugin declaration: the plugins { } block makes the Android and Kotlin plugins available across the build.
 *     - Version-catalog aliases: alias(libs.plugins.xxx) pulls plugin id + version from libs.versions.toml for one source of truth.
 *     - apply false pattern: plugins are made available to modules but not applied to the root project, which has no code to build.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Gradle version catalog (libs.versions.toml); Android Application, Kotlin Android, and Kotlin Compose plugins.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
// Top-level build file — plugins declared here, applied in :app
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
