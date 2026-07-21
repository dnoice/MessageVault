<!--
✒ Metadata
    - Title: README (Message Vault Edition - v3.0)
    - File Name: README.md
    - Relative Path: README.md
    - Artifact Type: docs
    - Version: 3.1.0
    - Date: 2026-07-21
    - Update: Tuesday, July 21, 2026
    - Author: Dennis 'dendogg' Smaltz
    - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
    - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!

✒ Changelog:
    - 3.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Leads with the hero plate and
      the Preserve / Browse / Seal line. Adds a Documentation table pointing at the
      architecture walkthrough, the style charter and the upgrade path. Repairs the two
      links the markdown move broke: ARCHITECTURE.md and UPGRADING.md now resolve under
      docs/ instead of 404ing at the repository root.
    - 3.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — v1.0.0 release rewrite.
      Documents the app as it actually ships: the Home destination, the foreground
      ExportService with progress/cancel notifications, the vault (app lock, screen
      privacy, passphrase-encrypted .mvault delivery), Browse multi-select and
      avatars, the full run-directory layout, and the complete .mvault wire format
      with its recovery path. License section restated for MIT.
    - 2.1.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Header completion: the
      always-required Key Features and Other Important Information sections were
      missing, leaving the block ending at Description.
    - 2.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Added a License section
      pointing at the new LICENSE file (personal, all rights reserved), landed
      alongside the repo's first git history and .gitignore.
    - 2.0.0 (2026-06-22) [Anthropic - Claude Opus 4.8 (1M context)] — Rewrite for
      the navigation app (Export / History / Browse / Settings), the storage
      layer, run metrics, the public export folder, and share/copy delivery.
      Version baseline corrected to AGP 8.13.2 / Gradle 8.13.
    - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial README.

✒ Description:
    Project overview, feature tour, output reference, and build/run guide for
    Message Vault — the personal, sideloaded Android app that archives your own
    SMS/MMS history into durable, queryable formats, reads it back on-device, and
    can seal a run into a passphrase-encrypted envelope before it leaves the phone.

✒ Key Features:
    - Feature tour: what the app archives (SMS, MMS, attachments, contacts) and
      what each of the five destinations is for.
    - Output reference: the exact contents of a run directory — messages.jsonl,
      archive.db and its schema, conversations/, attachments/, metrics.json,
      MANIFEST.md.
    - Vault documentation: app lock, screen privacy, and the full .mvault wire
      format with the standalone Python recovery path.
    - Build and run guide: the JDK/JAVA_HOME setup, the Gradle wrapper, and how to
      sideload a debug build onto a device.
    - Permission model: why READ_SMS, READ_CONTACTS, All-files access,
      POST_NOTIFICATIONS, and the foreground-service permissions are each needed.
    - License pointer: MIT, with the archives themselves explicitly out of scope.

✒ Other Important Information:
    - Dependencies: assumes the shipped toolchain baseline — AGP 8.13.2, Gradle
      8.13, JVM 21, Kotlin + Jetpack Compose (Material 3), minSdk 29,
      compileSdk/targetSdk 35. No external tooling is needed to read this file.
    - Compatible platforms: the document itself renders anywhere Markdown does;
      the app it describes is Android-only (minSdk 29).
---------
-->

# digiSpace · Message Vault

![Message Vault — personal SMS/MMS archival for Android. Preserve, Browse, Seal.](docs/assets/readme-hero-image.png)

A personal, sideloaded Android app that exports your own SMS/MMS history off the
device into durable, queryable formats — reads it back on-device — and can seal a
run into a passphrase-encrypted envelope before it ever leaves the phone.

**Preserve · Browse · Seal.**

Built for a real corpus: 50,000+ messages and several hundred megabytes of
attachments. A single streaming pass fans out to every selected output, and
nothing ever materializes the whole dataset in memory.

> **Sideload only.** `READ_SMS` is a restricted Android permission; this is a
> personal tool you grant on-device and install yourself. It is not a Play Store
> app, has never been submitted to one, and is not intended to be.

## What it does

- Reads **SMS** and **MMS** through the system content providers (`READ_SMS`).
- Resolves numbers to **contact names** (`READ_CONTACTS`, optional — degrades
  gracefully to raw numbers).
- Streams the whole history once and writes any combination of:
  - **JSONL** — `messages.jsonl`, one complete JSON object per line.
  - **SQLite** — `archive.db`, a standalone indexed database any tool can open.
  - **Markdown** — `conversations/`, one readable transcript per thread.
  - **Attachments** — `attachments/`, MMS media decoded to real files and linked
    from the other outputs.
- Records **run metrics** — wall clock, per-phase and per-sink timing, throughput,
  SMS/MMS counts, attachment count and bytes, date range, attachment failures —
  into `metrics.json` and a human-readable `MANIFEST.md`.
- Runs the export in a **foreground service**, so a long export survives leaving
  the app, shows live progress in the notification shade, and can be cancelled
  from there.
- Lets you **browse the archive back** on-device, revisit **past runs**, and
  **deliver** any run by share or copy-to-folder.
- Locks the whole app behind **biometrics or your device credential**, blocks
  screenshots and recents previews, and can **encrypt every delivered export**
  with a passphrase you choose (`.mvault`, AES-256-GCM).

## The app

Message Vault is a navigation-drawer app with five destinations.

| Screen | What it does |
| ------ | ------------ |
| **Home** | Landing screen: last run's headline numbers, quick actions into Export / Browse / History, and where exports currently land. Reads only `metrics.json` headlines, never a message. |
| **Export** | Pick sources and formats, start a run, watch live progress, see the results summary with Share / Copy actions. |
| **History** | Every past run read back from its `metrics.json` — counts, throughput, date range — with re-Share, Copy, and guarded delete. |
| **Browse** | Read the latest `archive.db` back: conversation list with avatars, thread bubbles, debounced full-text search, and long-press multi-select to copy or share messages as plain text. |
| **Settings** | Storage access grant, storage usage and cache clearing, appearance (System / Light / Dark), the Vault switches, and About. |

Conversations get a face: a saved contact's photo where one exists, otherwise a
deterministic generated mark derived from the number, drawn only from the locked
digiSpace palette (navy, gold, slate, parchment). Crimson is reserved for genuine
errors and never used for decoration, in either theme.

## Where exports land

With **All files access** granted (Settings → Grant full access), runs are written
to a browsable, syncable public folder:

```text
/sdcard/MessageVault/exports/<yyyyMMdd_HHmmss>/
```

Without it, runs fall back to the app-private sandbox
(`/sdcard/Android/data/com.digispace.messagevault.debug/files/exports/`), which
modern Android hides from the Files app — so granting access is recommended.

Pull a run with a file manager, `adb pull`, a sync client, or the in-app **Share
export** / **Copy to folder** actions.

## What a run directory contains

Every run is a self-contained, timestamped folder. Only the outputs you selected
are written; the manifest and metrics are always written.

```text
/sdcard/MessageVault/exports/20260720_185150/
├── messages.jsonl          # one JSON object per message, streamable both ways
├── archive.db              # standalone indexed SQLite database
├── conversations/          # one Markdown transcript per thread
│   ├── mom_1004.md         # <contact-slug>_<threadId>.md
│   └── dennis-work_88.md
├── attachments/            # MMS media as real files, uniquely named
│   ├── IMG_0421.jpg
│   └── IMG_0421_1.jpg      # collision-safe suffixing
├── metrics.json            # machine-readable run metrics
└── MANIFEST.md             # the same run, in prose
```

### `messages.jsonl`

One JSON object per line — `jq`, pandas, and every streaming JSON reader take it
natively, and a consumer never has to load the file whole. Missing fields are
emitted as real JSON `null` rather than dropped, so the shape is stable.

### `archive.db`

Plain SQLite written with the raw platform API — no Room, no migration metadata,
nothing that ties the file to this app. Open it in any SQLite tool:

```sql
CREATE TABLE messages(
    id INTEGER, kind TEXT, thread_id INTEGER,
    address TEXT, contact_name TEXT, direction TEXT,
    epoch_millis INTEGER, body TEXT, read INTEGER,
    PRIMARY KEY(id, kind)
);

CREATE TABLE attachments(
    part_id INTEGER PRIMARY KEY, message_id INTEGER,
    content_type TEXT, file_name TEXT, export_name TEXT
);

CREATE INDEX idx_msg_thread ON messages(thread_id);
CREATE INDEX idx_msg_addr   ON messages(address);
CREATE INDEX idx_msg_date   ON messages(epoch_millis);
```

`kind` is `SMS` or `MMS`; the composite primary key keeps an SMS and an MMS that
happen to share a numeric id distinct. `epoch_millis` is normalized to
milliseconds for both kinds. `attachments.export_name` is the file's name inside
`attachments/`, or `NULL` if that part could not be extracted.

A run that is cancelled or fails **never publishes a truncated `archive.db`** —
the whole export is one transaction, committed only when both source loops finish,
and a rolled-back run's partial file is deleted.

### `metrics.json` and `MANIFEST.md`

`metrics.json` is the machine-readable snapshot of the run: start time, wall
clock, per-phase timing (read SMS, read MMS, attachments), per-sink timing,
throughput, SMS/MMS counts, attachment count and bytes, message date range, and
the attachment failure count with the first error. `MANIFEST.md` says the same
things in prose. History and Home read only these files, which is why they stay
instant no matter how big the archive is.

If any attachment could not be extracted, the count appears in both files, in the
completion notification, and on the Export results card. A run that lost files
never reports an unqualified success.

## The vault

Three switches under **Settings → Vault**, all off by default.

| Switch | Effect |
| ------ | ------ |
| **App lock** | Nothing inside the app renders until you authenticate with biometrics or your device credential. Re-locks whenever the app leaves the foreground. Disabled with an explanation if no screen lock is enrolled. |
| **Screen privacy** | Sets `FLAG_SECURE`: blocks screenshots, screen recording, and the recents-switcher preview. |
| **Encrypt shared exports** | Share and Copy hand over a passphrase-locked `.mvault` instead of a plain `.zip`. |

No passphrase is ever stored — not in preferences, not in the Keystore, not
anywhere on the device. It is requested at the moment of sealing, used to derive
the key, and zeroed. The dialog asks for it twice, because a typo in a write-only
secret produces an archive nobody can ever open.

The app lock is deliberately **not** a passphrase this app invented: it delegates
to the platform's biometric prompt with device-credential fallback.

## The `.mvault` format

An encrypted export is a plain `.zip` of the run directory wrapped in a single
authenticated envelope. The format is documented here, in
`app/src/main/java/com/digispace/messagevault/security/VaultCrypto.kt`, and in
`tools/decrypt_mvault.py`, precisely so it never depends on this app existing.

All integers are big-endian. The header is plaintext; only the payload is
encrypted.

| Offset | Size | Meaning |
| ------ | ---- | ------- |
| 0 | 8 | Magic, ASCII `MVAULT01` |
| 8 | 1 | KDF id — `0x01` = PBKDF2WithHmacSHA256 |
| 9 | 4 | Iteration count (int; currently 250,000) |
| 13 | 16 | Salt |
| 29 | 12 | GCM nonce (IV) |
| 41 | rest | AES-256-GCM ciphertext, final 16 bytes are the auth tag |

```text
key       = PBKDF2-HMAC-SHA256(passphrase, salt, iterations, 256 bits)
plaintext = the run directory's .zip
```

Salt, nonce, and KDF parameters travel with the file, so decryption never depends
on this app's defaults. Both salt and nonce come fresh from `SecureRandom` per
file — two seals of the same archive never produce identical bytes, and a GCM
nonce is never reused under a key. Encryption and decryption both stream, so a
700 MB archive is never held in memory.

Because GCM is authenticated, a wrong passphrase or a modified file **fails
loudly** rather than decrypting into garbage.

### Recovering a `.mvault` without the app

`tools/decrypt_mvault.py` is a standalone implementation of the format above. It
needs Python 3.8+ and one package — not this app, not the phone, not Android.

```bash
pip install cryptography
python tools/decrypt_mvault.py 20260720_185150.mvault
python tools/decrypt_mvault.py archive.mvault -o restored.zip
```

It prompts for the passphrase, streams the decryption in 1 MB chunks, and deletes
the output if authentication fails.

> **A lost passphrase is unrecoverable. Permanently.**
>
> There is no reset, no recovery key, no escrow, no backdoor, and no support
> channel that can help — by design. Nothing on the device or in this repository
> knows your passphrase. If you forget it, the archive is cryptographically
> indistinguishable from random noise for the rest of time.
>
> Write it down somewhere that is not the phone the archive came from, and keep at
> least one unencrypted copy of anything you genuinely cannot lose.

## Build and run

Prerequisites: Android Studio (Panda / 2025.3.x or compatible) with its bundled
JBR, and a device on Android 10 (API 29) or newer.

1. Open the project in Android Studio and let Gradle sync. The project ships
   pinned to **AGP 8.13.2 / Gradle 8.13 / Kotlin 2.1.0 / Compose BOM 2024.12.01 /
   navigation-compose 2.8.5**, JDK 21, compileSdk and targetSdk 35, minSdk 29 — a
   deliberate, known-good island. See [docs/UPGRADING.md](docs/UPGRADING.md) before touching
   the toolchain.
2. Connect the device (USB or wireless debugging), select it, and Run. The debug
   build carries the `.debug` application-id suffix so it can coexist with a
   release build.
3. Grant **SMS access** when prompted. Grant **All files access** from Settings so
   exports land somewhere you can actually see them. Allow notifications so
   progress and completion show in the shade.
4. Pick sources and formats on **Export**, tap **Run export**, and watch the bar —
   or leave the app; the foreground service keeps going.

Command-line build — set `JAVA_HOME` to Android Studio's bundled JBR first:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`. Install it with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Permissions, and why each one exists

| Permission | Why | If denied |
| ---------- | --- | --------- |
| `READ_SMS` | Reads both the SMS **and** MMS providers. This is the app. | Nothing can be exported. |
| `READ_CONTACTS` | Resolves numbers to names and shows contact photos in Browse. | Degrades to raw numbers and generated avatars. |
| `MANAGE_EXTERNAL_STORAGE` | Puts runs in a browsable `/sdcard/MessageVault/` folder instead of the scoped-storage-hidden sandbox. Granted on-device via system Settings. | Exports fall back to the app-private sandbox. |
| `POST_NOTIFICATIONS` | Export progress, cancel action, completion, and delivery status. | Runs still work; you just lose the shade entries. |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` | Keeps a long export alive when the app is backgrounded. | Not runtime-granted; declared in the manifest. |

`READ_SMS` and `MANAGE_EXTERNAL_STORAGE` are exactly the permissions that make
Play Store distribution impossible for a tool like this — which is why it is
sideload-only and always will be.

## Architecture at a glance

```text
data/model/    Message / Attachment / progress — the shared vocabulary
data/source/   SmsSource, MmsSource (the freakshow), ContactResolver
export/        Exporter interface + JSONL / SQLite / Markdown + AttachmentExtractor
               + ArchiveEngine (one streaming pass, fans out) + RunMetrics
service/       ExportService — foreground service that owns a run + ExportStatus
storage/       ExportLocation (public folder + access), RunDelivery (zip / SAF),
               RunHistory (past runs), ArchiveReader (read archive.db back)
security/      VaultCrypto (AES-256-GCM + PBKDF2), VaultPrefs (the three switches)
ui/            AppNav shell + Home / Export / History / Browse / Settings,
               ArchiveViewModel, LockScreen, PassphraseDialog, avatars, theme/
util/          Format helpers, Notifications
```

Two invariants hold the whole thing up:

- **Flat memory.** Every source streams rows through `forEach(consume)`, so
  cursors close deterministically and nothing accumulates the corpus. Attachments
  stream from the provider straight to disk; the zip and the encryption stream too.
- **One pass.** `ArchiveEngine` reads the history exactly once and fans each
  message out to every selected `Exporter`. A new output format is a new
  `Exporter` plus one line in the engine.

The full guided tour is in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and the
visual language every screen is held to is [docs/STYLE.md](docs/STYLE.md).

## Documentation

| Document | What it covers |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Guided tour of every module, in dependency order, and how one export run executes end to end. |
| [docs/STYLE.md](docs/STYLE.md) | The visual charter. Why this reads as an archival instrument and not a messenger, and the rules every screen follows. |
| [docs/UPGRADING.md](docs/UPGRADING.md) | The pinned AGP 8.x island and what moving to AGP 9.x would involve. |
| [tools/decrypt_mvault.py](tools/decrypt_mvault.py) | Standalone decryptor for a sealed `.mvault`, so the archive outlives the app. |

## Known gotchas already handled

- **MMS dates arrive in seconds**, SMS in milliseconds — normalized to
  milliseconds at the source.
- **MMS is multi-table** — parts and addresses are queried per message, and the
  layout-only `application/smil` part is skipped.
- **Attachments stream** straight from `content://mms/part/{id}` to disk, and a
  failed copy deletes its partial file instead of leaving an orphan.
- **SQLite inserts are compiled once** and rebound per row, not recompiled per
  message.
- **A cancelled export settles on "Cancelled."**, not a false "Failed.", and rolls
  back rather than publishing a partial archive.
- **A sealed `.mvault` is handed over as `application/octet-stream`**, because
  claiming `application/zip` made document providers rename it to
  `<run>.mvault.zip` — a zip that is not a zip.

## License

MIT. Copyright (c) 2026 Dennis 'dendogg' Smaltz. See [LICENSE](LICENSE).

Your archives are yours: the license covers this software, never the contents of
what it produces. The app sends nothing anywhere.

︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
