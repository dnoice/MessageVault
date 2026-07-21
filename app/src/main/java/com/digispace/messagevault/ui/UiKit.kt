/*
 * ✒ Metadata
 *     - Title: UI Kit (Message Vault Edition - v1.0)
 *     - File Name: UiKit.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/UiKit.kt
 *     - Artifact Type: library
 *     - Version: 1.0.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial: one home for the spacing / radius / width tokens and the card, section-label, stat-pill, button, and empty/loading/error state primitives that the five screens had each re-declared privately and slowly drifted apart on.
 *
 * ✒ Description:
 *     The house style, expressed once. Every destination used to carry its own private
 *     CardShape, GroupLabel, StatPill and PrimaryButton; the copies drifted (20dp vs
 *     22dp card padding, 14dp vs 16dp gaps, three different empty-state treatments) and
 *     the app read as five screens rather than one product. This file holds the tokens
 *     and the small set of primitives they compose into, so a rhythm change lands in one
 *     place. It adds no behaviour — every primitive here replaces a private twin.
 *
 * ✒ Key Features:
 *     - MvSpace / MvShape / width tokens: the spacing scale, corner radii, and the content and reader width caps used app-wide.
 *     - MvCard: the one branded surface card — shape, elevation, padding, width cap, animateContentSize, and an optional whole-card tap.
 *     - MvSectionLabel: the single section-label treatment (tertiary, labelMedium), marked as a heading for screen readers.
 *     - MvStatPill / MvPrimaryButton / MvSecondaryButton: the shared headline-number and action treatments, all at or above the 48dp touch-target floor.
 *     - MvLoadingState / MvEmptyState / MvErrorState: the three non-content states given one intentional look; the error tone is the only place crimson appears.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (foundation, material3, animation, ui.semantics).
 *     - Palette discipline: crimson arrives only through MvErrorState / MaterialTheme.colorScheme.error — never as decoration.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.digispace.messagevault.ui.theme.Gold
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── Tokens ───────────────────────────────────────────────────────────────────

/** The vertical/horizontal rhythm. Screens compose from these, never from raw dp. */
object MvSpace {
    /** Gutter between screen content and the screen edge. */
    val ScreenH = 24.dp
    /** Top/bottom breathing room inside a scrolling screen. */
    val ScreenV = 22.dp
    /** Gap between sibling cards. */
    val Section = 16.dp
    /** Padding inside a card. */
    val Card = 22.dp
    /** Gap between elements inside a card. */
    val Item = 12.dp
    /** Gap between side-by-side controls. */
    val Inline = 10.dp
}

/** Corner geometry. Cards are soft; controls and pills are one step tighter. */
object MvShape {
    val Card = RoundedCornerShape(22.dp)
    val Control = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(16.dp)
}

/** Cards stop growing here so they read as cards on the Fold's inner screen. */
val MvContentWidth = 560.dp

/** Reading surfaces (conversation lists, threads) may run wider than a card. */
val MvReaderWidth = 720.dp

/** Material's minimum comfortable tap size; every interactive row clears it. */
val MvTouchTarget = 48.dp

/** Standard control height — above the touch-target floor, sized for a thumb. */
val MvControlHeight = 54.dp

// ── Primitives ───────────────────────────────────────────────────────────────

/**
 * The branded surface card. One shape, one elevation, one padding, one width cap —
 * plus animateContentSize so a card that gains or loses a line eases instead of snapping.
 *
 * @param onClick when non-null the whole card becomes one large tap target.
 */
@Composable
fun MvCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .widthIn(max = MvContentWidth)
        .animateContentSize(tween(220))
    Card(
        shape = MvShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // No shadow. A Material card rasterises the same rounded rect three times —
        // shadow, background fill, then clip — and with a surface this close in value to
        // the page behind it those antialiased edges do not land on identical pixels,
        // leaving a hairline ring traced inside every corner. A 2dp shadow was never
        // going to read as lift at this contrast anyway; the ring was all that survived.
        // A hairline border rasterises once, lands crisply at any radius, and separates
        // the card honestly.
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
        modifier = if (onClick != null) {
            base.clickable(onClickLabel = contentDescription, onClick = onClick)
        } else {
            base
        }
    ) {
        Column(
            Modifier.padding(MvSpace.Card),
            verticalArrangement = Arrangement.spacedBy(MvSpace.Item)
        ) { content() }
    }
}

/**
 * The single section-label treatment: short, tertiary, letter-spaced. Marked as a
 * heading so TalkBack can jump between a screen's sections.
 */
@Composable
fun MvSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier.semantics { heading() }
    )
}

/** A headline number with its caption, on a tinted primary wash. */
@Composable
fun MvStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    motif: ImageVector? = null
) {
    // The motif eases in rather than being painted with the pill: arriving a beat after
    // the number lets the figure land first and the decoration follow, which reads as
    // deliberate. It grows from its own bottom-right corner so it feels like it is
    // settling into place instead of being scaled from the middle of the card.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val motifAlpha by animateFloatAsState(
        // Faint enough to stay decoration, strong enough that the glyph is identifiable
        // rather than a smudge — it sits in its own corner now, not under the label.
        targetValue = if (shown) 0.18f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = 120),
        label = "motifAlpha"
    )
    val motifScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.82f,
        animationSpec = tween(durationMillis = 650, delayMillis = 120),
        label = "motifScale"
    )

    Box(
        modifier
            .clip(MvShape.Pill)
            // A cool-to-warm wash rather than one flat tint: primary into gold, top-left
            // to bottom-right, so the pill has somewhere to travel. The alpha falls as
            // well as the hue turning, which keeps the gradient readable in dark mode —
            // there primary IS gold, so hue alone would have nothing to say.
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
            // Top-right, not bottom-right. The label ("ATTACHMENTS") runs nearly the full
            // width of the pill, so the bottom corner is the one place the glyph cannot go
            // without being cropped into an unreadable fragment. The value above it is
            // short, which leaves this corner genuinely free — the whole shape reads.
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
                        scaleX = motifScale
                        scaleY = motifScale
                        transformOrigin = TransformOrigin(1f, 0f)
                    }
            )
        }
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
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
        shape = MvShape.Control
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Box(Modifier.size(width = 8.dp, height = 1.dp))
        }
        // Deliberately not maxLines = 1: a long label ("Clear cached exports") must wrap
        // on a narrow phone rather than be silently clipped.
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

// ── The three non-content states ─────────────────────────────────────────────

/** Work in progress. Always labelled — a bare spinner tells a screen reader nothing. */
@Composable
fun MvLoadingState(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MvSpace.Item)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}

/**
 * Nothing here yet — and that is fine. Reads as a deliberate resting state with a way
 * forward, not as a screen that failed to load.
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        action?.invoke()
    }
}

/**
 * Something genuinely went wrong. The one place in the app where crimson is correct —
 * carried by the title and the outline, never as a full crimson fill that would shout.
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = MvContentWidth)
            .animateContentSize(tween(220))
    ) {
        Column(
            Modifier.padding(MvSpace.Card),
            verticalArrangement = Arrangement.spacedBy(MvSpace.Item)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            action?.invoke()
        }
    }
}

/** Centers a single element in the remaining space (spinners, one-line states). */
@Composable
fun MvCentered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxSize().padding(MvSpace.ScreenH),
        contentAlignment = Alignment.Center
    ) { content() }
}
