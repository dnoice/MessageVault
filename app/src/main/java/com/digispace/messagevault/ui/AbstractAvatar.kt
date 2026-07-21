/*
 * ✒ Metadata
 *     - Title: Abstract Avatar (Message Vault Edition - v1.0)
 *     - File Name: AbstractAvatar.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/AbstractAvatar.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The mark became a specimen plate rather than a face, per STYLE.md 8.4: the circle clip is now MvShape.Mark (square) and the white radial gloss pass is deleted. The round glossy identity mark is the second-most recognisable messenger signature after the bubble, and the gloss in particular is a skeuomorphic social finish that says "person you talk to" rather than "correspondent in a record series". The deterministic seed → palette work is untouched, so every contact keeps the same mark it had.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial deterministic generative avatar: a per-contact abstract mark drawn on a Compose canvas — gradient field, translucent overlapping blobs, and a gloss highlight, all on the digiSpace palette. Replaces flat initials.
 *
 * ✒ Description:
 *     A deterministic, abstract identity plate for a correspondent. Given a stable seed
 *     (a contact name or number), it derives a fixed pseudo-random sequence and paints
 *     layered generative art — a diagonal gradient base and a few translucent colour
 *     fields that overlap for depth — clipped to a square specimen plate. Same seed
 *     always yields the same mark, so a correspondent keeps one identity across the
 *     conversation index and the thread masthead, with no artwork stored anywhere.
 *
 * ✒ Key Features:
 *     - Deterministic: an FNV-1a hash seeds an xorshift PRNG; identical seeds render identically, and everything is precomputed once (remember) so frames never flicker.
 *     - On-palette: gradient pairs and blob colours are drawn only from navy / gold / slate / parchment — crimson is reserved for errors and never appears.
 *     - Square, and unglossed: a specimen plate is how an archive depicts a subject; a glossy circle is how a social app depicts a friend.
 *     - Cheap: pure Canvas drawing, no bitmaps or assets; sized by the caller for index rows or the masthead.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (foundation Canvas, ui.graphics Brush/Color, ui.geometry Offset); com.digispace.messagevault.ui.theme palette (Navy/Gold/Slate/Parchment).
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ─────────
 */
package com.digispace.messagevault.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.ui.theme.Gold
import com.digispace.messagevault.ui.theme.Navy
import com.digispace.messagevault.ui.theme.Parchment
import com.digispace.messagevault.ui.theme.Slate

/** Small deterministic PRNG (xorshift32) seeded from a string via FNV-1a. */
private class AvatarRng(seed: String) {
    private var s: Int = run {
        var h = -2128831035 // 2166136261 as signed Int (FNV offset basis)
        for (ch in seed) h = (h xor ch.code) * 16777619
        if (h == 0) 0x9E3779B9.toInt() else h
    }

    fun next(): Float {
        s = s xor (s shl 13)
        s = s xor (s ushr 17)
        s = s xor (s shl 5)
        return ((s ushr 8) and 0xFFFFFF) / 16_777_216f  // [0,1), 2^24
    }

    fun range(min: Float, max: Float): Float = min + next() * (max - min)
    fun pick(options: List<Color>): Color = options[(next() * options.size).toInt().coerceIn(0, options.size - 1)]
}

private data class Blob(val cx: Float, val cy: Float, val rFrac: Float, val color: Color, val alpha: Float)

private data class AvatarSpec(
    val base: Color,
    val accent: Color,
    val blobs: List<Blob>
)

// Harmonious base→accent pairs; each pair keeps a lighter member so the mark reads on navy.
private val PAIRS = listOf(
    Navy to Gold, Slate to Gold, Navy to Parchment, Slate to Parchment,
    Gold to Slate, Parchment to Slate, Gold to Navy, Slate to Navy
)
private val BLOB_COLORS = listOf(Gold, Parchment, Slate, Navy)

private fun buildSpec(seed: String): AvatarSpec {
    val rng = AvatarRng(seed)
    val (base, accent) = PAIRS[(rng.next() * PAIRS.size).toInt().coerceIn(0, PAIRS.size - 1)]
    val count = 3
    val blobs = List(count) {
        Blob(
            cx = rng.range(0.05f, 0.95f),
            cy = rng.range(0.05f, 0.95f),
            rFrac = rng.range(0.34f, 0.72f),
            color = rng.pick(BLOB_COLORS),
            alpha = rng.range(0.30f, 0.52f)
        )
    }
    return AvatarSpec(base, accent, blobs)
}

/**
 * Draws the abstract mark for [seed] as a square identity plate of the given [size].
 */
@Composable
fun AbstractAvatar(seed: String, size: Dp, modifier: Modifier = Modifier) {
    val spec = remember(seed) { buildSpec(seed) }
    Canvas(modifier.size(size).clip(MvShape.Mark)) {
        val d = this.size.minDimension

        // Base diagonal gradient.
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(spec.base, spec.accent),
                start = Offset(0f, 0f),
                end = Offset(d, d)
            )
        )

        // Overlapping translucent colour fields build depth.
        spec.blobs.forEach { b ->
            drawCircle(
                color = b.color.copy(alpha = b.alpha),
                radius = b.rFrac * d,
                center = Offset(b.cx * d, b.cy * d)
            )
        }
        // No gloss pass. A white radial highlight is a skeuomorphic social-app finish —
        // it reads as a lit photographic object, i.e. a person you talk to.
    }
}

/** Convenience default sizes for an identity plate. */
val AvatarListSize: Dp = 40.dp
val AvatarHeaderSize: Dp = 36.dp
