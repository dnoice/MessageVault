<!--
✒ Metadata
    - Title: Visual Charter (Message Vault Edition - v1.0)
    - File Name: STYLE.md
    - Relative Path: docs/STYLE.md
    - Artifact Type: docs
    - Version: 1.0.0
    - Date: 2026-07-21
    - Update: Tuesday, July 21, 2026
    - Author: Dennis 'dendogg' Smaltz
    - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
    - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!

✒ Changelog:
    - 1.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Initial charter: resolves the five per-tab design surveys into one binding rule set (type roles, ink scale, palette duties, surface treatment, density, metadata presentation, iconography, motion) and names the UiKit primitives that implement it.

✒ Description:
    The binding visual charter for Message Vault. It defines, once, what "reads as an
    archive rather than a messenger" means in this app, and resolves the five per-tab
    surveys into a single rule set. Every screen agent follows this document; where a
    tab's survey proposed something this charter contradicts, the charter wins.

✒ Key Features:
    - The governing test: one sentence that settles any style argument on this app.
    - Type roles: exactly where monospace carries meaning and where it is forbidden.
    - Ink scale: four named alphas replacing the ~fifteen ad-hoc values across the screens.
    - Palette duties: what each of navy, gold, slate, parchment and crimson is FOR.
    - Surface law: plates, cards, rules — and the ban on gradients, elevation and lozenges.
    - Density and alignment: gutters, columns, row rhythm, the right-aligned value edge.
    - Metadata law: what every record must print and where it prints it.
    - Iconography: the approved glyph set and the permanently banned glyphs.
    - Motion character: two durations, linear-out, opacity only, no stagger.
    - Copy register: how the app is allowed to speak.
    - A primitive index mapping every rule to the UiKit composable that implements it.

✒ Other Important Information:
    - Dependencies: implemented by app/src/main/java/com/digispace/messagevault/ui/UiKit.kt; palette in ui/theme/Theme.kt; type scale in ui/theme/Type.kt.
    - Compatible platforms: documentation — applies to the Android app (minSdk 29, compileSdk 35).
    - Authority: UiKit.kt is edited by the design owner only. Screens consume primitives; they do not fork them.
---------
-->

# Message Vault — Visual Charter

## 0. The governing test

> **Message Vault is a reading instrument for a corpus the user already owns.
> It cannot send, reply, call or compose, and nothing in it may imply that it can.**

When any style question is ambiguous, apply this test:

**Would this element be at home on a catalogue card, a transcript page, a lab
readout, or a bound register — or is it at home on a chat screen?**

If the second, it is wrong, no matter how well it is executed.

Three shapes are permanently banned from this app, regardless of context:

1. The **bubble** — any asymmetric or heavily rounded container holding message text.
2. The **lozenge** — gradient-filled, soft-cornered stat capsules and status chips.
3. The **round avatar** — circular identity marks, and the gloss pass that goes with them.

---

## 1. Type roles

The app has two voices. They are not interchangeable and they are not decorative.

### 1.1 Monospace is the voice of the record

Monospace, always, no exceptions:

- Every **number the archive measured** — counts, byte sizes, durations, percentages,
  throughput, ordinals.
- Every **timestamp and date** that is presented as data.
- Every **identifier** — accession slugs (`20260720_150412`), thread ids, record
  ordinals, package ids, version strings.
- Every **filesystem path and filename** — `messages.jsonl`, `archive.db`, `media/`,
  and full absolute paths.
- Every **status token** — `COMPLETE`, `PARTIAL`, `RUNNING`, `ENABLED`, `NO INDEX`.
- Every **section label and field label**, uppercase and letter-spaced.
- Every **query input**, and the readout beside it.

### 1.2 Sans-serif is the voice of language

Sans-serif, always:

- Message body text in the transcript. This is human language; it is not data.
- Correspondent names and contact names.
- Explanatory prose, notes, dialog consequence lines, error explanations.

### 1.3 Forbidden

- **Never** set a measured figure, a path, an identifier or a stamp in sans.
- **Never** set a sentence of prose in monospace. Monospace prose is set-dressing;
  monospace data is meaning. Spending it on prose destroys the distinction.
- **Never** re-declare `FontFamily.Monospace` at a call site. Use the `MvType` styles.

### 1.4 The mono scale

`Type.kt` only ships mono at `labelSmall` (12sp), which forced every data line in the
app to 12sp. `MvType` in UiKit adds the missing sizes. All of them enable tabular
figures (`tnum`) so digits column-align:

| Token | Size | Use |
| ----- | ---- | --- |
| `MvType.MonoFigure` | 24sp | The one headline figure on a plate |
| `MvType.MonoValue` | 15sp | Field values that carry weight — totals, sizes |
| `MvType.Mono` | 13sp | The default data line: stamps, counts, paths, filenames |
| `MvType.MonoSmall` | 11sp | Gutter ordinals, stamps, captions under figures |
| `MvType.Label` | 11sp | Uppercase letter-spaced field and section labels |

Sizes are in `sp` and scale with the user's font setting. Nothing in this app may be
`dp`-sized text.

---

## 2. Ink — the alpha scale

Hierarchy is made of **four** quantised levels over `onSurface`, never of eyeballed
transparency. This replaces every ad-hoc `0.5 / 0.6 / 0.65 / 0.7 / 0.75 / 0.8` in the app.

| Token | Alpha | Use |
| ----- | ----- | --- |
| `MvAlpha.Data` | 1.00 | Values, figures, record text — anything the archive measured or stored |
| `MvAlpha.Body` | 0.72 | Supporting prose, explanations, secondary language |
| `MvAlpha.Faint` | 0.52 | Labels, captions, gutter ordinals, disabled state |
| `MvAlpha.Hairline` | 0.10 | Every rule and every border, without exception |
| `MvAlpha.Recess` | 0.04 | The fill of a recessed plate (paths, engraved fields) |

Note the inversion this forces, and it is deliberate: **the data is the brightest thing
on the screen and the label is the dimmest.** Today several screens do the opposite.

---

## 3. Palette duties

Each colour has exactly one job. Colour is not used to decorate and not used to
differentiate more than one thing per surface.

| Colour | Role | Job |
| ------ | ---- | --- |
| Navy `#1B2A4A` | light `primary`, dark `background` | Structure and the single emphasised figure |
| Gold `#C9A84C` | dark `primary`, advisory rules | Emphasis in dark; the left rule on a note. Never a wash, never a gradient |
| Slate `#3D4F5F` | `secondary`, dark `surface` | Surface only. **Slate is not an ink and not a signal** |
| Parchment `#F4EDD8` | light `background`, dark `onSurface` | Ground and ink |
| Crimson `#9B2C2C` | `error` | Genuine failures only — a failed run, a destructive confirm. Never a warning, never decoration |

### 3.1 The tertiary ban

`colorScheme.tertiary` **must not be used anywhere in this app.**

In the light scheme `tertiary == secondary == Slate` (Theme.kt:72-73), so every meaning
carried on tertiary — section labels, warnings, hints, attachment marks, the signature —
collapses into one indistinguishable grey. In the dark scheme `tertiary == Gold`, so the
same elements all ignite at once and the surface reads as decorated. Both modes are wrong.

Wherever a screen currently reaches for `tertiary`, it uses `MvInk.Faint` instead, and
if the element needed to be *noticed*, it becomes a **stamp** or a **note**, not a colour.

### 3.2 One accent per plate

A plate may promote **exactly one** element with `primary`. Everything else on that plate
is `onSurface` at one of the three ink levels. If two things want the accent, one of them
is not as important as you think.

### 3.3 Warning has no colour

Degraded and partial states are carried by **geometry and words**, not hue: an
`MvStamp` with `MvTone.Flagged` (hairline outline, no fill) or an `MvNote` (2dp gold left
rule, ordinary ink). This survives both schemes identically and keeps crimson reserved.

### 3.4 Direction is not primary-versus-secondary

Browse's inbound/outbound rule must never be `primary` vs `secondary`: in dark mode the
surface **is** Slate, so a Slate rule is invisible. Use `MvDirection.Outbound` (primary)
and `MvDirection.Inbound` (`onSurface` at 0.45). Both are distinct from each other and
from the ground in both schemes.

Selection is a **geometry** change — the gutter fills solid — never a colour change,
because a colour change is invisible on whichever direction already owns that colour.

---

## 4. Surfaces

### 4.1 Three surfaces, and only three

| Surface | Radius | Border | Fill | Purpose |
| ------- | ------ | ------ | ---- | ------- |
| **Card** (`MvCard`) | 14dp | 1dp hairline | `surface` | The outer container. Groups a section |
| **Plate** (`MvPlate`) | 4dp | 1dp hairline | none, or `MvAlpha.Recess` | Anything carrying data. Lives inside a card |
| **Mark** (`MvShape.Mark`) | 2dp | 1dp hairline | none, or solid when set | Stamps, state cells, selection marks, identity frames, rules |

The geometry tightened deliberately: `MvShape.Card` moved 22dp → **14dp** and
`MvShape.Control` 16dp → **10dp**. A 22dp radius repeated at every scale is what made the
app read friendly. Softness is now reserved for the outer container only; **anything
carrying data goes crisp.**

`MvShape.Pill` is retired. It was the same 16dp value as `Control`, i.e. a name with no
distinct meaning, and the concept it named is banned. It remains as a deprecated alias so
old call sites compile; do not write new ones.

### 4.2 Absolute rules

- **No elevation. Ever.** Cards separate with a hairline. A Material card rasterises the
  same rounded rect three times and leaves a hairline ring inside every corner at this
  contrast. Do not reintroduce `CardDefaults.cardElevation` with a non-zero value, and do
  not use a bare `Card` — use `MvCard` or `MvPlate`.
- **No gradients.** Not on stat surfaces, not on progress bars, not as a wash. A
  travelling gradient is a skeleton-loader idiom borrowed from social feeds.
- **No translucent tints as containers.** A selection bar or an active cell is an
  *opaque plate with a hairline*, not `primary.copy(alpha = 0.12f)`.
- **Every hairline is the same hairline**: 1dp at `MvAlpha.Hairline`. Use `MvRule` and
  `MvInk.Hairline`; never type the alpha by hand.

### 4.3 Rules do the work whitespace used to

A list of records is **ruled**, not spaced. Between transcript records, between index
rows, between field rows, between toggle rows: a hairline. Whitespace-separated cards is
the feed pattern; hairline-divided rows is the register pattern. This is the single
highest-leverage move available on every tab.

Where a gutter runs (Browse's direction column, a catalogue-number column), the rule is
**inset** to the text margin so the gutter runs visually uninterrupted down the whole
page. `MvRule(inset = MvGutterWidth)`.

---

## 5. Density and alignment

### 5.1 The rhythm

| Token | Value | Use |
| ----- | ----- | --- |
| `MvSpace.ScreenH` / `ScreenV` | 24 / 22dp | Screen gutters |
| `MvSpace.Section` | 16dp | Between sibling cards |
| `MvSpace.Card` | 18dp | Padding inside a card (was 22dp — tightened) |
| `MvSpace.Plate` | 14dp | Padding inside a plate |
| `MvSpace.Item` | 12dp | Between groups inside a card |
| `MvSpace.Row` | 6dp | Between ruled field rows |
| `MvSpace.Inline` | 10dp | Between side-by-side controls |

Density is the tell. Uniform 12dp inside a soft card is a card feed; 6dp ruled rows
inside a crisp plate is an instrument. Vary the density between groups and rows — do not
let one gap value run the whole screen.

### 5.2 The value edge

**Every field row in the app aligns its value to the same right edge.** Label left in
uppercase mono at `Faint`; value right-aligned in mono with tabular figures at `Data`.
That hard vertical edge down the right of a plate is the strongest single cue that a
surface is a ledger. Use `MvFieldRow`; do not hand-roll a `Row` with a `Spacer`.

Prose sentences joined by middots — `"SMS 42 · MMS 7 · 12.4s"` — are banned. Four fields
means four rows.

### 5.3 The gutter

`MvGutterWidth = 32.dp` is shared by Browse's direction rule and by every catalogue
number on every other tab, so columns line up across tabs and the app reads as one
instrument rather than five screens.

### 5.4 Numbers

- All integers pass through `MvNum(n)` — grouped digits, always. `1,204` and `1204` must
  never appear in the same card.
- Column figures are zero-padded to a fixed width where they sit in a column
  (`MvOrdinal(i)` → `007`).
- Byte figures show the human value and, on measurement blocks, the raw count:
  `4.21 MB  (4,414,983 B)`.
- A figure that ticks is shown, not animated. `animateIntAsState` on a live count is
  banned — it misreports state on a tool whose promise is accuracy.

---

## 6. Metadata law

**Metadata made visible is the aesthetic.** The app currently computes and discards
almost every identifier it owns. That stops.

### 6.1 Every record prints its identity

- A run prints its **accession** — the `yyyyMMdd_HHmmss` folder slug — in mono, as its
  identity line. The friendly date is demoted to a secondary line.
- A run prints its **ordinal** in the register (`№ 007`) in the gutter.
- A thread prints its **thread id**, its record count, and its date range.
- A transcript record prints its **ordinal** in the gutter.
- Every archive view prints **which archive it is reading** — filename and run date.

### 6.2 Every record prints where it lives

The on-disk path is the most archival fact the app knows. It is a **field of record on a
recessed plate**, selectable, at `MvAlpha.Data` — never a 0.65-alpha caption under a
button. `MvPathText` / `MvPlate(recessed = true)`.

### 6.3 Every record prints its condition

Status is stamped on **every** record, not only on failures. `COMPLETE`, `PARTIAL`,
`NO INDEX`, `INDEXED` — in a scannable column. Silence-means-good is an app convention;
a register states the condition of every entry.

### 6.4 Timestamps

- Data timestamps use `Format.timestamp` — `yyyy-MM-dd HH:mm:ss` — in mono,
  right-aligned. Fixed width, lexically sortable, column-aligned.
- `Format.friendly` survives in exactly one place: a **day band heading** in the
  transcript, where a human-readable date genuinely helps a reader.
- `Format.relativeAge` is never printed as a sentence. Age is a field: `AGE  14 d`.

### 6.5 Metadata position

Metadata sits **above or beside** the content it describes, never below it as a footnote,
and never as the last line of a plate. A plate ends on data, not on chrome.

### 6.6 Transient surfaces are banned

No `Toast`. A vault records; it does not flash. Confirmations become a timestamped mono
acknowledgement line inside the card that produced the action
(`PATH COPIED · 14:32:07`), carried by `MvInlineAck` with a live-region semantic so
TalkBack still announces it.

---

## 7. Controls

### 7.1 The Switch is retired

The rounded track-and-thumb pill is the single most recognisable consumer-mobile control
there is; nine of them stacked is a messenger's settings page. Replace with `MvStateToggle`:
label left, and on the right a fixed-width hairline **state cell** holding a mono uppercase
token.

- Settings uses `ENABLED` / `DISABLED` (and `REFUSED` where no credential exists).
- Export uses `INCLUDED` / `OMITTED`.

The row keeps `Role.Switch`, `mergeDescendants`, the 48dp floor and the same click
target. Accessibility is unchanged; the state also becomes *readable as text down a
column*, which is strictly better than decoding six pill positions.

### 7.2 Buttons

Three weights, and no more:

1. `MvPrimaryButton` — the one operation a screen exists to perform.
2. `MvSecondaryButton` — a real alternative operation.
3. `MvTextAction` — everything else: copy, share, delete, dismiss. No pill, no border.
   The destructive variant carries crimson **ink only** — no fill, no outline — and is
   separated from the others rather than given equal weight.

Three equal-weight rounded pills in a row is a photo-app toolbar. Card actions live in an
`MvCardFooter`: a hairline rule across the card's inner width, then text actions.

### 7.3 Readouts

- Progress is `MvMeter`: 6dp, square ends, hairline ticks at 25/50/75. Not a rounded
  capsule, not a shimmer.
- Indeterminate work is `MvMeasuring` — a hairline sweep under a mono label. Not a
  spinner. `CircularProgressIndicator` is retired outside `MvLoadingState`.
- A control's label never mutates into a status sentence. `Clear cache` stays
  `Clear cache` and disables; the measurement is a `CACHED` field elsewhere.

---

## 8. Iconography

Icons are **rationed**. An instrument earns its look by having fewer marks, not more.
Prefer a typographic mark — the literal string `SMS` in mono — over a glyph.

### 8.1 Permanently banned

`Forum`, `Sms`, `Chat`, `ChatBubble`, `Message`, `Send`, `Reply`, `Call`, `Person`,
`PersonAdd`, and any other glyph from Material's messaging or social set. `Forum` reached
Home and Export as the largest graphic on both screens; `MvIcons` exists so that cannot
happen again.

### 8.2 The approved set

Use `MvIcons` only. It maps archive concepts to glyphs: `Records`, `Index`, `Attachment`,
`Location`, `Inventory`, `Search`, `Copy`, `Extract`, `Delete`, `Settings`, `Lock`,
`Verified`. If a screen needs a concept that is not in `MvIcons`, add it to `MvIcons` —
do not reach into `Icons.Outlined` at a call site.

### 8.3 No icon chips

The 44dp rounded tinted tile with a glyph in it is an app icon or a contact avatar, and
`[tile] [title] [subtitle] [switch]` is the exact anatomy of a chat list row. Glyphs sit
**bare in the gutter at 20-24dp**, on no background.

### 8.4 Identity marks

Where a correspondent is depicted, it is a **square identity plate**: `MvShape.Mark`
corners, a hairline border, the existing deterministic seed palette, and the white gloss
pass removed. Real contact photos are clipped to the same square plate.
`MvIdentityFrame` provides the geometry.

---

## 9. Motion

Motion is **mechanical, short and honest**. A display refreshes; a UI performs. This app
refreshes.

| Token | Duration | Easing | Use |
| ----- | -------- | ------ | --- |
| `MvMotion.Snap` | 120ms | linear | State swaps, crossfades, selection feedback |
| `MvMotion.Settle` | 200ms | linear-out | Content that grows or shrinks, reveals |

Rules:

- **Opacity only.** No `slideInVertically`, no scale, no spring, no bounce. Records were
  already there; they do not travel into place.
- **No stagger.** Per-item delays are feed-arrival choreography. `MvReveal` fades a whole
  region once, and it is the only entrance any screen may use.
- **No decorative motion.** The 650ms growing motif, the 1600ms travelling shimmer and
  the breathing status dot are all deleted. A slow pulsing dot next to a status word is
  literally a typing indicator.
- **No eased counters.** See 5.4.
- Screens never pass a raw `tween(n)`. Use `MvMotion.snap()` / `MvMotion.settle()`.

---

## 10. Copy register

The app **states conditions**; it does not talk to the user.

- No second person. Not "your archives" — "ARCHIVES", or "the archive".
- No greetings, no apologies, no encouragement, no nagging. Not "Welcome to Message
  Vault", not "Couldn't read your archives", not "It's been 3 weeks — consider a fresh
  backup".
- No instruction lines and no arrow glyphs. `Tap to view history →` is deleted; the card
  is already clickable and already carries an `onClickLabel`. Affordance is structural.
- States and operations are **named**, in mono uppercase where they are tokens:
  `NO RECORDS`, `EXPORT FOLDER UNREADABLE`, `SMS PERMISSION REQUIRED`, `BEGIN RUN`,
  `RUN AGAIN`, `RETRY`.
- Sections are nouns, catalogue-numbered: `01 — STORAGE`, `02 — VAULT`,
  `03 — DEFAULT EXPORT`. Not `QUICK ACTIONS` — that is launcher vocabulary.
- Loading states name the operation and the corpus: `Reading index`, not
  `Reading your archives…`. Drop the trailing ellipsis; it is a sigh.
- Counts are figures with captions, not pluralised prose: `0417 RECORDS`, `12 MATCHES`,
  `07 SELECTED` — which also disposes of every `match/matches` and `(s)` construction in
  the app.

---

## 11. Accessibility — non-negotiable

Nothing in this charter may cost accessibility:

- 48dp minimum touch target on every interactive row. `MvTouchTarget`.
- Toggle rows stay merged with `Role.Switch` semantics; the state cell is decorative to
  the semantics tree, not a second focusable.
- Every icon is either `contentDescription = null` (decorative, and the label names the
  action) or genuinely described.
- Stamps and state cells carry **words**, so state never depends on colour alone. This is
  an accessibility improvement over the switches they replace.
- All text is `sp` and wraps. `maxLines = 1` only where truncation is genuinely correct
  (a path, via middle-ellipsis).
- `MvInlineAck` carries `liveRegion` so replacing Toasts does not silence TalkBack.

---

## 12. Primitive index

Every rule above has an implementation in `ui/UiKit.kt`. Screens consume these; screens
do not fork them.

```text
TOKENS
  MvSpace      ScreenH ScreenV Section Card Plate Item Row Inline
  MvShape      Card(14) Control(10) Plate(4) Mark(2) Pill(deprecated)
  MvAlpha      Data 1.0 · Body 0.72 · Faint 0.52 · Hairline 0.10 · Recess 0.04
  MvInk        Data Body Faint Hairline Recess Accent  (composable colour getters)
  MvType       MonoFigure MonoValue Mono MonoSmall Label
  MvMotion     Snap(120) Settle(200) · snap() settle() · SnapFloat SettleFloat
  MvDirection  Inbound Outbound  (scheme-safe pair)
  MvIcons      Records Index Attachment Location Inventory Search Copy Extract
               Delete Settings Lock Verified   (Forum/Sms/Chat/Send are absent by design)
  MvGutterWidth 32dp · MvContentWidth 560dp · MvReaderWidth 720dp
  MvTouchTarget 48dp · MvControlHeight 54dp

STRUCTURE
  MvCard           soft outer container, hairline, zero elevation, optional whole-card tap
  MvPlate          crisp data surface, hairline, optional recessed fill, optional tap
  MvRule           the 1dp hairline, with an inset variant
  MvVerticalRule   the same hairline, vertical, for divided cells
  MvSectionLabel   mono uppercase label; optional ordinal, trailing rule, trailing slot
  MvCardFooter     hairline-ruled action strip flush inside a card
  MvReveal         the app's one entrance — opacity only, no stagger

DATA
  MvFieldRow       LABEL left · value right-aligned mono · optional rule
  MvStatPlate      flat multi-cell figure plate, vertical hairlines (replaces MvStatPill)
  MvFigure         one mono figure with a mono uppercase caption
  MvMono           the single monospace text treatment
  MvCatalogId      accession / thread-id / ordinal treatment
  MvPathText       selectable mono path, middle-ellipsised on request
  MvStamp          rectangular hairline status token, MvTone Neutral/Active/Flagged/Failed
  MvNote           2dp gold left rule + "NOTE —" prefix; the one advisory treatment
  MvDayRule        full-width ruled date band, label flush-left riding the rule
  MvNum/MvOrdinal  grouped-digit and zero-padded formatters

CONTROLS
  MvStateToggle    the Switch replacement — merged row + mono state cell
  MvSelectMark     squared inclusion mark for manifest rows
  MvModeStrip      hairline segmented selector, solid active cell
  MvQueryField     squared query input, external mono label, right readout slot
  MvTextAction     the quiet tier below MvSecondaryButton; destructive variant
  MvPrimaryButton / MvSecondaryButton   unchanged in role, crisper in geometry

READOUTS & STATES
  MvMeter          squared determinate/indeterminate meter with quarter ticks
  MvMeasuring      hairline sweep + mono label, in-card
  MvInlineAck      timestamped mono acknowledgement line (the Toast replacement)
  MvConfirmDialog  plate-geometry dialog whose body is a field manifest
  MvLoadingState / MvEmptyState / MvErrorState / MvCentered   unchanged in role
  MvIdentityFrame  square hairline frame for identity marks and contact photos
```

---

## 13. Conflicts the charter settled

Where two surveys proposed different answers, this is the decision and the reason.

| Question | Decision | Why |
| -------- | -------- | --- |
| Plate radius: 2-4dp, 3dp, or 4-6dp? | **Plate 4dp, Mark 2dp** | Two steps is enough vocabulary. 4dp reads as a plate at card scale; 2dp reads as square at token scale |
| Keep `MvShape.Card` at 22dp? | **No — 14dp** | Three surveys independently named the soft radius as the messenger tell. Softness is now the container's alone |
| Ink scale values? | **1.0 / 0.72 / 0.52** | History's proposal; the widest spread of the three, so the levels are actually distinguishable |
| Where does the mono headline figure live? | **`MvType` in UiKit, not new Typography roles** | Keeps `Type.kt` untouched, so the five tab agents cannot collide on it |
| Switch replacement wording? | **Caller supplies the pair** | Settings wants ENABLED/DISABLED, Export wants INCLUDED/OMITTED — one primitive, two vocabularies |
| Warning colour? | **None — stamp or note** | Every colour proposal collapsed in one scheme. Geometry and words survive both |
| Stat surface? | **`MvStatPlate`; `MvStatPill` deprecated** | Nothing in the app wants a gradient lozenge. The pill stays only so old call sites compile |
| Date range notation? | **`2019-03-11 .. 2026-07-19`** | `→` is a UI glyph implying movement; `..` is range notation |
| Row primitive naming (`MvDataRow` / `MvRecordRow` / `MvFieldRow`)? | **`MvFieldRow`** | One name, three surveys' worth of use. Do not introduce the synonyms |
| Toasts? | **Deleted, replaced by `MvInlineAck`** | Unstyled, transient, and unrecorded — the opposite of the app's premise |
| Entrance animation? | **`MvReveal`, opacity only, no stagger** | Staggered rise is feed choreography. Records do not arrive |

---

︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
