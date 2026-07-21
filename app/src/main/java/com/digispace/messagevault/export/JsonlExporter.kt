/*
 * ✒ Metadata
 *     - Title: JSONL Exporter (Message Vault Edition - v1.0)
 *     - File Name: JsonlExporter.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/JsonlExporter.kt
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
 *     Writes the archive as JSONL ("JSON Lines"), where each message is one
 *     complete JSON object on its own line. Selected via the JSONL checkbox, it
 *     produces messages.jsonl in the run directory — the friendliest format for
 *     downstream tooling, since consumers can stream it back line by line without
 *     loading the whole file, and tools like jq or pandas read it natively.
 *
 * ✒ Key Features:
 *     - Streaming JSONL: one independent JSON object per line keeps producer and consumer memory-flat.
 *     - org.json.JSONObject: Android-bundled builder; put() escapes quotes/newlines so output is never malformed.
 *     - JSONObject.NULL: emits real JSON nulls for missing fields instead of omitting the key.
 *     - lateinit writer: declared up top but created in open(), with a clear error if touched too early.
 *     - BufferedWriter with flush() in close() forces the final buffered bytes out before closing.
 *
 * ✒ Other Important Information:
 *     - Dependencies: org.json (Android-bundled); com.digispace.messagevault.data.model.Message; java.io.BufferedWriter / File.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import com.digispace.messagevault.data.model.Message
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File

class JsonlExporter(private val outFile: File) : Exporter {
    override val label = "JSONL (${outFile.name})"
    private lateinit var writer: BufferedWriter

    override fun open() {
        outFile.parentFile?.mkdirs()
        writer = outFile.bufferedWriter()
    }

    override fun write(message: Message) {
        val obj = JSONObject().apply {
            put("id", message.id)
            put("kind", message.kind.name)
            put("thread_id", message.threadId)
            put("address", message.address ?: JSONObject.NULL)
            put("contact_name", message.contactName ?: JSONObject.NULL)
            put("direction", message.direction.name)
            put("epoch_millis", message.epochMillis)
            put("body", message.body)
            put("read", message.read)
            if (message.attachments.isNotEmpty()) {
                val arr = JSONArray()
                for (a in message.attachments) {
                    arr.put(JSONObject().apply {
                        put("part_id", a.partId)
                        put("content_type", a.contentType)
                        put("file_name", a.fileName ?: JSONObject.NULL)
                        put("export_name", a.exportName ?: JSONObject.NULL)
                    })
                }
                put("attachments", arr)
            }
        }
        writer.write(obj.toString())
        writer.newLine()
    }

    override fun close() {
        if (::writer.isInitialized) writer.flush().also { writer.close() }
    }
}
