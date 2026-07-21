<!--
✒ Metadata
    - Title: Upgrading to AGP 9.x (Message Vault Edition - v2.0)
    - File Name: UPGRADING.md
    - Relative Path: UPGRADING.md
    - Artifact Type: docs
    - Version: 2.0.1
    - Date: 2026-07-20
    - Update: Monday, July 20, 2026
    - Author: Dennis 'dendogg' Smaltz
    - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
    - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!

✒ Changelog:
    - 2.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header completion: the
      always-required Key Features and Other Important Information sections were
      missing, leaving the block ending at Description.
    - 2.0.0 (2026-06-22) [Anthropic - Claude Opus 4.8 (1M context)] — Correct the
      shipped baseline to AGP 8.13.2 / Gradle 8.13; refresh the June-2026 frontier;
      add the navigation-compose alignment note.
    - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial upgrade guide.

✒ Description:
    The version baseline and the path from the conservative AGP 8.x island to the
    current AGP 9.x frontier. Read this before any toolchain bump — AGP 9 is a real
    break, not a routine version bump.

✒ Key Features:
    - The pinned baseline: the exact AGP, Gradle, Kotlin, and JVM versions the
      project ships on, and why the conservative island was chosen.
    - The frontier snapshot: what the current AGP 9.x line offers and what it
      costs, so the trade is a decision rather than a reflex.
    - The breaking-change inventory: what AGP 9 actually breaks, in the order it
      will bite this project.
    - A staged upgrade path with a verification step after each stage, plus the
      navigation-compose alignment note.
    - Rollback guidance: how to get back to the known-good baseline if a bump
      goes wrong.

✒ Other Important Information:
    - Dependencies: assumes the shipped toolchain baseline — AGP 8.13.2, Gradle
      8.13, JVM 21, Android Studio's bundled JBR as JAVA_HOME. No tooling is
      needed to read this file; the upgrade itself needs the Gradle wrapper.
    - Compatible platforms: the document itself renders anywhere Markdown does;
      the toolchain it describes targets Android builds (minSdk 29).
---------
-->

# Upgrading to AGP 9.x (the Panda-native path)

This project ships pinned to a **deliberately conservative, known-good baseline**:

| Component          | Baseline (ships with project) |
| ------------------ | ----------------------------- |
| AGP                | 8.13.2                        |
| Gradle             | 8.13                          |
| Kotlin             | 2.1.0                         |
| Compose BOM        | 2024.12.01                    |
| navigation-compose | 2.8.5                         |
| JDK                | 21 (your runtime)             |
| compileSdk / targetSdk | 35                        |
| minSdk             | 29                            |

This compiles cleanly and Android Studio (Panda / 2025.3.x) opens it without
complaint — AGP 8.13.2 is well within Studio's rolling support window, and AGP
8.13 requires Gradle 8.13, which is what the wrapper pins. On first sync Panda may
*offer* to upgrade AGP; you don't have to accept it for the app to build and run.

> Note: the build command uses the bundled JBR. `java` is not on the shell PATH —
> set `JAVA_HOME` to `C:\Program Files\Android\Android Studio\jbr` before
> `.\gradlew.bat`.

## The current frontier (June 2026)

Verified against the live web, not from memory:

| Component          | Current stable |
| ------------------ | -------------- |
| AGP                | 9.2.0          |
| Gradle             | 9.6.0          |
| Kotlin             | 2.4.0          |
| Compose BOM        | 2026.05.00     |

**Decision on record:** stay on the AGP 8.x island for the enhancement work; take
the AGP 9.x jump later as its own isolated, verified change — never tangled with
feature work.

## What changes when you move to AGP 9.x

AGP 9 is a real break, not a bump. The headline changes:

1. **Built-in Kotlin.** AGP 9.0+ compiles Kotlin out of the box. **Remove** the
   `org.jetbrains.kotlin.android` plugin from `app/build.gradle.kts` and from the
   version catalog — applying it is no longer correct.

2. **Gradle 9 is mandatory.** AGP 8.x → 9.x forces Gradle 8.x → 9.x. Bump
   `gradle/wrapper/gradle-wrapper.properties` to a Gradle 9.x distribution
   (AGP 9.2 requires Gradle 9.4.1+; 9.6.0 is current stable).

3. **New DSL only.** AGP 9 exposes the new DSL interfaces exclusively and removes
   the old deprecated variant API. `kotlinOptions { jvmTarget = "21" }` moves to
   the Kotlin `compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }` form.

4. **navigation-compose alignment.** navigation-compose 2.8.x targets Compose 1.7
   (the current BOM). If you bump Compose to 1.8+/BOM 2026.x in the same pass,
   move navigation-compose to its matching 2.9.x line.

5. **Short-term escape hatch.** You can temporarily keep legacy behavior with
   `android.builtInKotlin=false` (and `android.enableLegacyVariantApi=true`) in
   `gradle.properties`. Both opt-outs disappear in AGP 10.0 — a bridge, not a
   destination.

## The easy button

From Panda: let the **AGP Upgrade Assistant** drive, or point the AGP 9 upgrade
skill at the repo (this app is not KMP). Either handles the plugin removal, the
Gradle bump, and most of the DSL rewrites mechanically. Review the diff before
committing.

## Compose note

If you bump Compose to BOM 2026.05.00 / compose 1.10.x for the newest tooling, do
it in the same pass as the Kotlin bump so the Compose compiler version stays
aligned with Kotlin — and move navigation-compose to 2.9.x at the same time.

︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
