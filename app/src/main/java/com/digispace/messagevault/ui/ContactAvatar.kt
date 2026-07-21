/*
 * ✒ Metadata
 *     - Title: Contact Avatar (Message Vault Edition - v1.0)
 *     - File Name: ContactAvatar.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/ContactAvatar.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Both paths now render inside MvIdentityFrame: a square, hairline-bordered specimen plate rather than a circle crop. Per STYLE.md 8.4 the round identity mark is banned app-wide, and clipping a real contact photo to a circle made even the photo path read as a roster of people to talk to.
 *     - 1.0.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Initial: use the real contact photo when one exists, falling back to the generated abstract mark.
 *
 * ✒ Description:
 *     Draws a conversation's face. When the number belongs to a saved contact that has
 *     a profile photo, that photo is shown; otherwise it falls back to the deterministic
 *     generated mark from [AbstractAvatar], so every conversation always has an identity.
 *     Photos come from the LIVE contacts database, not the archive — browsing an old
 *     export shows today's photos, and a contact deleted since the export quietly falls
 *     back to its generated mark.
 *
 * ✒ Key Features:
 *     - PhoneLookup → PHOTO_THUMBNAIL_URI (list-sized) with PHOTO_URI as a fallback; thumbnails keep scrolling cheap.
 *     - Memoized per number, caching misses too, so a scrolling list never re-queries a number it already resolved.
 *     - Degrades quietly: READ_CONTACTS may be denied and a photo stream may be missing or corrupt — any failure becomes the generated mark, never an error.
 *     - Clipped to the same square identity plate as the generated mark, so mixed lists stay visually uniform.
 *
 * ✒ Other Important Information:
 *     - Dependencies: android.provider.ContactsContract; BitmapFactory; Jetpack Compose; AbstractAvatar.
 *     - Requires READ_CONTACTS (already requested for name resolution); without it every avatar is generated.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Number → photo, memoized for the life of the process (misses cached too). */
private object ContactPhotos {

    private val cache = HashMap<String, ImageBitmap?>()

    @Synchronized
    private fun cached(key: String): Pair<Boolean, ImageBitmap?> =
        if (cache.containsKey(key)) true to cache[key] else false to null

    @Synchronized
    private fun put(key: String, value: ImageBitmap?) { cache[key] = value }

    suspend fun load(context: Context, rawNumber: String?): ImageBitmap? {
        val key = rawNumber?.trim().orEmpty()
        if (key.isEmpty()) return null
        val (hit, value) = cached(key)
        if (hit) return value

        val bitmap = withContext(Dispatchers.IO) { decode(context, key) }
        put(key, bitmap)
        return bitmap
    }

    private fun decode(context: Context, number: String): ImageBitmap? = try {
        val lookup = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)
        )
        val photoUri = context.contentResolver.query(
            lookup,
            arrayOf(
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
                ContactsContract.PhoneLookup.PHOTO_URI
            ),
            null, null, null
        )?.use { c ->
            if (!c.moveToFirst()) null
            else (c.takeIf { !it.isNull(0) }?.getString(0)
                ?: c.takeIf { !it.isNull(1) }?.getString(1))
        }

        photoUri?.let { uri ->
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
    } catch (e: Exception) {
        // READ_CONTACTS denied, contact gone, unreadable blob — all mean "no photo".
        null
    }
}

/**
 * The contact's photo when there is one, else the generated mark seeded from [seed].
 *
 * @param seed stable identity for the generated fallback (display name or number).
 * @param number raw phone number used to look the contact photo up; null disables lookup.
 */
@Composable
fun ContactAvatar(
    seed: String,
    number: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var photo by remember(number) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(number) { photo = ContactPhotos.load(context, number) }

    val bitmap = photo
    MvIdentityFrame(size = size, modifier = modifier) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AbstractAvatar(seed = seed, size = size)
        }
    }
}
