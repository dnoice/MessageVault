/*
 * ✒ Metadata
 *     - Title: UI Kit (Message Vault Edition - v2.1)
 *     - File Name: UiKit.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/UiKit.kt
 *     - Artifact Type: library
 *     - Version: 2.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 2.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The systemwide consistency pass. Five tab agents applied the charter in parallel and each privately re-invented the same four things, so those four move here: MvFieldPlate (four screens wrote MvPlate { Column(padding…) } at four different insets, so ledger rows did not share margins between tabs), MvLocationPlate (one concept, four spellings of its label), MvFailureNote (the in-card crimson treatment ArchiveScreen hand-rolled and SettingsScreen substituted a bare crimson sentence for), and MvBytes / MvClock / MvAckHoldMs (two identical private clock helpers, a third that printed the full date, and acknowledgements that expired on one tab and never on another). MvIcons gains Back / Dismiss / External so Browse can stop reaching into Icons.* at call sites, which STYLE.md 8.2 forbids and which Browse's own header admitted to. Tokens and existing primitives are unchanged; nothing here alters behaviour.
 *     - 2.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, implementing STYLE.md. Added the missing token families (MvAlpha/MvInk ink scale, MvType mono roles with tabular figures, MvMotion, MvDirection, MvIcons, MvGutterWidth, MvShape.Plate/Mark, MvSpace.Plate/Row) and the primitives the five screens each needed and were each about to invent privately: MvPlate, MvRule/MvVerticalRule, MvFieldRow, MvStatPlate, MvFigure, MvMono, MvCatalogId, MvPathText, MvStamp, MvNote, MvDayRule, MvStateToggle, MvSelectMark, MvModeStrip, MvQueryField, MvTextAction, MvCardFooter, MvMeter, MvMeasuring, MvInlineAck, MvConfirmDialog, MvReveal, MvIdentityFrame, MvNum/MvOrdinal. Tightened the geometry (Card 22->14dp, Control 16->10dp, card padding 22->18dp) and retired the spinner from MvLoadingState. MvStatPill and MvShape.Pill kept working but deprecated by the charter.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial: one home for the spacing / radius / width tokens and the card, section-label, stat-pill, button, and empty/loading/error state primitives that the five screens had each re-declared privately and slowly drifted apart on.
 *
 * ✒ Description:
 *     The house style, expressed once. This file is the implementation of STYLE.md — the
 *     charter that defines what "reads as an archive rather than a messenger" means in
 *     this app. It holds the tokens (spacing, geometry, the ink scale, the monospace type
 *     roles, motion, the approved glyph set) and the primitives they compose into, so a
 *     rhythm change lands in one place and no screen has to invent a divider, a status
 *     token or a field row for itself. It adds no behaviour — every primitive here either
 *     replaces a private twin or gives a screen a sanctioned way to stop hand-rolling one.
 *
 * ✒ Key Features:
 *     - Token families: MvSpace / MvShape / MvAlpha / MvInk / MvType / MvMotion / MvDirection / MvIcons, plus the width, gutter and touch-target measures.
 *     - Two surfaces: MvCard (soft outer container) and MvPlate (crisp data surface). Both hairline-bordered, both zero-elevation, neither ever gradient-filled.
 *     - The ruled vocabulary: MvRule / MvVerticalRule / MvSectionLabel / MvCardFooter / MvDayRule — hairlines at one alpha, so every division in the app matches every other.
 *     - The data vocabulary: MvFieldRow, MvStatPlate, MvFigure, MvMono, MvCatalogId, MvPathText, MvNum / MvOrdinal — labels left and faint, values right-aligned and monospace with tabular figures.
 *     - Status without colour: MvStamp (rectangular hairline token) and MvNote (gold left rule), so partial and degraded states read identically in both schemes and crimson stays reserved for genuine failure.
 *     - Controls that are not messenger controls: MvStateToggle (the Switch replacement, semantics intact), MvSelectMark, MvModeStrip, MvQueryField, MvTextAction.
 *     - Readouts: MvMeter (squared, tick-marked, no shimmer), MvMeasuring, MvInlineAck (the Toast replacement, with a live region), MvConfirmDialog (a field manifest, not a paragraph).
 *     - Motion discipline: MvMotion's two durations and MvReveal's opacity-only entrance replace the tween(140/180/200/220/260/500/650/900/1600) scatter across the screens.
 *     - One spelling per concept: MvFieldPlate (the ledger column), MvLocationPlate (a path of record), MvFailureNote (in-card crimson), MvBytes / MvClock / MvAckHoldMs — each added because five parallel screens had independently grown their own.
 *     - Accessibility carried by the primitives: 48dp floors, merged toggle rows with Role.Switch, worded state tokens so nothing depends on colour alone, live-region acknowledgements.
 *
 * ✒ Usage Instructions:
 *     Screens compose from these and never from raw dp, raw alphas, raw tweens or raw
 *     Icons.* lookups. If a screen needs something this file does not have, it is added
 *     here — forking a primitive privately is what produced the drift v1.0.0 was written
 *     to end. Read STYLE.md before adding anything: the charter decides, this file obeys.
 *
 * ✒ Examples:
 *     - A ledger plate:            MvPlate { MvFieldRow("MESSAGES", MvNum(12004)); MvFieldRow("SIZE", Format.bytes(n), rule = false) }
 *     - A run's identity:          MvCatalogId("20260720_150412")
 *     - A run's condition:         MvStamp("PARTIAL", tone = MvTone.Flagged)
 *     - A settings row:            MvStateToggle("Require unlock", checked, onToggle)
 *     - An export option row:      MvStateToggle("JSONL", on, onToggle, onText = "INCLUDED", offText = "OMITTED")
 *     - A run in progress:         MvMeter(progress = 0.47f, contentDescription = "47 percent complete")
 *     - A copy confirmation:       MvInlineAck("PATH COPIED · " + clock)
 *     - A destructive confirm:     MvConfirmDialog("DELETE RECORD", fields, consequence, "DELETE", onConfirm, onDismiss)
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (foundation, material3, material-icons-extended, animation, ui.semantics).
 *     - Palette discipline: crimson arrives only through MaterialTheme.colorScheme.error (MvErrorState, MvTone.Failed, a destructive confirm label) — never as decoration. colorScheme.tertiary is banned app-wide; it collapses onto secondary in the light scheme.
 *     - Elevation discipline: no surface in this app casts a shadow. A Material card rasterises the same rounded rect three times and leaves a hairline ring inside every corner at this contrast.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digispace.messagevault.ui.theme.Gold
import com.digispace.messagevault.util.Format
import java.util.Locale

// ── Tokens: rhythm ───────────────────────────────────────────────────────────

/** The vertical/horizontal rhythm. Screens compose from these, never from raw dp. */
object MvSpace {
    /** Gutter between screen content and the screen edge. */
    val ScreenH = 24.dp
    /** Top/bottom breathing room inside a scrolling screen. */
    val ScreenV = 22.dp
    /** Gap between sibling cards. */
    val Section = 16.dp
    /** Padding inside a card. Tightened from 22dp — density is the tell. */
    val Card = 18.dp
    /** Padding inside a plate. One step denser than a card. */
    val Plate = 14.dp
    /** Gap between groups inside a card. */
    val Item = 12.dp
    /** Gap between ruled field rows. A field list at 12dp reads as a card feed. */
    val Row = 6.dp
    /** Gap between side-by-side controls. */
    val Inline = 10.dp
}

/**
 * Corner geometry. Softness belongs to the outer container and nowhere else: anything
 * carrying data goes crisp. Card came down from 22dp and Control from 16dp because a
 * generous radius repeated at every scale is the single loudest consumer-app signal
 * the app was giving off.
 */
object MvShape {
    /** The outer container. The only soft shape in the app. */
    val Card = RoundedCornerShape(14.dp)
    /** Buttons and inputs. */
    val Control = RoundedCornerShape(10.dp)
    /** Data surfaces — anything that reads as a plate rather than a card. */
    val Plate = RoundedCornerShape(4.dp)
    /** Stamps, state cells, selection marks, identity frames. Effectively square. */
    val Mark = RoundedCornerShape(2.dp)

    /**
     * Deprecated by STYLE.md. It was always the same value as [Control], i.e. a name with
     * no distinct meaning, and the lozenge it named is banned. Kept only so pre-charter
     * call sites keep compiling — do not write new ones.
     */
    val Pill = Control
}

/** Cards stop growing here so they read as cards on the Fold's inner screen. */
val MvContentWidth = 560.dp

/** Reading surfaces (conversation lists, threads) may run wider than a card. */
val MvReaderWidth = 720.dp

/** Material's minimum comfortable tap size; every interactive row clears it. */
val MvTouchTarget = 48.dp

/** Standard control height — above the touch-target floor, sized for a thumb. */
val MvControlHeight = 54.dp

/**
 * The shared left gutter: Browse's direction rule, and the catalogue numbers on every
 * other tab, all measure to this. One gutter across five tabs is what makes the app read
 * as one instrument rather than five screens that happen to share a palette.
 */
val MvGutterWidth = 32.dp

// ── Tokens: ink ──────────────────────────────────────────────────────────────

/**
 * The alpha scale. Four quantised levels replace the fifteen-odd eyeballed values the
 * screens had drifted into. Note the deliberate inversion: the **data** is the brightest
 * thing on a surface and the **label** is the dimmest — several screens currently do the
 * reverse, which is why their numbers read as captions.
 */
object MvAlpha {
    /** Values, figures, record text — anything the archive measured or stored. */
    const val Data = 1.0f
    /** Supporting prose and explanations. */
    const val Body = 0.72f
    /** Labels, captions, gutter ordinals, disabled state. */
    const val Faint = 0.52f
    /** Every rule and every border in the app, without exception. */
    const val Hairline = 0.10f
    /** The fill of a recessed plate — engraved fields, paths. */
    const val Recess = 0.04f
}

/**
 * [MvAlpha] resolved against the current scheme. Both schemes are covered: onSurface is
 * Navy on parchment in light and Parchment on slate in dark, so every level holds.
 *
 * `colorScheme.tertiary` is deliberately absent. In the light scheme it equals secondary
 * (both Slate), so anything that relies on it collapses; in dark it is Gold, so the same
 * elements all ignite at once. Use [Faint] and, if the element needed noticing, an
 * [MvStamp] or an [MvNote] instead of a hue.
 */
object MvInk {
    val Data: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val Body: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = MvAlpha.Body)
    val Faint: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = MvAlpha.Faint)
    val Hairline: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = MvAlpha.Hairline)
    val Recess: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = MvAlpha.Recess)

    /** The one promoted element on a plate. Never two. */
    val Accent: Color
        @Composable get() = MaterialTheme.colorScheme.primary
}

/**
 * The transcript's direction pair. Deliberately NOT primary-versus-secondary: in dark
 * mode the surface IS Slate, so a secondary rule is invisible against the ground it sits
 * on. Outbound takes the accent; inbound takes dimmed onSurface, which is distinct from
 * the accent and from the ground in both schemes.
 *
 * Selection is signalled by geometry (a filled gutter), never by recolouring the rule —
 * recolouring is invisible on whichever direction already owns that colour.
 */
object MvDirection {
    val Outbound: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val Inbound: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
}

// ── Tokens: type ─────────────────────────────────────────────────────────────

/**
 * The monospace roles Type.kt does not carry. Monospace is the voice of the record:
 * measured numbers, timestamps, identifiers, paths, filenames, status tokens and field
 * labels. It is never spent on prose — monospace prose is set-dressing, monospace data
 * is meaning, and spending it on the first destroys the second.
 *
 * Every numeric role enables tabular figures so digits column-align down a list; that
 * hard vertical edge is what makes a column of numbers read as measured rather than
 * promoted. Sizes are in sp and scale with the user's font setting.
 */
object MvType {
    private val Mono_ = FontFamily.Monospace

    /** The one headline figure on a plate. */
    val MonoFigure = TextStyle(
        fontFamily = Mono_,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    )

    /** Field values that carry weight — totals, sizes, the figures a run produced. */
    val MonoValue = TextStyle(
        fontFamily = Mono_,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        fontFeatureSettings = "tnum"
    )

    /** The default data line: stamps, counts, paths, filenames, ids. */
    val Mono = TextStyle(
        fontFamily = Mono_,
        fontSize = 13.sp,
        fontFeatureSettings = "tnum"
    )

    /** Gutter ordinals, captions under figures, secondary stamps. */
    val MonoSmall = TextStyle(
        fontFamily = Mono_,
        fontSize = 11.sp,
        fontFeatureSettings = "tnum"
    )

    /** Uppercase, letter-spaced field and section labels. */
    val Label = TextStyle(
        fontFamily = Mono_,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
}

// ── Tokens: motion ───────────────────────────────────────────────────────────

/**
 * Two durations, linear-ish, opacity only. A display refreshes; a UI performs. This app
 * refreshes. Screens never write a raw `tween(n)` — the previous scatter ran to eight
 * different clocks across three files with no shared meaning.
 */
object MvMotion {
    /** State swaps, crossfades, selection feedback. */
    const val Snap = 120
    /** Content that grows or shrinks; the one entrance. */
    const val Settle = 200

    fun <T> snap(): TweenSpec<T> = tween(durationMillis = Snap, easing = LinearEasing)
    fun <T> settle(): TweenSpec<T> = tween(durationMillis = Settle, easing = LinearOutSlowInEasing)
}

// ── Tokens: iconography ──────────────────────────────────────────────────────

/**
 * The approved glyph set, and the enforcement point for the rule that this app must
 * never look like a messenger. Screens use these names; screens do not reach into
 * `Icons.Outlined` themselves — that is exactly how Forum, the two-speech-bubble
 * conversation glyph, ended up as the largest graphic on both Home and Export.
 *
 * Absent by design and permanently banned: Forum, Sms, Chat, ChatBubble, Message, Send,
 * Reply, Call, Person, PersonAdd, and the rest of Material's messaging and social set.
 *
 * Glyphs are rationed. Prefer a typographic mark — the literal string "SMS" set in
 * [MvType.Mono] — over an icon, and never mount a glyph on a rounded tinted tile: that
 * shape is an app icon or a contact avatar.
 */
object MvIcons {
    /** A record, a document, a transcript. Replaces Forum wherever it meant "messages". */
    val Records: ImageVector = Icons.AutoMirrored.Outlined.Article
    /** An index, a manifest, a list of records. */
    val Index: ImageVector = Icons.AutoMirrored.Outlined.ListAlt
    /** An enclosure. */
    val Attachment: ImageVector = Icons.Outlined.AttachFile
    /** Where the corpus physically lives. */
    val Location: ImageVector = Icons.Outlined.FolderOpen
    /** The vault's holdings. */
    val Inventory: ImageVector = Icons.Outlined.Inventory2
    /** A query over a fixed corpus. */
    val Search: ImageVector = Icons.Outlined.Search
    /** Copy a value out. */
    val Copy: ImageVector = Icons.Outlined.ContentCopy
    /** Extract records from the corpus. Deliberately not a "share" glyph. */
    val Extract: ImageVector = Icons.Outlined.SaveAlt
    /** Destroy a record. */
    val Delete: ImageVector = Icons.Outlined.DeleteOutline
    /** Configuration. */
    val Settings: ImageVector = Icons.Outlined.Tune
    /** Vault lock state. */
    val Lock: ImageVector = Icons.Outlined.Lock
    /** A verified / complete condition. */
    val Verified: ImageVector = Icons.Outlined.VerifiedUser
    /** An inclusion mark. Used inside [MvSelectMark]; not for general decoration. */
    val Mark: ImageVector = Icons.Filled.Check
    /** Return to the enclosing index. Auto-mirrored for RTL. */
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    /** Abandon a transient mode — a selection, a filter. Never "close the app". */
    val Dismiss: ImageVector = Icons.Filled.Close
    /** Hand off to another app. The archive resolves a fact; it does not act on it. */
    val External: ImageVector = Icons.AutoMirrored.Outlined.OpenInNew
    /** Open the destination drawer. */
    val Menu: ImageVector = Icons.Filled.Menu
    /** The three theme conditions, for the drawer's tap-through. */
    val ThemeAuto: ImageVector = Icons.Outlined.SettingsBrightness
    val ThemeLight: ImageVector = Icons.Outlined.LightMode
    val ThemeDark: ImageVector = Icons.Outlined.DarkMode
}

// ── Formatters ───────────────────────────────────────────────────────────────

/**
 * Grouped digits, always. `1,204` and `1204` must never appear in the same card — the
 * register cannot be inconsistent in its own hand.
 */
fun MvNum(n: Long): String = String.format(Locale.US, "%,d", n)

/** Convenience overload for the many Int counts the engine reports. */
fun MvNum(n: Int): String = MvNum(n.toLong())

/** Zero-padded column ordinal: `MvOrdinal(7)` -> `007`. Fixed width locks the column. */
fun MvOrdinal(n: Int, width: Int = 3): String = String.format(Locale.US, "%0${width}d", n)

/**
 * A byte figure the way a measurement block reports one: the humanised value and, beside
 * it, the raw count — `4.2 MB  (4,414,983 B)`.
 *
 * Reserved for aggregates and measurements. A single record's size stays at
 * `Format.bytes` alone, or every ledger row grows a parenthetical.
 */
fun MvBytes(n: Long): String = "${Format.bytes(n)}  (${MvNum(n)} B)"

/**
 * The clock half of the shared `yyyy-MM-dd HH:mm:ss` stamp — `14:32:07`. Every
 * [MvInlineAck] in the app is stamped with this and nothing else; three tabs had each
 * grown a private copy of it, and one of them printed the full date instead.
 */
fun MvClock(): String = Format.timestamp(System.currentTimeMillis()).takeLast(8)

/**
 * How long an [MvInlineAck] stands before the screen clears it. One value app-wide: an
 * acknowledgement that persists forever on one tab and expires on another is two
 * different controls wearing the same face.
 */
const val MvAckHoldMs = 6_000L

// ── Surfaces ─────────────────────────────────────────────────────────────────

/**
 * The branded surface card — the soft outer container, and the only soft shape in the
 * app. One shape, zero elevation, one padding, one width cap, plus animateContentSize so
 * a card that gains or loses a line eases instead of snapping.
 *
 * No shadow, ever. A Material card rasterises the same rounded rect three times —
 * shadow, background fill, then clip — and with a surface this close in value to the page
 * behind it those antialiased edges do not land on identical pixels, leaving a hairline
 * ring traced inside every corner. A 2dp shadow was never going to read as lift at this
 * contrast anyway; the ring was all that survived. A hairline border rasterises once,
 * lands crisply at any radius, and separates the card honestly.
 *
 * @param onClick when non-null the whole card becomes one large tap target. The
 *   [contentDescription] becomes the click label — which is why a card never needs to
 *   print a "Tap to …" instruction line for an assistive user or anyone else.
 * @param spacing gap between children. Field lists override this to [MvSpace.Row]; the
 *   12dp default is for groups, and a field list set at 12dp reads as a card feed.
 */
@Composable
fun MvCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    contentPadding: Dp = MvSpace.Card,
    spacing: Dp = MvSpace.Item,
    content: @Composable () -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .widthIn(max = MvContentWidth)
        .animateContentSize(MvMotion.settle())
    Card(
        shape = MvShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MvInk.Hairline),
        modifier = if (onClick != null) {
            base.clickable(onClickLabel = contentDescription, onClick = onClick)
        } else {
            base
        }
    ) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) { content() }
    }
}

/**
 * The crisp data surface: near-square, hairline-bordered, no elevation, no gradient, and
 * — deliberately — **no built-in padding**, because a plate's job is to contain divided
 * rows that run edge to edge. Pad the rows, not the plate.
 *
 * This is the counterpart to [MvCard]. Cards group a section; plates carry data. An
 * option manifest, a run receipt, a stat readout, a path field and a selection bar are
 * all plates.
 *
 * @param recessed fills at [MvAlpha.Recess] so the plate reads as engraved into the card
 *   rather than laid on top of it. Use for paths and other fields of record.
 */
@Composable
fun MvPlate(
    modifier: Modifier = Modifier,
    recessed: Boolean = false,
    bordered: Boolean = true,
    shape: Shape = MvShape.Plate,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var m = modifier.fillMaxWidth().clip(shape)
    if (recessed) m = m.background(MvInk.Recess)
    if (bordered) m = m.border(1.dp, MvInk.Hairline, shape)
    if (onClick != null) m = m.clickable(onClickLabel = contentDescription, onClick = onClick)
    Column(m, content = content)
}

/**
 * The overwhelmingly common use of [MvPlate]: a stack of [MvFieldRow]s. Because MvPlate
 * carries no padding, every screen that wanted one wrote `MvPlate { Column(padding…) }`
 * itself — and four screens picked four different insets, so the ledger rows on one tab
 * did not sit on the same margins as the ledger rows on the next. This is that column,
 * once.
 *
 * Horizontal [MvSpace.Plate] so the rules stop short of the plate border; vertical
 * [MvSpace.Row] so the first and last rows are not welded to it.
 */
@Composable
fun MvFieldPlate(
    modifier: Modifier = Modifier,
    recessed: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    MvPlate(modifier, recessed = recessed) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MvSpace.Plate, vertical = MvSpace.Row),
            content = content
        )
    }
}

/**
 * A square hairline frame for identity marks and contact photos. The circle — and the
 * white gloss pass that goes with it — is the second-most recognisable messenger
 * signature after the bubble. A square specimen plate is how an archive depicts a
 * subject.
 */
@Composable
fun MvIdentityFrame(
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .size(size)
            .clip(MvShape.Mark)
            .border(1.dp, MvInk.Hairline, MvShape.Mark),
        content = content
    )
}

// ── Rules ────────────────────────────────────────────────────────────────────

/**
 * The canonical hairline — 1dp at [MvAlpha.Hairline], matching every card and plate
 * border exactly. Rules are what separate a register from a feed: a list of records is
 * ruled, not spaced.
 *
 * @param inset start inset, so a divider aligns to the text column and lets a gutter
 *   (a direction rule, a catalogue-number column) run visually uninterrupted down the
 *   whole page. Pass [MvGutterWidth] wherever a gutter is in play.
 */
@Composable
fun MvRule(modifier: Modifier = Modifier, inset: Dp = 0.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = inset)
            .height(1.dp)
            .background(MvInk.Hairline)
    )
}

/** The same hairline, vertical — for dividing the cells of a plate. */
@Composable
fun MvVerticalRule(modifier: Modifier = Modifier) {
    Box(modifier.width(1.dp).fillMaxHeight().background(MvInk.Hairline))
}

/**
 * The section label: monospace, uppercase, letter-spaced, optionally carrying a
 * catalogue ordinal and a rule running to the trailing edge — which is what turns a
 * caption into a plate header. Marked as a heading so TalkBack can jump between sections.
 *
 * @param ordinal when set, renders as `01 — STORAGE`. Numbering is what separates a
 *   catalogue from a settings menu.
 * @param rule draws the hairline from the label to the trailing edge.
 * @param trailing an optional slot at the trailing edge — a status stamp, a chevron —
 *   which is how a tappable plate signals itself instead of printing an instruction.
 */
@Composable
fun MvSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    ordinal: Int? = null,
    rule: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    val label = if (ordinal != null) "${MvOrdinal(ordinal, 2)} — ${text.uppercase(Locale.US)}" else text
    Row(
        modifier.fillMaxWidth().semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MvType.Label, color = MvInk.Faint)
        if (rule) {
            Box(
                Modifier
                    .weight(1f)
                    .padding(start = MvSpace.Inline, end = if (trailing != null) MvSpace.Inline else 0.dp)
                    .height(1.dp)
                    .background(MvInk.Hairline)
            )
        } else if (trailing != null) {
            Box(Modifier.weight(1f))
        }
        trailing?.invoke()
    }
}

/**
 * The full-width ruled date band that replaces the centred rounded date capsule. A
 * floating lozenge between messages is the messenger date divider; a ledger breaks days
 * with a rule flush to the left margin and the date riding it.
 */
@Composable
fun MvDayRule(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = MvSpace.Row),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(Locale.US), style = MvType.Label, color = MvInk.Faint)
        Box(
            Modifier
                .weight(1f)
                .padding(start = MvSpace.Inline)
                .height(1.dp)
                .background(MvInk.Hairline)
        )
    }
}

/**
 * A hairline-ruled action strip flush inside a card. Replaces the row of three
 * equal-weight rounded pills, which is a photo-app toolbar and which gave permanent
 * destruction the same visual weight and the same shape as copying a string.
 */
@Composable
fun MvCardFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        MvRule()
        Row(
            Modifier.fillMaxWidth().heightIn(min = MvTouchTarget),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// ── Data ─────────────────────────────────────────────────────────────────────

/** The single monospace text treatment. Never re-declare FontFamily.Monospace at a call site. */
@Composable
fun MvMono(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MvType.Mono,
    color: Color = MvInk.Data,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

/**
 * The ledger row, and the highest-leverage primitive in this file: uppercase label left
 * at [MvAlpha.Faint], value right-aligned in monospace with tabular figures at
 * [MvAlpha.Data], hairline beneath.
 *
 * Every field row in the app aligns its value to the same right edge. That hard vertical
 * edge down a plate is the strongest single cue that a surface is a ledger, and it is
 * what four fields concatenated into `"SMS 42 · MMS 7 · 12.4s"` can never be. Four fields
 * means four rows.
 *
 * @param accent promotes the value to [MvInk.Accent]. Exactly one per plate, at most.
 */
@Composable
fun MvFieldRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MvType.Mono,
    rule: Boolean = true,
    accent: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = MvSpace.Row),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label.uppercase(Locale.US),
                style = MvType.Label,
                color = MvInk.Faint,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                style = valueStyle,
                color = if (accent) MvInk.Accent else MvInk.Data,
                textAlign = TextAlign.End
            )
            if (trailing != null) {
                Box(Modifier.padding(start = MvSpace.Inline)) { trailing() }
            }
        }
        if (rule) MvRule()
    }
}

/** One cell of an [MvStatPlate]: a monospace figure under a monospace uppercase caption. */
data class MvStatCell(val caption: String, val value: String)

/**
 * The replacement for [MvStatPill]: a flat plate of figures divided by vertical
 * hairlines, all sharing a baseline and a column edge. No gradient, no lozenge radius,
 * no ghost glyph growing in behind the number.
 *
 * Gradient-filled rounded stat cards side by side is the house style of every consumer
 * fitness/fintech/social dashboard. An archive's counts belong on a ruled plate.
 *
 * @param accentIndex which single cell gets the accent colour. -1 for none.
 */
@Composable
fun MvStatPlate(
    cells: List<MvStatCell>,
    modifier: Modifier = Modifier,
    accentIndex: Int = 0
) {
    MvPlate(modifier) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            cells.forEachIndexed { i, cell ->
                if (i > 0) MvVerticalRule()
                Column(
                    Modifier.weight(1f).padding(MvSpace.Plate),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(cell.caption.uppercase(Locale.US), style = MvType.Label, color = MvInk.Faint)
                    Text(
                        cell.value,
                        style = MvType.MonoFigure,
                        color = if (i == accentIndex) MvInk.Accent else MvInk.Data
                    )
                }
            }
        }
    }
}

/** A bare counted figure with its uppercase caption — the rectilinear alternative to a stat pill. */
@Composable
fun MvFigure(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    style: TextStyle = MvType.MonoFigure
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(caption.uppercase(Locale.US), style = MvType.Label, color = MvInk.Faint)
        Text(value, style = style, color = if (accent) MvInk.Accent else MvInk.Data)
    }
}

/**
 * The catalogue/accession treatment: monospace, tracked, faint. For run slugs
 * (`20260720_150412`), thread ids (`TH-00417`) and record ordinals.
 *
 * Every accession in this app already has an identifier; hiding it and showing a friendly
 * restatement instead is what makes a vault read as an app rather than a registry.
 */
@Composable
fun MvCatalogId(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MvType.MonoSmall.copy(letterSpacing = 1.sp),
        color = MvInk.Faint
    )
}

/**
 * Where the corpus physically lives — the most archival fact the app knows, and
 * currently the faintest text on two screens. Full ink, monospace, selectable, so the
 * user can verify it rather than trust a toast about it.
 */
@Composable
fun MvPathText(
    path: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    SelectionContainer(modifier) {
        Text(
            path,
            style = MvType.Mono,
            color = MvInk.Data,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    }
}

/**
 * Where something lives on disk, as one figure of record: a labelled section rule over a
 * recessed plate holding the selectable path. Four screens print a path and four had
 * spelled the label four ways — a section label, a bare Text, an MvMono inside the plate,
 * and a caption. One concept, one treatment, on every tab.
 *
 * @param label the noun. LOCATION unless the screen genuinely means something narrower,
 *   e.g. the destination a finished run wrote to.
 */
@Composable
fun MvLocationPlate(
    path: String,
    modifier: Modifier = Modifier,
    label: String = "LOCATION",
    singleLine: Boolean = false
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MvSpace.Row)) {
        MvSectionLabel(label.uppercase(Locale.US))
        MvPlate(recessed = true) {
            MvPathText(path, Modifier.padding(MvSpace.Plate), singleLine = singleLine)
        }
    }
}

// ── Status without colour ────────────────────────────────────────────────────

/**
 * Stamp tones. Each is defined so it survives BOTH schemes — no tone leans on
 * `tertiary`, and [Flagged] carries its meaning in the outline and the word rather than
 * in a text colour, because gold text on parchment is unreadable and slate text on slate
 * is invisible.
 */
enum class MvTone {
    /** The ordinary condition of a record: COMPLETE, INDEXED, IDLE. */
    Neutral,

    /** The live condition: RUNNING, SELECTED. */
    Active,

    /** Degraded but not broken: PARTIAL, STALE, NO INDEX. Gold outline, ordinary ink. */
    Flagged,

    /** A genuine failure. The only tone that spends crimson. */
    Failed
}

/**
 * A rectangular, hairline-outlined, monospace-uppercase status mark — explicitly not a
 * rounded filled chip. Choosing the stamp over the chip is most of the difference
 * between a records viewer and a social app.
 *
 * Stamp **every** record's condition, not only the failures. Silence-means-good is an app
 * convention; a register states the condition of every entry, in a scannable column. The
 * word is also why state never depends on colour alone here.
 */
@Composable
fun MvStamp(
    text: String,
    modifier: Modifier = Modifier,
    tone: MvTone = MvTone.Neutral
) {
    val outline = when (tone) {
        MvTone.Neutral -> MvInk.Faint
        MvTone.Active -> MaterialTheme.colorScheme.primary
        MvTone.Flagged -> Gold
        MvTone.Failed -> MaterialTheme.colorScheme.error
    }
    val ink = when (tone) {
        MvTone.Neutral -> MvInk.Faint
        MvTone.Active -> MaterialTheme.colorScheme.primary
        // Ordinary ink, not gold: gold reads as an outline in both schemes but fails as
        // text on parchment. The outline carries the flag; the word carries the meaning.
        MvTone.Flagged -> MvInk.Data
        MvTone.Failed -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier
            .clip(MvShape.Mark)
            .border(1.dp, outline, MvShape.Mark)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text.uppercase(Locale.US), style = MvType.Label, color = ink)
    }
}

/**
 * The one advisory treatment: a 2dp gold left rule with a monospace NOTE prefix and
 * ordinary ink. Replaces every `colorScheme.tertiary` warning in the app, which vanished
 * into the section labels in light mode and ignited alongside them in dark.
 *
 * A marginal rule is a document convention; a tinted sentence is an app convention. And
 * it keeps crimson reserved for genuine failure.
 */
@Composable
fun MvNote(
    text: String,
    modifier: Modifier = Modifier,
    label: String = "NOTE"
) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(2.dp).fillMaxHeight().background(Gold))
        Column(
            Modifier.padding(start = MvSpace.Inline),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label.uppercase(Locale.US), style = MvType.Label, color = MvInk.Faint)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MvInk.Body)
        }
    }
}

/**
 * [MvNote]'s crimson counterpart, and the in-card form of [MvErrorState]: a 2dp error
 * rule, a monospace condition label, and the explanation in ordinary ink. For a genuine
 * failure that belongs *inside* a card rather than in place of one — a run that stopped, a
 * measurement that could not be taken.
 *
 * This is the only crimson a screen may spend besides [MvTone.Failed] and MvErrorState.
 * A bare crimson sentence is not an alternative to it: the marginal rule is what makes the
 * failure a marked record rather than a shout, and it is why the message itself stays
 * legible in both schemes.
 */
@Composable
fun MvFailureNote(
    text: String,
    modifier: Modifier = Modifier,
    label: String = "FAILED"
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) { heading() }
    ) {
        Box(Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.error))
        Column(
            Modifier.padding(start = MvSpace.Inline),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                label.uppercase(Locale.US),
                style = MvType.Label,
                color = MaterialTheme.colorScheme.error
            )
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MvInk.Body)
        }
    }
}

/**
 * The Toast replacement: a timestamped monospace acknowledgement line that appears inside
 * the card that produced the action. A vault records; it does not flash. Carries a live
 * region so replacing Toasts does not silence TalkBack.
 *
 * Renders nothing when [message] is null — the caller owns the timing.
 */
@Composable
fun MvInlineAck(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        message,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MvType.MonoSmall,
        color = MvInk.Faint
    )
}

// ── Controls ─────────────────────────────────────────────────────────────────

/**
 * The fixed-width state cell: a near-square hairline box holding one monospace uppercase
 * word. Decorative to the semantics tree — the row that owns it carries the state.
 */
@Composable
fun MvStateCell(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 92.dp
) {
    val outline = if (active) MaterialTheme.colorScheme.primary else MvInk.Hairline
    val ink = if (active) MaterialTheme.colorScheme.primary else MvInk.Faint
    Box(
        modifier
            .width(width)
            .clip(MvShape.Mark)
            .border(1.dp, outline, MvShape.Mark)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text.uppercase(Locale.US), style = MvType.Label, color = ink)
    }
}

/**
 * The Switch replacement. The rounded track-and-thumb pill is the single most
 * recognisable consumer-mobile control there is, and nine of them stacked is a
 * messenger's settings page. Here the row states a condition instead: label left,
 * fixed-width [MvStateCell] right.
 *
 * Semantics are unchanged from the Switch it replaces — merged row, `Role.Switch`, the
 * 48dp floor, the same click target — and the state becomes readable as text down a
 * column, which is strictly better than decoding six pill positions and does not depend
 * on colour.
 *
 * @param onText / [offText] the vocabulary. Settings says ENABLED/DISABLED; the export
 *   manifest says INCLUDED/OMITTED.
 * @param unavailableText shown instead when [enabled] is false — e.g. REFUSED where the
 *   device has no credential to lock against.
 */
@Composable
fun MvStateToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    onText: String = "ENABLED",
    offText: String = "DISABLED",
    unavailableText: String? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = MvTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .semantics(mergeDescendants = true) {}
            .padding(vertical = MvSpace.Row),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(Modifier.width(MvGutterWidth), contentAlignment = Alignment.CenterStart) { leading() }
        }
        Column(Modifier.weight(1f).padding(end = MvSpace.Inline)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MvInk.Data else MvInk.Faint
            )
            if (supporting != null) {
                Text(supporting, style = MvType.MonoSmall, color = MvInk.Faint)
            }
        }
        MvStateCell(
            text = when {
                !enabled && unavailableText != null -> unavailableText
                checked -> onText
                else -> offText
            },
            active = checked && enabled
        )
    }
}

/**
 * The squared inclusion mark for a manifest row. An archive marks items for inclusion; it
 * does not flip live switches. Decorative to semantics — the row owns the state.
 */
@Composable
fun MvSelectMark(
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier
            .size(size)
            .clip(MvShape.Mark)
            .background(if (selected) accent else Color.Transparent)
            .border(1.dp, if (selected) accent else MvInk.Hairline, MvShape.Mark),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                MvIcons.Mark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size - 6.dp)
            )
        }
    }
}

/**
 * A hairline segmented selector with near-square cells and a **solid** active fill —
 * not Material's pill-capped segmented row with a 16% translucent wash, which reads as a
 * mode switcher in a photo app and whose contrast behaves differently in each scheme.
 */
@Composable
fun MvModeStrip(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier
            .fillMaxWidth()
            .height(MvTouchTarget)
            .clip(MvShape.Plate)
            .border(1.dp, MvInk.Hairline, MvShape.Plate)
    ) {
        options.forEachIndexed { i, option ->
            if (i > 0) MvVerticalRule()
            val active = i == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (active) accent else Color.Transparent)
                    .selectable(
                        selected = active,
                        role = Role.RadioButton,
                        onClick = { onSelect(i) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option.uppercase(Locale.US),
                    style = MvType.Label,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MvInk.Body
                )
            }
        }
    }
}

/**
 * The archival input: squared geometry, an external monospace field label rather than a
 * floating Material one, monospace input, and a right-side readout slot for the corpus
 * scope or the hit count — so the count stops being a line of pluralised prose
 * underneath.
 */
@Composable
fun MvQueryField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "QUERY",
    placeholder: String? = null,
    readout: String? = null
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MvSpace.Row)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(Locale.US),
                style = MvType.Label,
                color = MvInk.Faint,
                modifier = Modifier.weight(1f)
            )
            if (readout != null) {
                Text(readout, style = MvType.MonoSmall, color = MvInk.Faint)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = MvShape.Plate,
            textStyle = MvType.Mono,
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MvType.Mono, color = MvInk.Faint) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = MvTouchTarget)
        )
    }
}

/** Full-width filled action. [icon] is decorative — the label already names the action. */
@Composable
fun MvPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = MvControlHeight),
        shape = MvShape.Control,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Box(Modifier.size(width = 10.dp, height = 1.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Outlined companion to [MvPrimaryButton], at the same rhythm one step quieter. */
@Composable
fun MvSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = MvTouchTarget),
        shape = MvShape.Control,
        // Material's default 24dp each side costs 48dp of a button that is often half the
        // screen. Paired one-word actions ("BROWSE", "HISTORY") were left too narrow for
        // their own label and broke MID-WORD — "BROWS / E". Wrapping a long label at a
        // space is graceful; splitting a single word is a defect.
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Box(Modifier.size(width = 8.dp, height = 1.dp))
        }
        // Still not maxLines = 1: a genuinely long label ("Clear cached exports") must be
        // free to wrap at its space rather than be silently clipped.
        Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/**
 * The quiet tier below [MvSecondaryButton]: a text-only 48dp action with no pill and no
 * border, for footer strips and inline actions. The [destructive] variant carries crimson
 * **ink only** — no fill, no outline — and belongs separated from its neighbours rather
 * than given equal weight beside them.
 */
@Composable
fun MvTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = MvShape.Mark,
        modifier = modifier.defaultMinSize(minHeight = MvTouchTarget)
    ) {
        Text(
            text.uppercase(Locale.US),
            style = MvType.Label,
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

// ── Readouts ─────────────────────────────────────────────────────────────────

/**
 * The instrument readout: squared ends, hairline ticks at the quarters, and — when
 * [progress] is null — a narrow travelling graduation rather than a full-width gradient
 * wash sweeping forever.
 *
 * A 10dp fully-rounded capsule is the download idiom of every consumer app, and an
 * endless gold shimmer is the skeleton-loader idiom borrowed from social feeds. Both also
 * spend the accent continuously, so nothing else on the surface can use it for emphasis.
 */
@Composable
fun MvMeter(
    progress: Float?,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    contentDescription: String? = null
) {
    val track = MvInk.Hairline
    val fill = MaterialTheme.colorScheme.primary
    val tick = MvInk.Faint
    val cd = contentDescription
    val base = modifier
        .fillMaxWidth()
        .height(height)
        .then(if (cd != null) Modifier.semantics { this.contentDescription = cd } else Modifier)

    if (progress != null) {
        val p = progress.coerceIn(0f, 1f)
        Canvas(base) {
            drawRect(color = track)
            drawRect(color = fill, size = Size(size.width * p, size.height))
            val t = 1.dp.toPx()
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawRect(color = tick, topLeft = Offset(size.width * f, 0f), size = Size(t, size.height))
            }
        }
    } else {
        val transition = rememberInfiniteTransition(label = "mvMeter")
        val sweep by transition.animateFloat(
            initialValue = -0.28f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "mvMeterSweep"
        )
        Canvas(base) {
            drawRect(color = track)
            drawRect(
                color = fill,
                topLeft = Offset(size.width * sweep, 0f),
                size = Size(size.width * 0.28f, size.height)
            )
            val t = 1.dp.toPx()
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawRect(color = tick, topLeft = Offset(size.width * f, 0f), size = Size(t, size.height))
            }
        }
    }
}

/**
 * The in-card measuring treatment: a monospace label over a hairline sweep. A rotating
 * ring reads as an app loading; a sweeping hairline reads as an instrument taking a
 * reading.
 */
@Composable
fun MvMeasuring(label: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MvSpace.Row)) {
        Text(label.uppercase(Locale.US), style = MvType.Label, color = MvInk.Faint)
        MvMeter(progress = null, height = 2.dp, contentDescription = label)
    }
}

/**
 * The app's one entrance: a short opacity-only fade, no translation, no stagger, applied
 * once to a whole region. Records do not arrive — they were already there and you are
 * looking at them. Per-item staggered rise is social-timeline choreography.
 */
@Composable
fun MvReveal(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = MvMotion.settle(),
        label = "mvReveal"
    )
    Box(modifier.graphicsLayer { this.alpha = alpha }) { content() }
}

// ── Dialogs ──────────────────────────────────────────────────────────────────

/**
 * The house confirmation. The moment of destruction is exactly when the app should look
 * most like an instrument, so the target's facts are laid out as an [MvFieldRow] manifest
 * rather than dissolved into a paragraph — an itemised statement reads as more honest
 * than the same facts in a sentence, and it aligns on the same grid as the record it is
 * about to destroy.
 *
 * @param fields the manifest: label to value, in the order an archivist would read them.
 * @param consequence one short line of plain language. This is prose, so it is sans.
 * @param busy locks the confirm action while the operation is in flight.
 */
@Composable
fun MvConfirmDialog(
    title: String,
    fields: List<Pair<String, String>>,
    consequence: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "CANCEL",
    destructive: Boolean = true,
    busy: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        shape = MvShape.Plate,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                title.uppercase(Locale.US),
                style = MvType.Label,
                color = MvInk.Faint,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MvSpace.Row)) {
                fields.forEachIndexed { i, (label, value) ->
                    MvFieldRow(label, value, rule = i < fields.lastIndex)
                }
                Text(
                    consequence,
                    style = MaterialTheme.typography.bodySmall,
                    color = MvInk.Body,
                    modifier = Modifier.padding(top = MvSpace.Row)
                )
            }
        },
        confirmButton = {
            MvTextAction(confirmLabel, onConfirm, enabled = !busy, destructive = destructive)
        },
        dismissButton = {
            MvTextAction(dismissLabel, onDismiss, enabled = !busy)
        }
    )
}

// ── The three non-content states ─────────────────────────────────────────────

/**
 * Work in progress. Always labelled — a bare indicator tells a screen reader nothing.
 * The spinner is gone: a rotating ring is generic mobile feedback, a hairline sweep is an
 * instrument taking a reading.
 *
 * Copy convention: name the operation and the corpus ("Reading index"), not the user's
 * feelings about it ("Reading your archives…").
 */
@Composable
fun MvLoadingState(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(top = 56.dp, start = MvSpace.ScreenH, end = MvSpace.ScreenH),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Item)
    ) {
        Box(Modifier.widthIn(max = 200.dp)) { MvMeasuring(label) }
    }
}

/**
 * Nothing here yet — and that is fine. Reads as a deliberate resting state with a way
 * forward, not as a screen that failed to load. The title is set as a plate line, not as
 * a greeting: "NO ACCESSIONS ON RECORD", never "Welcome to …".
 */
@Composable
fun MvEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    MvCard(modifier) {
        Text(
            title,
            style = MvType.MonoValue,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        MvRule()
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MvInk.Body
        )
        action?.invoke()
    }
}

/**
 * Something genuinely went wrong. The one place in the app where crimson is correct —
 * carried by the title and a left rule, never as a full crimson fill that would shout.
 */
@Composable
fun MvErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Card(
        shape = MvShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // Same treatment as MvCard: no shadow, one crisp hairline. See MvCard.
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MvInk.Hairline),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = MvContentWidth)
            .animateContentSize(MvMotion.settle())
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.error))
            Column(
                Modifier.padding(MvSpace.Card),
                verticalArrangement = Arrangement.spacedBy(MvSpace.Item)
            ) {
                Text(
                    title.uppercase(Locale.US),
                    style = MvType.MonoValue,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MvInk.Body
                )
                action?.invoke()
            }
        }
    }
}

/** Centers a single element in the remaining space (readouts, one-line states). */
@Composable
fun MvCentered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxSize().padding(MvSpace.ScreenH),
        contentAlignment = Alignment.Center
    ) { content() }
}

// ── Deprecated by the charter ────────────────────────────────────────────────

/**
 * A headline number with its caption, on a gradient wash.
 *
 * **Deprecated by STYLE.md — use [MvStatPlate].** Gradient-filled rounded stat cards side
 * by side are the house style of every consumer dashboard, and the 650ms glyph growing in
 * behind the figure is expressive motion on a surface that should not perform. Kept only
 * so pre-charter call sites keep compiling while the screens are converted; do not write
 * new ones.
 */
@Composable
fun MvStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    motif: ImageVector? = null
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val motifAlpha by animateFloatAsState(
        targetValue = if (shown) 0.18f else 0f,
        animationSpec = MvMotion.settle(),
        label = "motifAlpha"
    )

    Box(
        modifier
            .clip(MvShape.Plate)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        Gold.copy(alpha = 0.09f)
                    )
                )
            )
    ) {
        if (motif != null) {
            Icon(
                motif,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp)
                    .size(54.dp)
                    .graphicsLayer {
                        alpha = motifAlpha
                        transformOrigin = TransformOrigin(1f, 0f)
                    }
            )
        }
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MvType.MonoFigure, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MvType.Label, color = MvInk.Faint)
        }
    }
}
