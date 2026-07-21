/*
 * ✒ Metadata
 *     - Title: Run Delivery (Message Vault Edition - v1.0)
 *     - File Name: RunDelivery.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/storage/RunDelivery.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — copyIntoTree swallowed every IO failure into a bare `false`, which reached the user as "Copy failed." with no cause: a full SD card, a revoked tree grant and a read-only folder were indistinguishable and none of them suggested a next step. The write now lets its exception out to the caller (which reports the reason), and `false` is reserved for the one case that genuinely has no exception — the provider refusing to create the document.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — copyIntoTree derived the document MIME from the file instead of always claiming "application/zip": an encrypted .mvault was being reconciled by the provider into "<run>.mvault.zip", which no decrypt tool and no unzipper could open.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial: streaming zip + copy-zip-into-SAF-tree.
 *
 * ✒ Description:
 *     Hands a finished export run off the device by two routes beyond the public
 *     folder: bundle it into a single .zip to share via any app, or drop that zip
 *     into a folder the user picks through the Storage Access Framework (e.g. a
 *     OneDrive-synced folder). Both routes go through one zip so the nested
 *     attachments/ tree never has to be recreated through the document API.
 *
 * ✒ Key Features:
 *     - Streaming zip: walkTopDown() visits files one at a time and copyTo() streams each into the ZipOutputStream, so even a multi-hundred-MB run is never held in memory whole.
 *     - DocumentsContract delivery: the no-extra-dependency way to create a document in a user-granted tree Uri and stream bytes into it.
 *     - Single-zip design: one archive serves both the Share route and the SAF copy route from the storage layer.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Context, ContentResolver, DocumentsContract; java.util.zip; java.io.File.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.digispace.messagevault.security.VaultCrypto
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object RunDelivery {

    /**
     * Streams every file under [runDir] into <[cacheDir]>/<run name>.zip and
     * returns the zip. One file in memory at a time. Overwrites a prior zip.
     */
    fun zip(runDir: File, cacheDir: File): File {
        val out = File(cacheDir, "${runDir.name}.zip")
        if (out.exists()) out.delete()
        val prefix = runDir.absolutePath.length + 1
        ZipOutputStream(FileOutputStream(out).buffered()).use { zos ->
            runDir.walkTopDown().filter { it.isFile }.forEach { f ->
                zos.putNextEntry(ZipEntry(f.absolutePath.substring(prefix).replace('\\', '/')))
                f.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return out
    }

    /**
     * Writes [zipFile] into the user-picked [treeUri] folder. Returns true on
     * success. Caller should already hold (transient) permission on the tree.
     *
     * Throws on an IO failure rather than reporting a bare false. "Copy failed." with
     * no cause is unactionable — the caller turns the exception into a message that
     * names what went wrong (no space, revoked grant, read-only folder). A false return
     * means only that the provider declined to create the document at all.
     *
     * The MIME type is derived from the file rather than hardcoded: a sealed
     * .mvault handed over as "application/zip" gets its name reconciled by the
     * document provider (OneDrive, Drive, Files all do this) and lands as
     * "<run>.mvault.zip" — a zip that is not a zip, failing far from the cause.
     */
    fun copyIntoTree(context: Context, zipFile: File, treeUri: Uri): Boolean {
        val resolver = context.contentResolver
        val parent = DocumentsContract.buildDocumentUriUsingTree(
            treeUri, DocumentsContract.getTreeDocumentId(treeUri)
        )
        val mime = if (zipFile.extension == VaultCrypto.EXTENSION)
            "application/octet-stream" else "application/zip"
        val dest = DocumentsContract.createDocument(
            resolver, parent, mime, zipFile.name
        ) ?: return false
        resolver.openOutputStream(dest)?.use { o ->
            zipFile.inputStream().use { it.copyTo(o) }
        } ?: return false
        return true
    }
}
