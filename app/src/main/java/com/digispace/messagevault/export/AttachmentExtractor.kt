/*
 * ✒ Metadata
 *     - Title: Attachment Extractor (Message Vault Edition - v1.0)
 *     - File Name: AttachmentExtractor.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/export/AttachmentExtractor.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Stop swallowing copy failures. A throw partway through a large part left a truncated orphan file on disk that nothing linked to, and a filling disk made every later attachment fail invisibly while the run still reported plain success. Failures now delete the partial file and are counted (with the first error message) so the engine can report them.
 *     - 1.0.1 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Track filesExtracted + bytesExtracted (and capture the copyTo byte count) so the engine can report attachment totals in the run metrics.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Pulls the actual bytes of MMS attachments (photos, audio, vCards, ...) out
 *     of the content provider and writes them as real files in attachments/, giving
 *     each a unique, filesystem-safe name, then stamps that name back onto the
 *     Attachment object so the other exporters can link to the file. It is a helper
 *     the engine runs before the exporters for each message when the "extract
 *     attachments" option is on.
 *
 * ✒ Key Features:
 *     - Streaming copy: input.copyTo(output) shuttles bytes in small chunks so a large photo is never held whole in memory.
 *     - Nested use { }: both the InputStream and file OutputStream close deterministically even if the copy throws.
 *     - MimeTypeMap extension lookup: maps a content type ("image/jpeg") to a file extension ("jpg") so files land with sensible names.
 *     - Collision-safe naming via uniquify(): appends _1, _2, ... with a HashSet tracking names already taken.
 *     - Fault-tolerant copy: runCatching leaves exportName null on failure rather than aborting the whole export for one unreadable part.
 *     - No orphan half-files: a failed copy deletes its partially-written target, so attachments/ only ever holds files the archive actually references.
 *     - Per-run filesExtracted / bytesExtracted / failedExtractions counters feed the engine's run metrics, so a run that lost attachments says so instead of reporting an unqualified success.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android ContentResolver / MimeTypeMap; com.digispace.messagevault.util.Format; com.digispace.messagevault.data.model.Message.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.export

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.digispace.messagevault.data.model.Message
import com.digispace.messagevault.util.Format
import java.io.File

class AttachmentExtractor(
    context: Context,
    private val dir: File
) {
    private val resolver = context.contentResolver
    private val used = HashSet<String>()

    /** Cumulative across the run; the engine reads these for metrics. */
    var filesExtracted: Int = 0
        private set
    var bytesExtracted: Long = 0L
        private set

    /**
     * Attachments that could NOT be written (provider IO error, part missing from the
     * media store, disk full). Counted rather than thrown: one unreadable part must not
     * abort a 50k-message run — but a silent count of zero extra files is indistinguishable
     * from "this corpus had no attachments", so the engine reports these numbers.
     */
    var failedExtractions: Int = 0
        private set
    /** Message of the FIRST failure, kept for the manifest/metrics; later ones are usually the same cause. */
    var firstFailure: String? = null
        private set

    fun prepare() { dir.mkdirs() }

    /**
     * Extracts every attachment on [message], writing files and stamping each
     * Attachment.exportName in place so downstream exporters can link to them.
     */
    fun extract(message: Message) {
        for (a in message.attachments) {
            val base = a.fileName?.let { Format.slug(it) }
                ?: "mms${message.id}_part${a.partId}${extensionFor(a.contentType)}"
            val name = uniquify(base)
            val target = File(dir, name)
            // copyTo returns the byte count. A null stream (part absent from the media
            // store) is a failure just like a thrown IOException, so both land in the
            // failure branch below.
            val outcome = runCatching {
                resolver.openInputStream(Uri.parse("content://mms/part/${a.partId}"))
                    ?.use { input -> target.outputStream().use { input.copyTo(it) } }
            }
            val bytes = outcome.getOrNull()
            if (bytes != null) {
                a.exportName = name
                filesExtracted++
                bytesExtracted += bytes
            } else {
                // copyTo may have died partway through a large video, leaving a truncated
                // file under a name nothing links to. Remove it: an orphan half-file in
                // attachments/ is worse than no file, because it looks like real data.
                runCatching { target.delete() }
                failedExtractions++
                if (firstFailure == null) {
                    val t = outcome.exceptionOrNull()
                    firstFailure = if (t != null) {
                        "part ${a.partId}: ${t.message ?: t.javaClass.simpleName}"
                    } else {
                        "part ${a.partId}: no data from provider"
                    }
                }
            }
        }
    }

    private fun extensionFor(ct: String): String {
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(ct)
        return if (ext != null) ".$ext" else ".bin"
    }

    private fun uniquify(name: String): String {
        if (used.add(name)) return name
        val dot = name.lastIndexOf('.')
        val stem = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (true) {
            val candidate = "${stem}_$i$ext"
            if (used.add(candidate)) return candidate
            i++
        }
    }
}
