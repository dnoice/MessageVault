<!--
✒ Metadata
    - Title: Message Vault — Claude Code Reorientation Handoff
    - File Name: HANDOFF_CLAUDE_CODE.md
    - Relative Path: HANDOFF_CLAUDE_CODE.md
    - Artifact Type: docs
    - Version: 1.0.1
    - Date: 2026-07-20
    - Update: Monday, July 20, 2026
    - Author: Dennis 'dendogg' Smaltz
    - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
    - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!

✒ Changelog:
    - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header correction: the
      Artifact Type was a non-canonical "handoff brief" and the block ended at
      the Signature, with no Description, Key Features, Other Important
      Information, or closing rule.
    - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8] — Initial handoff brief.

✒ Description:
    The reorientation brief handed to a fresh Claude Code session running in the
    terminal inside Android Studio. It carries over everything the previous
    session knew that the filesystem does not say out loud: who the operator is,
    how he works, the house standards, and the invariants the codebase depends
    on. Read it end to end before touching any file in this project.

✒ Key Features:
    - Operator context: who Dennis is, his background, and how to pitch
      explanations of the Android-native toolchain.
    - House standards handover: the ✒ Metadata docstring block, the byte-exact
      signature, the palette, and the comment-syntax rules per file type.
    - Architecture invariants: flat memory, the single streaming export pass,
      and the seams that must not be violated.
    - Build and device rules: how to compile, and which adb and device
      operations are off limits.
    - Working agreement: verification over assumption, surgical edits over
      rewrites, and read-before-edit.

✒ Other Important Information:
    - Dependencies: none — this is reader context only, with no tooling or
      runtime requirements. It assumes the shipped toolchain baseline (AGP
      8.13.2, Gradle 8.13, JVM 21, minSdk 29, compileSdk/targetSdk 35).
    - Compatible platforms: renders anywhere Markdown does; the project it
      describes is developed on Windows and targets Android.
---------
-->

# Message Vault — Claude Code Reorientation Handoff

You are Claude Code, running in a terminal inside Android Studio on Dennis's
Windows machine ("AURA"). You are picking up a project that a previous Claude
instance (Opus 4.8, in the web app) built with Dennis across a long session. You
do **not** share that session's memory or filesystem — this brief is your
reorientation. Read it fully before touching anything.

## Who you are working with

Dennis 'dendogg' Smaltz — U.S. Army veteran, solo operator of digiSpace Research
Studio (Los Angeles). Self-taught developer; entry point was Termux on Android,
so he is sharp and hands-on but **new to the Android Studio / Kotlin / Gradle /
Jetpack Compose ecosystem specifically**. Treat the Android-native tooling as
something to explain as you go, not assume. He values: verification over
assumption, clear non-confusing output, and the digiSpace house standards below.

Communication he expects:

- Status/output messages must be understood on the FIRST read — no mental
  gymnastics. Lead with the outcome, one fact per line when several, never let a
  number imply a problem that isn't one (explain gaps inline).

- Verify, don't assume. Prove round-trips. Don't claim success an operation only
  *reported*; confirm it.

- He will catch hand-waving. Be honest about what you do and don't know.

## digiSpace house standards (NON-NEGOTIABLE)

1. **Docstring header** on every deliverable/source file: the `✒ Metadata`
   block, with the full ordered field set —
   Title, File Name, Relative Path, Artifact Type, Version, Date, Update,
   Author, A.I. Acknowledgement, Signature — followed by `✒ Description`,
   `✒ Changelog`, `✒ Key Features`, `✒ Usage Instructions`,
   `✒ Other Important Information`, closed with a `---------` rule.
   The dedicated standard is `DOCSTRING_STANDARDS.md` (v2.1.0). NOTE: the
   existing MessageVault source files currently use a NON-STANDARD freeform
   banner (PURPOSE / WHERE THIS FITS / KEY CONCEPTS). That was a known slip.
   Part of your job is to migrate them to the canonical `✒ Metadata` block.

2. **Author** field: always `Dennis 'dendogg' Smaltz`.
3. **A.I. Acknowledgement**: the CURRENT model string = most recent substantive
   editor. If you (Claude Code) are the editor, put your actual model string.
   Do not blindly copy "Opus 4.8" — use what you actually are. One model of
   record per file; per-entry lineage goes in the Changelog tags.

4. **Signature**, rendered EXACTLY, never paraphrased or stripped of glyphs:
   `︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!`
   Scope: metadata/docstring headers and formal deliverables only — not chat.

5. **No `§` glyph** anywhere in digiSpace artifacts (it was eliminated corpus-wide).
6. Markdown deliverables are **markdownlint-clean** to the digiSpace ruleset
   (note: MD013 line-length is NOT enforced in that ruleset; MD040 fenced-code-
   language IS). A `markdown-lint` skill exists.

## TASK 1 — Become the versioning expert for THIS environment

Do not trust any version number from memory or from this doc as current. The
toolchain moves; "yesterday's AGP is not today's AGP." Establish ground truth by
INSPECTION, then verify against the live web.

Read these files in the project and record exact pinned versions:

- `gradle/libs.versions.toml`  (AGP, Kotlin, Compose BOM, coroutines, lifecycle)
- `gradle/wrapper/gradle-wrapper.properties`  (Gradle distribution version)
- `app/build.gradle.kts`  (compileSdk / minSdk / targetSdk, JDK level)
- `build.gradle.kts`, `settings.gradle.kts`

Then confirm what the environment actually has:

```bash
# the gradle wrapper is the source of truth for the build
./gradlew --version            # on Windows: .\gradlew.bat --version
java -version
# installed SDK platforms / build-tools:
#   Android Studio > SDK Manager, or sdkmanager --list_installed
```

Known starting baseline as of the build session (VERIFY, may be stale):

- AGP 8.7.3, Gradle 8.11.1, Kotlin 2.1.0, Compose BOM 2024.12.01
- compileSdk/targetSdk 35, minSdk 29, JDK 21
- Android Studio: "Panda 4" / 2025.3.4 Patch 1
- The frontier had moved to AGP 9.x (built-in Kotlin, requires Gradle 9.x, new
  DSL). See `UPGRADING.md`. AGP 9 is a real break — read that file before any
  upgrade. Decide WITH Dennis whether to take the AGP 9 jump as part of the
  enhancement work or stay on the known-good 8.x island.

Deliverable for Task 1: a short, plain-language summary to Dennis of exactly what
versions are pinned, what the environment has installed, where they diverge, and
the upgrade decision (8.x island vs AGP 9.x). Use the web to confirm current
stable versions — do not assert versions from training data.

## TASK 2 — Learn the codebase (moderate audit sweep)

This codebase is new to you. Read it in DEPENDENCY ORDER; there is a companion
`ARCHITECTURE.md` that is a guided tour — read it first, it will save you time.

Package map (`app/src/main/java/com/digispace/messagevault/`):

- `data/model/Models.kt` — the shared vocabulary: `Message`, `Attachment`,
  `Direction`, `Kind`, `ExportProgress`. Everything maps into these.

- `data/source/` — `SmsSource`, `MmsSource` (the hard one: 3 queries per MMS;
  handles the seconds-vs-millis date landmine and skips SMIL parts),
  `ContactResolver` (cached number→name).

- `export/` — `Exporter` (interface), `JsonlExporter`, `SqliteExporter` (raw
  SQLite, portable .db), `MarkdownExporter` (one file per thread),
  `AttachmentExtractor`, and `ArchiveEngine` (the conductor: one streaming pass,
  fans out to selected exporters, coroutine + Dispatchers.IO, cancellation-aware).

- `ui/` — `ArchiveViewModel` (StateFlow, unidirectional data flow),
  `ArchiveScreen` (Compose, deliberately "dumb"), `ui/theme/` (digiSpace palette:
  navy #1B2A4A, gold #C9A84C, slate #3D4F5F, parchment #F4EDD8, crimson #9B2C2C
  reserved for errors ONLY).

- `MainActivity.kt` (host + runtime permission flow), `MessageVaultApp.kt`.

Design invariants to preserve:

- **Flat memory**: sources stream via `forEach(consume)`; exporters write per
  message and accumulate nothing. The app handles a 50k+ corpus on-device. Do
  not introduce anything that loads the whole dataset into memory.

- Single streaming pass in `ArchiveEngine`; new outputs = new `Exporter` + one
  line in the engine list.

- Personal SIDELOAD build — `READ_SMS` is granted on-device, not via Play.

Audit sweep: read each file, note anything that is a bug, a rough edge, a
non-standard docstring, or a TODO. Produce a short findings list for Dennis
BEFORE writing new code. Confirmed real-hardware status: built and ran on the
Z-Fold 6 (SM-F956U), exported 14,508 real messages successfully.

## TASK 3 — Enhance the app

Direction from Dennis: "Robust and comprehensive personal tool." Look and feel
needs an upgrade; new features welcome. Because this is PERSONAL USE, not Play
Store, you have freedom most Android apps don't — use it. No restricted-
permission review to satisfy, no Play policy constraints, no data-safety
declarations. That is a real advantage; lean into it.

Candidate work (DISCUSS and prioritize WITH Dennis — do not just start building):

- **Metrics/instrumentation** (Dennis explicitly asked for this): wall-clock
  total + per-phase timing (SMS / MMS / attachments), throughput (msgs/sec),
  SMS-vs-MMS counts, attachment count + bytes, date range, per-exporter time.
  Write a `metrics.json` and expand `MANIFEST.md`; surface a throughput line on
  the done screen. The engine is the single choke-point — instrument there.

- **UI/UX upgrade**: the current screen is a permission gate + a checkbox card +
  a run/progress card. Room for: a proper results/summary screen, an export
  history list, richer progress (current phase + ETA), Material 3 polish,
  foldable-aware layout (it runs on a Z-Fold 6 — take advantage of the large
  inner screen).

- **New features to weigh**: in-app browsing/search of the archive (read the
  SQLite back); incremental/since-last-run export; date-range or contact
  filters; scheduled/background export (WorkManager); share/zip the run; a
  re-reader/search module; default-SMS-handler mode (the "legitimate" way to
  hold restricted permissions — big commitment, discuss before pursuing).

Rules of engagement for Task 3:

- Propose a prioritized plan to Dennis and get buy-in before large builds.
- Every new/edited source file gets the canonical `✒ Metadata` docstring.
- Preserve the flat-memory and single-pass invariants.
- After any change: build (`.\gradlew.bat assembleDebug`), and where possible
  verify on the Z-Fold 6. Don't claim it works because it compiled — run it.

- Keep the look in the digiSpace palette; crimson stays error-only.

## First moves when you start

1. Confirm where you are: print the working directory, confirm it is the
   MessageVault project root (contains `settings.gradle.kts`).

2. Do Task 1 (versioning ground truth) and Task 2 (read `ARCHITECTURE.md`, then
   the code in dependency order). Report findings to Dennis.

3. Bring Dennis a prioritized enhancement plan. Then build, smallest valuable
   slice first (metrics is a clean, self-contained starting point he already
   wants).

## Companion docs already in the repo

- `README.md` — what the app does + build/run.
- `ARCHITECTURE.md` — the guided narrative tour (READ THIS).
- `UPGRADING.md` — the AGP 9.x migration path and the version baseline.

---------

︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
