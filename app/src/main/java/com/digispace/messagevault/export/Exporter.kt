/*
 * ✒ Metadata
 *     - Title: Exporter Contract (Message Vault Edition - v1.0)
 *     - File Name: Exporter.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/Exporter.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Add markComplete() with a no-op default, so a transactional sink can distinguish a finished run from a cancelled/failed one (close() runs on every path and cannot).
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Defines the contract every output format must satisfy, so the engine codes
 *     against this interface rather than any specific format. It is the seam
 *     between the engine and the concrete exporters: the engine holds a
 *     List<Exporter> and calls open/write/close on each, letting JSONL, SQLite,
 *     and Markdown act as interchangeable sinks behind one shape.
 *
 * ✒ Key Features:
 *     - Interface-only contract: lists method signatures with no bodies, so concrete classes supply the behavior (polymorphism chosen at runtime).
 *     - Closeable extension: signals the exporter holds a resource to release and enables use {} blocks.
 *     - Lifecycle pattern: open() then many write() then markComplete() (success only) then close(), so a SQLite exporter can wrap writes in one transaction or a file exporter open its handle once.
 *     - Flat-memory rule: every implementation writes per message and accumulates nothing, keeping 50k messages safe on a phone.
 *
 * ✒ Other Important Information:
 *     - Dependencies: java.io.Closeable; com.digispace.messagevault.data.model.Message.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import com.digispace.messagevault.data.model.Message
import java.io.Closeable

interface Exporter : Closeable {
    /** Human-readable name shown in the run log. */
    val label: String

    /** Allocate resources (open files, begin transactions). */
    fun open()

    /** Persist one message. Called once per message, in stream order. */
    fun write(message: Message)

    /**
     * Signals that the streaming pass finished normally. The engine calls this ONCE,
     * after the last source loop returns and before close(). Sinks that can only be
     * committed as a whole (SqliteExporter's single transaction) use it to tell a
     * finished run apart from a cancelled or failed one — close() alone cannot, since
     * it runs from the engine's finally block on every path. Default no-op: append-only
     * sinks (JSONL, Markdown) have nothing to decide.
     */
    fun markComplete() {}

    /** Flush + release. Always called, even on failure. */
    override fun close()
}
