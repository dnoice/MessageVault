/*
 * ✒ Metadata
 *     - Title: Format Helpers (Message Vault Edition - v1.0)
 *     - File Name: Format.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/util/Format.kt
 *     - Artifact Type: library
 *     - Version: 1.0.3
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — Reader/dashboard formatters: day() day-group header, dateShort() dense-row date, and relativeAge() "time since".
 *     - 1.0.2 (2026-06-22) [Anthropic - Claude Opus 4.8 (1M context)] — Add friendly() date/time formatter for the reader.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Add duration() and bytes() human-readable formatters for the run-metrics output.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Tiny shared formatting helpers that live in one spot instead of being
 *     copy-pasted: rendering timestamps, building filesystem-safe filename slugs,
 *     and producing human-readable durations and byte sizes. Used wherever the
 *     same formatting rules are needed across the app.
 *
 * ✒ Key Features:
 *     - Stateless singleton (object): accessed as Format.timestamp(...), nothing to construct.
 *     - Reused SimpleDateFormat instances pinned to Locale.US for device-identical output.
 *     - friendly(): reader-facing date/time, e.g. "Jun 21, 2026 · 9:49 PM".
 *     - slug(): regex-based, collision-safe names valid on every filesystem and OneDrive.
 *     - duration() / bytes(): human-readable run-metrics formatting (binary 1024 units).
 *
 * ✒ Other Important Information:
 *     - Dependencies: java.text.SimpleDateFormat, java.util.Date, java.util.Locale. Leaf util — no app-logic dependencies; used by the Markdown exporter, attachment extractor, and engine manifest.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Format {
    private val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val friendlyFmt = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US)
    private val dayFmt = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)

    fun timestamp(epochMillis: Long): String = stamp.format(Date(epochMillis))

    /** Human, reader-friendly date/time, e.g. "Jun 21, 2026 · 9:49 PM". */
    fun friendly(epochMillis: Long): String = friendlyFmt.format(Date(epochMillis))

    /** Date only, for day-group headers, e.g. "Tuesday, Jan 7, 2026". */
    fun day(epochMillis: Long): String = dayFmt.format(Date(epochMillis))

    private val dateShortFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)

    /** Compact date without time, for dense list rows, e.g. "Jul 19, 2026". */
    fun dateShort(epochMillis: Long): String = dateShortFmt.format(Date(epochMillis))

    /** Coarse "time since", e.g. "just now", "3 hours ago", "2 weeks ago". */
    fun relativeAge(epochMillis: Long, nowMillis: Long): String {
        val diff = nowMillis - epochMillis
        if (diff < 60_000) return "just now"
        val min = diff / 60_000
        val hr = diff / 3_600_000
        val day = diff / 86_400_000
        fun plural(n: Long, unit: String) = "$n $unit${if (n == 1L) "" else "s"} ago"
        return when {
            min < 60 -> plural(min, "min")
            hr < 24 -> plural(hr, "hour")
            day < 7 -> plural(day, "day")
            day < 30 -> plural(day / 7, "week")
            day < 365 -> plural(day / 30, "month")
            else -> plural(day / 365, "year")
        }
    }

    /** Collision-safe, filesystem-friendly slug for filenames. */
    fun slug(raw: String): String {
        val cleaned = raw.trim()
            .replace(Regex("[^A-Za-z0-9._+-]+"), "_")
            .trim('_')
        return if (cleaned.isEmpty()) "unknown" else cleaned.take(64)
    }

    /** Human-readable duration: "850ms", "12.4s", "3m 07s". */
    fun duration(millis: Long): String = when {
        millis < 1000 -> "${millis}ms"
        millis < 60_000 -> String.format(Locale.US, "%.1fs", millis / 1000.0)
        else -> {
            val m = millis / 60_000
            val s = (millis % 60_000) / 1000
            String.format(Locale.US, "%dm %02ds", m, s)
        }
    }

    private val BYTE_UNITS = arrayOf("B", "KB", "MB", "GB", "TB")

    /** Human-readable byte size: "0 B", "4.2 MB". Binary (1024) units. */
    fun bytes(n: Long): String {
        if (n < 1024) return "$n B"
        var value = n.toDouble()
        var unit = 0
        while (value >= 1024 && unit < BYTE_UNITS.size - 1) {
            value /= 1024
            unit++
        }
        return String.format(Locale.US, "%.1f %s", value, BYTE_UNITS[unit])
    }
}
