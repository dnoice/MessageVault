/*
 * ✒ Metadata
 *     - Title: Markdown Exporter (Message Vault Edition - v1.0)
 *     - File Name: MarkdownExporter.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/MarkdownExporter.kt
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
 *     Writes the archive as human-readable transcripts — one Markdown file per
 *     conversation thread, in a conversations/ folder. Use it when you want to
 *     read your history (or drop it into a digiSpace doc), as opposed to querying
 *     it (SQLite) or machine-processing it (JSONL).
 *
 * ✒ Key Features:
 *     - Per-thread files: a HashMap<Long, BufferedWriter> opens a file the first
 *       time each thread is seen and appends thereafter, so interleaved streamed
 *       messages never require an up-front sort.
 *     - getOrPut(key) { ... }: idiomatic Kotlin "open the file once, reuse it
 *       after" in a single line.
 *     - Header-once dedup via a HashSet so the "# Conversation" title block is
 *       written once per file, not per message.
 *     - Blockquote bodies: lineSequence() lazily prefixes each line with "> " to
 *       keep multi-line texts tidy.
 *     - Resilient close(): runCatching { } flushes/closes every writer, swallowing
 *       a single bad handle so the rest still flush.
 *
 * ✒ Other Important Information:
 *     - Dependencies: java.io (BufferedWriter, File); com.digispace.messagevault.data.model
 *       (Direction, Message); com.digispace.messagevault.util.Format; Exporter.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import com.digispace.messagevault.data.model.Direction
import com.digispace.messagevault.data.model.Message
import com.digispace.messagevault.util.Format
import java.io.BufferedWriter
import java.io.File

class MarkdownExporter(private val dir: File) : Exporter {
    override val label = "Markdown (conversations/)"

    private val writers = HashMap<Long, BufferedWriter>()
    private val seenHeader = HashSet<Long>()

    override fun open() { dir.mkdirs() }

    override fun write(message: Message) {
        val w = writers.getOrPut(message.threadId) {
            val who = message.contactName ?: message.address ?: "thread_${message.threadId}"
            val file = File(dir, "${Format.slug(who)}_${message.threadId}.md")
            file.bufferedWriter()
        }
        if (seenHeader.add(message.threadId)) {
            val who = message.contactName ?: message.address ?: "Unknown"
            w.write("# Conversation — $who\n\n")
            w.write("_Thread ${message.threadId}. Exported by digiSpace Message Vault._\n\n")
        }

        val arrow = when (message.direction) {
            Direction.OUTBOUND -> "→"
            Direction.INBOUND -> "←"
            else -> "·"
        }
        val speaker = if (message.direction == Direction.OUTBOUND) "Me"
            else (message.contactName ?: message.address ?: "Them")

        w.write("**$arrow $speaker** · ${Format.timestamp(message.epochMillis)}")
        if (message.kind.name == "MMS") w.write(" · _MMS_")
        w.write("\n\n")
        if (message.body.isNotBlank()) {
            // Indent body as a blockquote so multi-line texts read cleanly.
            message.body.lineSequence().forEach { w.write("> $it\n") }
            w.write("\n")
        }
        for (a in message.attachments) {
            val ref = a.exportName ?: a.fileName ?: "part ${a.partId}"
            w.write("> 📎 `$ref` (${a.contentType})\n")
        }
        if (message.attachments.isNotEmpty()) w.write("\n")
        w.write("---\n\n")
    }

    override fun close() {
        writers.values.forEach { runCatching { it.flush(); it.close() } }
        writers.clear()
    }
}
