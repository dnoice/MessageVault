/*
 * ✒ Metadata
 *     - Title: Contact Resolver (Message Vault Edition - v1.0)
 *     - File Name: ContactResolver.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/data/source/ContactResolver.kt
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
 *     Turns a raw phone number into a human display name by asking Android's
 *     contacts database, memoizing results so a large run that repeatedly sees the
 *     same numbers only hits the contacts provider once per distinct number. Used by
 *     SmsSource and MmsSource while building each Message; degrades gracefully to
 *     numbers-only when READ_CONTACTS is not granted.
 *
 * ✒ Key Features:
 *     - PhoneLookup query: uses ContactsContract.PhoneLookup, the purpose-built provider URI for "given this number, who is it?".
 *     - Memoized cache: a HashMap caches null too, so a number already confirmed unknown is never looked up twice.
 *     - Graceful degradation: catches SecurityException when READ_CONTACTS is missing rather than crashing.
 *     - Safe cursor handling: ?.use { } guarantees the Cursor closes even on throw.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android Context / ContentResolver; android.provider.ContactsContract; android.net.Uri.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.data.source

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

class ContactResolver(private val context: Context) {

    // Normalized-number -> name (or null if no match). null is cached too, so we
    // never re-query a number we already know is unknown.
    private val cache = HashMap<String, String?>()

    fun resolve(rawAddress: String?): String? {
        if (rawAddress.isNullOrBlank()) return null
        val key = rawAddress.trim()
        if (cache.containsKey(key)) return cache[key]

        val name = lookup(key)
        cache[key] = name
        return name
    }

    private fun lookup(number: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (se: SecurityException) {
            // READ_CONTACTS not granted — degrade gracefully to numbers-only.
            null
        }
    }
}
