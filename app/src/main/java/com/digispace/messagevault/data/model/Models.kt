/*
 * ✒ Metadata
 *     - Title: Data Models (Message Vault Edition - v1.0)
 *     - File Name: Models.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/data/model/Models.kt
 *     - Artifact Type: library
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
 *     Defines the normalized data shapes — the single shared vocabulary — that every
 *     layer speaks. The SMS and MMS sources both map into these types, and every
 *     exporter reads out of them, so the rest of the app never has to care which
 *     kind a message is or which messy provider columns it came from.
 *
 * ✒ Key Features:
 *     - Unified Message type: SMS and MMS collapse into one shape downstream code consumes.
 *     - Direction / Kind enums: fixed constant sets unify SMS TYPE and MMS MSG_BOX safely.
 *     - Attachment: streams raw MMS bytes lazily at export time, never held in memory here.
 *     - Normalized epochMillis: MMS seconds are converted upstream so consumers see one unit.
 *     - ExportProgress: lightweight progress beacon emitted as the engine streams.
 *
 * ✒ Other Important Information:
 *     - Dependencies: none — sits at the bottom of the stack; everything depends on it.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.data.model

/** Where a message sat: received vs. sent. Keeps SMS TYPE / MMS MSG_BOX unified. */
enum class Direction { INBOUND, OUTBOUND, OTHER }

/** SMS or MMS — recorded so exporters can annotate, but the shape is shared. */
enum class Kind { SMS, MMS }

/**
 * A single attachment carried by an MMS part (image, audio, vcard, etc.).
 * [partId] is the provider part id; the raw bytes are streamed lazily at
 * export time via content://mms/part/{partId} — never held in memory here.
 */
data class Attachment(
    val partId: Long,
    val contentType: String,
    val fileName: String?,
    /** Resolved, collision-safe name assigned by the extractor at write time. */
    var exportName: String? = null
)

/**
 * The unit of the archive. Both SMS and MMS collapse into this.
 *
 * @param epochMillis normalized to milliseconds for BOTH kinds. (MMS dates come
 *        off the provider in *seconds* — the source layer multiplies by 1000 so
 *        nothing downstream has to remember that landmine.)
 */
data class Message(
    val id: Long,
    val kind: Kind,
    val threadId: Long,
    val address: String?,          // raw phone number / shortcode
    val contactName: String?,      // resolved display name, if any
    val direction: Direction,
    val epochMillis: Long,
    val body: String,              // SMS body, or concatenated MMS text parts
    val read: Boolean,
    val attachments: List<Attachment> = emptyList()
)

/** Lightweight progress beacon emitted as the engine streams. */
data class ExportProgress(
    val processed: Int,
    val total: Int,
    val phase: String
)
