/*
 * ✒ Metadata
 *     - Title: MMS Source (Message Vault Edition - v1.0)
 *     - File Name: MmsSource.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/data/source/MmsSource.kt
 *     - Artifact Type: library
 *     - Version: 1.0.1
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Capture the real counterparty: read the full address set and pick FROM (137) for inbound but TO (151) for outbound/group, instead of always reading FROM (which is *self* on sent messages). Sent MMS and group threads now resolve a real contact name instead of null.
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     Reads MMS (picture/group/multi-part) messages out of Android. An MMS is not
 *     a single row, so reconstructing one message takes three queries; doing it
 *     ourselves instead of leaning on a backup app's XML is what lets us file
 *     attachments cleanly. Lives in the data/source layer, peer to SmsSource,
 *     called by ArchiveEngine and using ContactResolver for counterparty names.
 *
 * ✒ Key Features:
 *     - Three-query reconstruction: content://mms for rows, content://mms/part for
 *       text/attachment parts, content://mms/{id}/addr for the participants.
 *     - Direction-aware counterparty: FROM (137) is the other party on an inbound
 *       message, but on a sent one FROM is *you* — so outbound/group messages resolve
 *       against TO (151) instead, matching how SMS always stores the far end.
 *     - Hands attachment references (not bytes) onward; AttachmentExtractor streams the bytes later.
 *     - Per-row sub-queries inside the main loop — cheap because MMS counts are far smaller than SMS.
 *     - Skips "application/smil" layout parts; reads inline text or, rarely, text stored as a blob.
 *     - Normalizes MMS DATE from seconds to milliseconds (×1000) so downstream never has to remember it.
 *     - Names magic numbers (137 FROM / 151 TO) in a private companion object instead of scattering raw ints.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Android ContentResolver / Telephony provider; com.digispace.messagevault.data.source.ContactResolver; data.model (Attachment, Direction, Kind, Message).
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.data.source

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.digispace.messagevault.data.model.Attachment
import com.digispace.messagevault.data.model.Direction
import com.digispace.messagevault.data.model.Kind
import com.digispace.messagevault.data.model.Message

class MmsSource(
    private val context: Context,
    private val contacts: ContactResolver
) {
    private val resolver = context.contentResolver

    private companion object {
        const val PART_TEXT = "text/plain"
        const val PART_SMIL = "application/smil"
        const val ADDR_TYPE_FROM = 137  // PduHeaders.FROM
        const val ADDR_TYPE_TO = 151    // PduHeaders.TO
        // Placeholder the provider stores for "myself" on some sent messages.
        const val INSERT_ADDRESS_TOKEN = "insert-address-token"
    }

    fun count(): Int =
        resolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(Telephony.Mms._ID),
            null, null, null
        )?.use { it.count } ?: 0

    fun forEach(consume: (Message) -> Unit) {
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.THREAD_ID,
            Telephony.Mms.DATE,        // SECONDS
            Telephony.Mms.MESSAGE_BOX, // 1 inbox, 2 sent
            Telephony.Mms.READ
        )
        resolver.query(
            Telephony.Mms.CONTENT_URI,
            projection,
            null, null,
            "${Telephony.Mms.DATE} ASC"
        )?.use { c ->
            val idI = c.getColumnIndexOrThrow(Telephony.Mms._ID)
            val thI = c.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
            val daI = c.getColumnIndexOrThrow(Telephony.Mms.DATE)
            val boI = c.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
            val reI = c.getColumnIndexOrThrow(Telephony.Mms.READ)

            while (c.moveToNext()) {
                val mmsId = c.getLong(idI)
                val parts = readParts(mmsId)
                val direction = directionOf(c.getInt(boI))
                val counterparty = counterpartyOf(readAddresses(mmsId), direction)

                consume(
                    Message(
                        id = mmsId,
                        kind = Kind.MMS,
                        threadId = c.getLong(thI),
                        address = counterparty,
                        contactName = contacts.resolve(counterparty),
                        direction = direction,
                        epochMillis = c.getLong(daI) * 1000L,  // seconds -> millis
                        body = parts.text,
                        read = c.getInt(reI) == 1,
                        attachments = parts.attachments
                    )
                )
            }
        }
    }

    private data class Parts(val text: String, val attachments: List<Attachment>)

    private fun readParts(mmsId: Long): Parts {
        val sb = StringBuilder()
        val attachments = ArrayList<Attachment>()
        val uri = Uri.parse("content://mms/part")

        resolver.query(
            uri,
            arrayOf("_id", "ct", "text", "_data", "name", "cl"),
            "mid = ?", arrayOf(mmsId.toString()), null
        )?.use { p ->
            val pidI = p.getColumnIndexOrThrow("_id")
            val ctI = p.getColumnIndexOrThrow("ct")
            val txI = p.getColumnIndexOrThrow("text")
            val daI = p.getColumnIndexOrThrow("_data")
            val nmI = p.getColumnIndexOrThrow("name")
            val clI = p.getColumnIndexOrThrow("cl")

            while (p.moveToNext()) {
                val ct = p.getString(ctI) ?: continue
                when {
                    ct == PART_SMIL -> { /* layout only — ignore */ }
                    ct == PART_TEXT -> {
                        val inline = p.getString(txI)
                        if (!inline.isNullOrEmpty()) {
                            if (sb.isNotEmpty()) sb.append('\n')
                            sb.append(inline)
                        } else if (!p.isNull(daI)) {
                            // Rare: text stored as a blob rather than inline.
                            readPartText(p.getLong(pidI))?.let {
                                if (sb.isNotEmpty()) sb.append('\n')
                                sb.append(it)
                            }
                        }
                    }
                    else -> {
                        val name = p.getString(nmI) ?: p.getString(clI)
                        attachments.add(
                            Attachment(
                                partId = p.getLong(pidI),
                                contentType = ct,
                                fileName = name
                            )
                        )
                    }
                }
            }
        }
        return Parts(sb.toString(), attachments)
    }

    private fun readPartText(partId: Long): String? = try {
        resolver.openInputStream(Uri.parse("content://mms/part/$partId"))
            ?.bufferedReader()?.use { it.readText() }
    } catch (e: Exception) { null }

    private data class Addr(val address: String?, val type: Int)

    /** All participant rows for an MMS: content://mms/{id}/addr (FROM 137, TO 151, …). */
    private fun readAddresses(mmsId: Long): List<Addr> {
        val uri = Uri.parse("content://mms/$mmsId/addr")
        val out = ArrayList<Addr>()
        resolver.query(uri, arrayOf("address", "type"), null, null, null)?.use { a ->
            val adI = a.getColumnIndexOrThrow("address")
            val tyI = a.getColumnIndexOrThrow("type")
            while (a.moveToNext()) out.add(Addr(a.getString(adI), a.getInt(tyI)))
        }
        return out
    }

    /**
     * The far end of the conversation. On an inbound message that is the sender
     * (FROM); on a sent one FROM is *me*, so the recipient (TO) is the real
     * counterparty. Group threads may carry several TO rows — the first named one
     * stands in as the thread's face, matching how SMS stores a single address.
     */
    private fun counterpartyOf(addrs: List<Addr>, direction: Direction): String? {
        fun pick(type: Int): String? = addrs.firstOrNull {
            it.type == type && !it.address.isNullOrBlank() && it.address != INSERT_ADDRESS_TOKEN
        }?.address
        return when (direction) {
            Direction.OUTBOUND -> pick(ADDR_TYPE_TO) ?: pick(ADDR_TYPE_FROM)
            else -> pick(ADDR_TYPE_FROM) ?: pick(ADDR_TYPE_TO)
        }
    }

    private fun directionOf(box: Int): Direction = when (box) {
        Telephony.Mms.MESSAGE_BOX_INBOX -> Direction.INBOUND
        Telephony.Mms.MESSAGE_BOX_SENT,
        Telephony.Mms.MESSAGE_BOX_OUTBOX -> Direction.OUTBOUND
        else -> Direction.OTHER
    }
}
