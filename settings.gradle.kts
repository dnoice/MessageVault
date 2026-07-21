/*
 * ✒ Metadata
 *     - Title: Settings Gradle (Message Vault Edition - v1.0)
 *     - File Name: settings.gradle.kts
 *     - Relative Path: settings.gradle.kts
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
 *     The first file Gradle reads; it defines the build as a whole — the project
 *     name, which modules exist, and where Gradle is allowed to download plugins
 *     and libraries from. Think of it as the build's table of contents plus its
 *     supplier list, read before any build.gradle.kts.
 *
 * ✒ Key Features:
 *     - pluginManagement { repositories }: where Gradle looks for plugins (e.g. the Android plugin); google() and mavenCentral() are the two big public sources.
 *     - dependencyResolutionManagement { repositories }: where library dependencies are fetched; FAIL_ON_PROJECT_REPOS centralizes sources so modules can't quietly add their own.
 *     - include(":app"): registers the single app module; a larger project might include(":core", ":data", ...).
 *     - Kotlin DSL (.kts): build scripts written in Kotlin so the IDE can autocomplete and type-check them.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Gradle settings DSL; repositories google(), mavenCentral(), gradlePluginPortal().
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MessageVault"
include(":app")
