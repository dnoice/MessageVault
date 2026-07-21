/*
 * ✒ Metadata
 *     - Title: SMS Source (Message Vault Edition - v1.0)
 *     - File Name: SmsSource.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/data/source/SmsSource.kt
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
 *     Reads every text message out of Android's SMS provider one row at a time,
 *     converting each into a normalized Message. This is the simpler of the two
 *     sources (MMS is the hard one) and is the clearest place to learn the
 *     content-provider query pattern. Lives in the data/source layer and is called
 *     by ArchiveEngine; it reads from the OS and hands Messages to a consumer.
 *
 * ✒ Key Features:
 *     - Streaming forEach(consume): a (Message) -> Unit higher-order function feeds
 *       messages out one at a time so the cursor lives only as long as the loop and
 *       50k objects are never held in memory; use { } guarantees the cursor closes.
 *     - Standard provider query shape: (uri, projection, selection, selectionArgs,
 *       sortOrder) returning a Cursor pointer over the result rows.
 *     - Telephony.Sms contract columns instead of hardcoded strings, shielding the
 *       code from column-name changes.
 *     - Performant cursor iteration: column indices resolved once via
 *       getColumnIndexOrThrow(), then read by index inside the moveToNext() loop.
 *     - count() returns an exact row total up front so the UI progress bar is real.
 *     - SMS DATE is read in milliseconds (MMS will differ).
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android ContentResolver and Telephony.Sms;
 *       com.digispace.messagevault.data.model (Message, Kind, Direction);
 *       ContactResolver.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.data.source

import android.content.Context
import android.provider.Telephony
import com.digispace.messagevault.data.model.Direction
import com.digispace.messagevault.data.model.Kind
import com.digispace.messagevault.data.model.Message

class SmsSource(
    private val context: Context,
    private val contacts: ContactResolver
) {

    /** Exact row count up front, so the UI gets a real total for the progress bar. */
    fun count(): Int =
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID),
            null, null, null
        )?.use { it.count } ?: 0

    /** Streams every SMS, oldest first, invoking [consume] per message. */
    fun forEach(consume: (Message) -> Unit) {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,        // milliseconds for SMS
            Telephony.Sms.TYPE,        // 1 inbox, 2 sent, ...
            Telephony.Sms.READ
        )
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null, null,
            "${Telephony.Sms.DATE} ASC"
        )?.use { c ->
            val idI = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val thI = c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val adI = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val boI = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val daI = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val tyI = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val reI = c.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (c.moveToNext()) {
                val address = c.getString(adI)
                consume(
                    Message(
                        id = c.getLong(idI),
                        kind = Kind.SMS,
                        threadId = c.getLong(thI),
                        address = address,
                        contactName = contacts.resolve(address),
                        direction = directionOf(c.getInt(tyI)),
                        epochMillis = c.getLong(daI),
                        body = c.getString(boI) ?: "",
                        read = c.getInt(reI) == 1
                    )
                )
            }
        }
    }

    private fun directionOf(type: Int): Direction = when (type) {
        Telephony.Sms.MESSAGE_TYPE_INBOX -> Direction.INBOUND
        Telephony.Sms.MESSAGE_TYPE_SENT,
        Telephony.Sms.MESSAGE_TYPE_OUTBOX,
        Telephony.Sms.MESSAGE_TYPE_QUEUED -> Direction.OUTBOUND
        else -> Direction.OTHER
    }
}
