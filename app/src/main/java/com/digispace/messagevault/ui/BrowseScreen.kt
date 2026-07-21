/*
 * ✒ Metadata
 *     - Title: Browse Screen (Message Vault Edition - v2.1)
 *     - File Name: BrowseScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/BrowseScreen.kt
 *     - Artifact Type: library
 *     - Version: 2.1.0
 *     - Date: 2026-07-21
 *     - Update: Tuesday, July 21, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 2.1.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — Systemwide consistency pass. The three raw Icons.* lookups this file had documented as a deliberate exception are gone: STYLE.md 8.2 says a missing concept is added to MvIcons rather than reached for at a call site, so MvIcons gained Back / Dismiss / External and this screen uses them. The app's last Toast is gone with them — a failed contact hand-off is now recorded as a timestamped MvInlineAck on the masthead like every other outcome, and openContact returns its success rather than flashing a capsule over the transcript. Acknowledgements are stamped with the shared MvClock() instead of a full date, and they expire on the shared MvAckHoldMs instead of standing for the whole session. The index gutter's magic 52dp is derived from the identity plate and MvSpace.Item, so the app carries no unexplained second gutter measure. Style only.
 *     - 2.0.0 (2026-07-21) [Anthropic - Claude Opus 4.8] — The archival-instrument pass, applying STYLE.md to the tab that carried the most messenger risk. Identity marks are square plates, not glossy circles. The conversation index is a two-column ledger — correspondent and thread id left, record count and stamp right — instead of a name-plus-last-message inbox row. The thread header is a record-series masthead printing thread id, extent, date range and the archive it is reading. Bubble/DayChip are renamed TranscriptRecord/DayRule, records carry a gutter ordinal, the direction rule is square and selection is a geometry change so it reads on outbound records too. Rows are ruled rather than spaced, every stamp is Format.timestamp in monospace, search is a query against a named corpus with the matched term washed and the source thread cited, the selection bar is an opaque ruled plate naming COPY and EXTRACT, the copy Toast became an MvInlineAck, tertiary is gone, and the crossfades run on MvMotion.
 *     - 1.2.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Search says what it is doing. A body scan across a 50k-message archive takes real time, and until it returned the screen showed the previous results (or "No matches") with nothing to indicate a query was in flight — the honest reading of which was that the search had failed. There is now a searching state, a hit count on the results, and a one-character query explains that two are needed instead of silently ignoring the keystroke.
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: the loading / unreadable / empty / list states now use the shared UiKit treatments and cross-fade between each other; "no matches" reads as a real empty state instead of a bare line; conversation rows clear the 48dp touch target and announce themselves as one item; the selection tint on a bubble animates; the search icon and the archive-pinned banner are labelled for TalkBack.
 *     - 1.0.6 (2026-07-20) [Anthropic - Claude Opus 4.8] — Reading position survives a config change: the open thread, the search query, and the message multi-selection are rememberSaveable, so a rotation or a fold/unfold no longer dumps the reader back at the conversation list with an empty search box and a lost selection. Lists still reload.
 *     - 1.0.5 (2026-07-20) [Anthropic - Claude Opus 4.8] — CRASH FIX: opening an interrupted export's archive threw SQLITE_READONLY_ROLLBACK out of the loader and killed the app. The load is now guarded and an unopenable archive shows an explanation instead.
 *     - 1.0.4 (2026-07-20) [Anthropic - Claude Opus 4.8] — Accept a specific archive to open (archiveDirPath): History cards now browse their own run's archive.db instead of only the latest. A pinned archive stays put on resume; the latest view still auto-refreshes.
 *     - 1.0.3 (2026-07-20) [Anthropic - Claude Opus 4.8] — Reader depth: generative abstract avatars on conversations, search hits, and the thread header; day-group chips between bubbles; threads open at the newest message; Select all in the selection bar. Lists now reload on resume.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — Selectable thread: long-press a bubble to enter multi-select, tap to toggle, and Copy or Share the picked messages as clean text. A selection bar replaces the header while selecting.
 *     - 1.0.1 (2026-06-22) [Anthropic - Claude Opus 4.8 (1M context)] — Full reader: conversation list, thread bubbles, debounced body search.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Placeholder destination.
 *
 * ✒ Description:
 *     The Browse destination: reads an exported archive.db back as a corpus you can
 *     read. A ledger index of the threads it holds, a query that scans every body in
 *     that corpus, and a thread rendered as full-width transcript records — numbered,
 *     ruled, attributed to a named speaker by a rule in the gutter. Entirely on-device
 *     and read-only. Nothing here sends, replies, or composes, and nothing here is
 *     styled as though it could.
 *
 * ✒ Key Features:
 *     - Conversation index: a ruled two-column ledger — correspondent and thread id left, record count and last-activity stamp right.
 *     - Thread masthead: the series' identity — thread id, extent, first..last range, and the accession of the archive being read.
 *     - Transcript records: full width, gutter-numbered, ruled apart, direction carried by a square rule and a named speaker. Never a bubble.
 *     - Multi-select extraction: long-press to begin, tap to toggle; COPY to the clipboard or EXTRACT through the system sheet, acknowledged by a timestamped line rather than a Toast.
 *     - Corpus query: debounced body scan with the matched term washed in place and the source thread cited on every hit.
 *     - Background reads: all DB access runs on Dispatchers.IO off the UI thread.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (Material3); com.digispace.messagevault.ui.UiKit; com.digispace.messagevault.storage.ArchiveReader; com.digispace.messagevault.util.Format; kotlinx.coroutines.
 *     - Style: implements STYLE.md. Every glyph on this screen comes from MvIcons, including the three — Back, Dismiss, External — that were previously taken straight from Icons.* here. STYLE.md 8.2 requires a missing concept be added to MvIcons rather than reached for at a call site, and it now is.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.storage.ArchiveReader
import com.digispace.messagevault.storage.ArchivedMessage
import com.digispace.messagevault.storage.Conversation
import com.digispace.messagevault.storage.SearchHit
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * The identity plate's edge. Square, not circular — see [MvIdentityFrame]. The index
 * rule insets to plate + gap so the hairline starts at the text margin and the identity
 * column runs uninterrupted down the page.
 */
private val PlateSize = 40.dp
private val PlateGap = MvSpace.Item
private val IndexInset = PlateSize + PlateGap

/** `TH-00417` — the thread's catalogue identity, printed everywhere the thread appears. */
private fun threadCatalogId(threadId: Long): String =
    "TH-" + String.format(Locale.US, "%05d", threadId)

@Composable
fun BrowseScreen(archiveDirPath: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinned = !archiveDirPath.isNullOrBlank()
    var loading by remember { mutableStateOf(true) }
    var dbFile by remember { mutableStateOf<File?>(null) }
    var convs by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var hits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
    var searchBusy by remember { mutableStateOf(false) }
    var unreadable by remember { mutableStateOf(false) }

    // Reading position survives a configuration change. MainActivity declares no
    // android:configChanges, so a rotation — or any fold/unfold on the Fold 6 — recreates
    // the activity; with plain remember the open thread and the search box were wiped while
    // the nav back stack survived, which read as a bug rather than a restart. The archive
    // itself (convs/hits/dbFile) is cheap to reload and stays a plain remember.
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedWho by rememberSaveable { mutableStateOf("") }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    fun clearSelection() { selectedId = null; selectedWho = ""; selectedAddress = null }

    // Resolve the archive to read: the pinned run's db (from a History card) or the latest.
    // An archive that exists but can't be opened (interrupted export → hot journal) is
    // reported, never thrown — this used to crash the process.
    suspend fun load() {
        runCatching {
            val candidate: File? = withContext(Dispatchers.IO) {
                if (pinned) File(archiveDirPath!!, "archive.db").takeIf { it.exists() && it.length() > 0 }
                else ArchiveReader.latestDb(context)
            }
            val ok = candidate != null &&
                withContext(Dispatchers.IO) { ArchiveReader.isReadable(candidate!!) }
            unreadable = candidate != null && !ok
            dbFile = if (ok) candidate else null
            convs = if (ok) withContext(Dispatchers.IO) { ArchiveReader.conversations(candidate!!) }
                else emptyList()
        }
        loading = false
    }

    // Load whenever the target archive changes (also exits any open thread). The open
    // thread is cleared only on a GENUINE change of archive — comparing against a saved
    // path, because this effect also runs on the first composition after a recreation,
    // where clearing would throw away the very selection we just restored.
    var lastPath by rememberSaveable { mutableStateOf(archiveDirPath) }
    LaunchedEffect(archiveDirPath) {
        if (archiveDirPath != lastPath) { lastPath = archiveDirPath; clearSelection() }
        load()
    }
    // Keep the "latest" view fresh on resume; a pinned archive stays put.
    OnScreenResume { if (!pinned) scope.launch { load() } }
    // Keyed on dbFile too: typing while the archive was still opening ran this against a
    // null db and then never re-ran, leaving a live query permanently showing no matches.
    LaunchedEffect(query, dbFile) {
        val f = dbFile
        val q = query.trim()
        if (f != null && q.length >= 2) {
            searchBusy = true
            delay(250)  // debounce keystrokes
            // A body scan over a large archive is slow enough to look broken; the busy
            // flag is what lets the list say it is scanning rather than that it found nothing.
            hits = withContext(Dispatchers.IO) {
                runCatching { ArchiveReader.search(f, q) }.getOrDefault(emptyList())
            }
            searchBusy = false
        } else {
            hits = emptyList()
            searchBusy = false
        }
    }

    // The accession of the volume being read. Printed, always — an archive viewer that
    // never names the volume it has open is asking to be trusted.
    val accession = dbFile?.parentFile?.name ?: dbFile?.name

    // Re-resolve the open thread from the reloaded list; fall back to the saved identity
    // so the thread still opens while the list is loading behind it.
    val sel = selectedId?.let { id ->
        convs.firstOrNull { it.threadId == id }
            ?: Conversation(id, selectedWho, 0, 0L, "", false, selectedAddress)
    }
    if (sel != null && dbFile != null) {
        BackHandler { clearSelection() }
        ThreadView(dbFile!!, sel, accession, onBack = { clearSelection() })
        return
    }

    // One key for the four mutually exclusive states. Opacity only, one short duration —
    // the state either is or isn't; nothing here dissolves luxuriously.
    val phase = when {
        loading -> "loading"
        unreadable -> "unreadable"
        dbFile == null -> "empty"
        else -> "list"
    }
    Crossfade(targetState = phase, animationSpec = MvMotion.snap(), label = "browse") { p ->
        when (p) {
            "loading" -> MvCentered { MvLoadingState("Opening archive") }
            "unreadable" -> MvCentered { UnreadableArchive() }
            "empty" -> MvCentered { EmptyBrowse() }
            else -> ConversationList(
                convs = convs,
                query = query,
                onQuery = { query = it },
                searching = query.trim().length >= 2,
                searchBusy = searchBusy,
                hits = hits,
                pinned = pinned,
                accession = accession,
                onOpen = { tid, who, address ->
                    selectedId = tid
                    selectedWho = who
                    selectedAddress = address
                }
            )
        }
    }
}

@Composable
private fun ConversationList(
    convs: List<Conversation>,
    query: String,
    onQuery: (String) -> Unit,
    searching: Boolean,
    searchBusy: Boolean,
    hits: List<SearchHit>,
    pinned: Boolean,
    accession: String?,
    onOpen: (Long, String, String?) -> Unit
) {
    val reader = Modifier.fillMaxWidth().widthIn(max = MvReaderWidth)
    Column(
        Modifier.fillMaxSize().padding(horizontal = MvSpace.ScreenH),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Provenance, permanently. Which volume, and whether it is the one the app would
        // pick for itself or one the reader pinned from the register.
        if (accession != null) {
            MvFieldRow(
                label = "Archive",
                value = accession,
                valueStyle = MvType.MonoSmall,
                modifier = reader.padding(top = MvSpace.Row),
                trailing = { MvStamp(if (pinned) "PINNED" else "LATEST") }
            )
        }

        MvQueryField(
            value = query,
            onValueChange = onQuery,
            modifier = reader.padding(top = MvSpace.Item),
            label = "Query",
            placeholder = "message bodies",
            readout = when {
                searching && searchBusy -> "SCANNING"
                searching -> "${MvOrdinal(hits.size, 2)} MATCHES"
                else -> "${MvOrdinal(convs.size, 3)} THREADS"
            }
        )
        if (query.isNotEmpty()) {
            Box(reader, contentAlignment = Alignment.CenterEnd) {
                MvTextAction("Clear", onClick = { onQuery("") })
            }
        } else {
            Spacer(Modifier.height(MvSpace.Item))
        }

        // One phase key, so an in-flight scan is its own state rather than being
        // indistinguishable from a finished one that found nothing.
        val phase = when {
            searching && searchBusy -> "busy"
            searching && hits.isEmpty() -> "nohits"
            searching -> "hits"
            query.isNotEmpty() && query.trim().length < 2 -> "short"
            convs.isEmpty() -> "noconvs"
            else -> "convs"
        }
        Crossfade(targetState = phase, animationSpec = MvMotion.snap(), label = "results") { p ->
            when (p) {
                "busy" -> MvCentered { MvLoadingState("Scanning message bodies") }
                "short" -> MvCentered {
                    MvEmptyState(
                        title = "QUERY TOO SHORT",
                        message = "A body scan needs at least two characters — a single " +
                            "letter matches most of the corpus."
                    )
                }
                "nohits" -> MvCentered {
                    MvEmptyState(
                        title = "NO MATCHES",
                        message = "No record in this archive contains that text."
                    )
                }
                "hits" -> LazyColumn(reader) {
                    // No key: two hits can share a thread and a timestamp, and a
                    // duplicate key is a hard crash in LazyColumn.
                    items(hits) { h ->
                        HitRow(hit = h, query = query, onClick = { onOpen(h.threadId, h.who, h.address) })
                    }
                }
                "noconvs" -> MvCentered {
                    MvEmptyState(
                        title = "NO RECORDS",
                        message = "This archive holds no threads. Run an export with SMS or " +
                            "MMS included, then open it here."
                    )
                }
                else -> LazyColumn(reader) {
                    items(convs, key = { it.threadId }) { c ->
                        IndexRow(c) { onOpen(c.threadId, c.who, c.address) }
                    }
                }
            }
        }
    }
}

/**
 * One entry in the conversation index, as a ledger line rather than an inbox row.
 *
 * The headline of the row is deliberately *how much material exists*, not *what someone
 * last said*: correspondent and thread id on the left, record count over the last-activity
 * stamp right-aligned in monospace on the right. The last-message snippet — the specific
 * tell that made this list read as a messenger inbox — is gone; the count and the stamp
 * are what an index of a record series leads with.
 */
@Composable
private fun IndexRow(c: Conversation, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = MvTouchTarget)
                .clickable(onClick = onClick)
                .semantics(mergeDescendants = true) { }
                .padding(vertical = MvSpace.Item),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(seed = c.who, number = c.address, size = PlateSize)
            Spacer(Modifier.width(PlateGap))
            Column(Modifier.weight(1f)) {
                Text(
                    c.who,
                    style = MaterialTheme.typography.titleMedium,
                    color = MvInk.Data,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MvCatalogId(threadCatalogId(c.threadId))
                    if (c.hasMms) {
                        Spacer(Modifier.width(MvSpace.Inline))
                        MvMono("MMS", style = MvType.MonoSmall, color = MvInk.Faint)
                    }
                }
            }
            Spacer(Modifier.width(MvSpace.Inline))
            Column(horizontalAlignment = Alignment.End) {
                MvMono(MvOrdinal(c.count, 4), style = MvType.MonoValue, color = MvInk.Data)
                MvMono(
                    Format.timestamp(c.lastEpochMillis),
                    style = MvType.MonoSmall,
                    color = MvInk.Faint
                )
            }
        }
        MvRule(inset = IndexInset)
    }
}

/**
 * A search result, deliberately NOT shaped like an index entry: it cites the thread it
 * came from and shows why it matched, with the matched substring washed in place. A hit
 * that looks exactly like an inbox row tells the reader nothing about the result set.
 */
@Composable
private fun HitRow(hit: SearchHit, query: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = MvTouchTarget)
                .clickable(onClick = onClick)
                .semantics(mergeDescendants = true) { }
                .padding(vertical = MvSpace.Item),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MvCatalogId(threadCatalogId(hit.threadId), modifier = Modifier.weight(1f))
                MvMono(
                    Format.timestamp(hit.epochMillis),
                    style = MvType.MonoSmall,
                    color = MvInk.Faint
                )
            }
            Text(
                hit.who,
                style = MaterialTheme.typography.titleMedium,
                color = MvInk.Data,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                washMatch(hit.snippet, query),
                style = MaterialTheme.typography.bodySmall,
                color = MvInk.Body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        MvRule()
    }
}

/** Marks every occurrence of [query] inside [text] with a wash, so a hit shows its cause. */
@Composable
private fun washMatch(text: String, query: String): AnnotatedString {
    val wash = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    return remember(text, query, wash) {
        buildAnnotatedString {
            val q = query.trim()
            if (q.isEmpty()) {
                append(text)
            } else {
                val haystack = text.lowercase(Locale.US)
                val needle = q.lowercase(Locale.US)
                var i = 0
                while (true) {
                    val at = haystack.indexOf(needle, i)
                    if (at < 0) { append(text.substring(i)); break }
                    append(text.substring(i, at))
                    withStyle(SpanStyle(background = wash)) {
                        append(text.substring(at, at + q.length))
                    }
                    i = at + q.length
                }
            }
        }
    }
}

@Composable
private fun ThreadView(
    dbFile: File,
    conv: Conversation,
    accession: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var msgs by remember(conv.threadId) { mutableStateOf<List<ArchivedMessage>?>(null) }

    // Selection lives per-thread; keyed by "kind:id" since id alone isn't unique across SMS/MMS.
    // Saved, not merely remembered: multi-selecting messages to share and then unfolding the
    // phone used to drop the entire selection with no warning.
    var selecting by rememberSaveable(conv.threadId) { mutableStateOf(false) }
    var selectedKeys by rememberSaveable(conv.threadId, saver = SelectedKeysSaver) {
        mutableStateOf(setOf<String>())
    }
    // The Toast replacement: a vault records, it does not flash. See MvInlineAck.
    var ack by remember(conv.threadId) { mutableStateOf<String?>(null) }
    // Same standing time as every other acknowledgement in the app. This one never
    // expired, so a line reporting a copy stayed under the masthead for the whole session.
    LaunchedEffect(ack) { if (ack != null) { delay(MvAckHoldMs); ack = null } }

    LaunchedEffect(conv.threadId) {
        msgs = withContext(Dispatchers.IO) { ArchiveReader.messages(dbFile, conv.threadId) }
    }

    fun exitSelection() { selecting = false; selectedKeys = emptySet() }

    // While selecting, the system back gesture cancels the selection instead of leaving the thread.
    BackHandler(enabled = selecting) { exitSelection() }

    val list = msgs

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            SelectionBar(
                count = selectedKeys.size,
                onClose = { exitSelection() },
                onCopy = {
                    val text = buildSelectionText(msgs, selectedKeys, conv.who)
                    if (text.isNotEmpty()) {
                        clipboard.setText(AnnotatedString(text))
                        ack = "${MvOrdinal(selectedKeys.size, 2)} RECORDS COPIED · ${MvClock()}"
                    }
                    exitSelection()
                },
                onShare = {
                    val text = buildSelectionText(msgs, selectedKeys, conv.who)
                    if (text.isNotEmpty()) {
                        shareText(context, text, conv.who)
                        ack = "${MvOrdinal(selectedKeys.size, 2)} RECORDS EXTRACTED · ${MvClock()}"
                    }
                    exitSelection()
                },
                onSelectAll = {
                    msgs?.let { all -> selectedKeys = all.map(::selectionKey).toSet() }
                }
            )
        } else {
            ThreadMasthead(
                conv = conv,
                accession = accession,
                records = list?.size ?: conv.count,
                firstEpoch = list?.firstOrNull()?.epochMillis,
                lastEpoch = list?.lastOrNull()?.epochMillis ?: conv.lastEpochMillis,
                onBack = onBack,
                onContact = {
                    if (!openContact(context, conv.address)) {
                        ack = "NO CONTACTS APP · ${MvClock()}"
                    }
                }
            )
        }
        if (ack != null) {
            MvInlineAck(
                ack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.Row)
            )
        }

        if (list == null) {
            MvCentered { MvLoadingState("Reading thread") }
        } else if (list.isEmpty()) {
            // A thread row can outlive its messages if the archive was written mid-run.
            MvCentered {
                MvEmptyState(
                    title = "NO RECORDS IN THREAD",
                    message = "This thread carries no messages in the archive being read."
                )
            }
        } else {
            val rows = remember(list) { buildThreadRows(list) }
            val listState = rememberLazyListState()
            // A thread opens at its newest record (the list is oldest-first).
            LaunchedEffect(rows) {
                if (rows.isNotEmpty()) listState.scrollToItem(rows.lastIndex)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = MvSpace.ScreenH)
            ) {
                items(rows.size) { i ->
                    when (val row = rows[i]) {
                        is ThreadRow.Day -> MvDayRule(row.label)
                        is ThreadRow.Msg -> {
                            val m = row.m
                            val key = selectionKey(m)
                            TranscriptRecord(
                                m = m,
                                ordinal = row.n,
                                who = conv.who,
                                selected = key in selectedKeys,
                                onLongPress = {
                                    ack = null
                                    selecting = true
                                    selectedKeys = selectedKeys + key
                                },
                                onTap = {
                                    if (selecting) {
                                        selectedKeys =
                                            if (key in selectedKeys) selectedKeys - key else selectedKeys + key
                                        if (selectedKeys.isEmpty()) selecting = false
                                    }
                                }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(MvSpace.Section)) }
            }
        }
    }
}

/**
 * The record-series masthead. It replaces a messenger conversation header — back arrow,
 * round photo, big correspondent name, "View contact" — with the identity and extent of
 * the series being read: thread id, record count, the first..last range, and the accession
 * of the archive it was read out of. The contact hand-off survives, reduced to a trailing
 * glyph: an archive resolves who someone is, it does not become a client that can reach
 * them.
 */
@Composable
private fun ThreadMasthead(
    conv: Conversation,
    accession: String?,
    records: Int,
    firstEpoch: Long?,
    lastEpoch: Long,
    onBack: () -> Unit,
    onContact: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = MvSpace.Row, end = MvSpace.ScreenH, top = MvSpace.Row),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    MvIcons.Back,
                    contentDescription = "Back to the conversation index",
                    tint = MvInk.Data
                )
            }
            Column(Modifier.weight(1f).padding(start = MvSpace.Row)) {
                Text(
                    conv.who,
                    style = MaterialTheme.typography.titleMedium,
                    color = MvInk.Data,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MvCatalogId(threadCatalogId(conv.threadId), modifier = Modifier.weight(1f))
                    MvMono(
                        "${MvOrdinal(records, 4)} RECORDS",
                        style = MvType.MonoSmall,
                        color = MvInk.Faint
                    )
                }
            }
            if (!conv.address.isNullOrBlank()) {
                IconButton(onClick = onContact) {
                    Icon(
                        MvIcons.External,
                        contentDescription = "Open the system contact card",
                        tint = MvInk.Faint
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MvSpace.ScreenH, vertical = MvSpace.Row),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MvMono(
                accession ?: "—",
                style = MvType.MonoSmall,
                color = MvInk.Faint,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(MvSpace.Inline))
            // Range notation, not an arrow: an arrow glyph implies movement.
            MvMono(
                if (firstEpoch != null) {
                    Format.timestamp(firstEpoch).take(10) + " .. " + Format.timestamp(lastEpoch).take(10)
                } else {
                    Format.timestamp(lastEpoch).take(10)
                },
                style = MvType.MonoSmall,
                color = MvInk.Faint,
                textAlign = TextAlign.End
            )
        }
        MvRule()
    }
}

/**
 * A Set<String> in a MutableState isn't Bundle-storable on its own; flatten it to a list
 * of keys on save and rebuild the set on restore.
 */
private val SelectedKeysSaver: Saver<MutableState<Set<String>>, Any> =
    listSaver(
        save = { it.value.toList() },
        restore = { mutableStateOf(it.toSet()) }
    )

/** A thread renders as an interleaving of ruled day bands and numbered transcript records. */
private sealed interface ThreadRow {
    data class Day(val label: String) : ThreadRow
    /** [n] is the record's ordinal within the thread — the number printed in the gutter. */
    data class Msg(val m: ArchivedMessage, val n: Int) : ThreadRow
}

/** Insert a Day row whenever the calendar day changes (input is oldest-first). */
private fun buildThreadRows(msgs: List<ArchivedMessage>): List<ThreadRow> {
    val rows = ArrayList<ThreadRow>(msgs.size + 8)
    var lastDay: String? = null
    var n = 0
    for (m in msgs) {
        val day = Format.day(m.epochMillis)
        if (day != lastDay) { rows.add(ThreadRow.Day(day)); lastDay = day }
        rows.add(ThreadRow.Msg(m, ++n))
    }
    return rows
}

/**
 * The extraction bar. Not Android's action mode: an opaque plate ruled top and bottom,
 * with the quantity of records stated in monospace and the operations named as words.
 * A translucent primary wash over the transcript reads as a system affordance borrowed
 * from a mail client; a ruled plate reads as a tool clamped over the record series.
 */
@Composable
private fun SelectionBar(
    count: Int,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSelectAll: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = MvTouchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(MvIcons.Dismiss, contentDescription = "Cancel selection", tint = MvInk.Data)
            }
            MvMono(
                "${MvOrdinal(count, 2)} SELECTED",
                style = MvType.Mono,
                color = MvInk.Data,
                modifier = Modifier.weight(1f)
            )
            MvTextAction("All", onClick = onSelectAll)
            MvTextAction("Copy", onClick = onCopy, enabled = count > 0)
            MvTextAction("Extract", onClick = onShare, enabled = count > 0)
        }
        MvRule()
    }
}

/**
 * A RECORD, not a chat bubble — and the name says so, because the vocabulary is the
 * cheapest guardrail against the bubble creeping back on the next pass.
 *
 * Opposing left/right bubbles are the single most recognisable messenger signature, and
 * this app reads an archive rather than carrying a conversation. Every entry is therefore
 * full width and numbered in the gutter — line numbering is the defining mark of a
 * transcript, a deposition and a source listing, none of which anyone would mistake for a
 * messenger. Direction is carried by a square rule in that gutter plus a named speaker,
 * the way a transcript attributes a line.
 *
 * The rule uses [MvDirection], not primary-versus-secondary: in the dark scheme the
 * surface IS slate, so a secondary rule is invisible against the ground it sits on.
 * Selection widens the gutter rule rather than recolouring it — recolouring was invisible
 * on outbound records, which already owned that colour, and ambiguity is unaffordable in
 * an extraction workflow.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TranscriptRecord(
    m: ArchivedMessage,
    ordinal: Int,
    who: String,
    selected: Boolean,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val outbound = m.direction == "OUTBOUND"
    val rule = if (outbound) MvDirection.Outbound else MvDirection.Inbound
    val speaker = if (outbound) "ME" else who.uppercase(Locale.US)
    // Neutral, not accented: the wash states "included in the selection", and the gutter
    // geometry is what carries it. One short swap — a state either is or isn't.
    val hairline = MvInk.Hairline
    val wash by animateColorAsState(
        if (selected) hairline else Color.Transparent,
        MvMotion.snap(),
        label = "wash"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(wash)
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(vertical = MvSpace.Item)
        ) {
            // The gutter: a square direction rule and the record's ordinal. The one
            // instrument-like mark on the screen should not have rounded ends.
            Box(Modifier.width(MvGutterWidth).fillMaxHeight()) {
                Box(
                    Modifier
                        .width(if (selected) 8.dp else 3.dp)
                        .heightIn(min = 28.dp)
                        .fillMaxHeight()
                        .background(rule)
                )
                MvMono(
                    MvOrdinal(ordinal),
                    style = MvType.MonoSmall,
                    color = MvInk.Faint,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 6.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                // The attribution strip: one continuous monospace record header rather
                // than a name in one voice followed by technical text in another.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MvMono(
                        speaker,
                        style = MvType.Label,
                        color = MvInk.Data,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(MvSpace.Inline))
                    MvMono(
                        Format.timestamp(m.epochMillis),
                        style = MvType.MonoSmall,
                        color = MvInk.Faint
                    )
                    if (m.kind == "MMS") {
                        Spacer(Modifier.width(MvSpace.Inline))
                        MvMono("MMS", style = MvType.MonoSmall, color = MvInk.Faint)
                    }
                }
                // Human language, so sans. The rest of the record is data, so monospace.
                if (m.body.isNotBlank()) {
                    Text(m.body, style = MaterialTheme.typography.bodyMedium, color = MvInk.Data)
                }
                // An enclosure is a field of the record, marked the way a catalogue marks
                // one. Off tertiary, which collapsed onto the inbound rule in light mode.
                if (m.attachmentCount > 0) {
                    MvMono(
                        "[ATT ${MvOrdinal(m.attachmentCount, 2)}]",
                        style = MvType.MonoSmall,
                        color = MvInk.Faint
                    )
                }
                if (m.body.isBlank() && m.attachmentCount == 0 && m.kind == "MMS") {
                    MvMono("[NO CONTENT]", style = MvType.MonoSmall, color = MvInk.Faint)
                }
            }
        }
        // Ruled, not spaced — and inset to the text margin so the direction gutter runs
        // visually uninterrupted down the whole thread.
        MvRule(inset = MvGutterWidth)
    }
}

/**
 * Opens the system contact card for [number].
 *
 * A hand-off, not a feature this app implements: Android's contact sheet already offers
 * call, message and everything else, in the app those belong to. Falls back to the
 * dialer's "add contact" view for a number that isn't saved, so the tap always does
 * something.
 *
 * Returns false when the hand-off could not be made, so the caller can record it on the
 * card that produced it. This was the app's last Toast, kept on the argument that a
 * system-level failure sits outside the charter's ban — but a transient grey capsule
 * floating over a register is exactly what "a vault records, it does not flash" rules out,
 * whoever's fault the failure is.
 */
private fun openContact(context: android.content.Context, number: String?): Boolean {
    if (number.isNullOrBlank()) return false
    val lookup = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)
    )
    val contactId = runCatching {
        context.contentResolver.query(
            lookup, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null
        )?.use { if (it.moveToFirst()) it.getLong(0) else null }
    }.getOrNull()

    val intent = if (contactId != null) {
        Intent(Intent.ACTION_VIEW).setData(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        )
    } else {
        // Not in the address book — offer to add it instead of dead-ending.
        Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            putExtra(ContactsContract.Intents.Insert.PHONE, number)
        }
    }
    return runCatching { context.startActivity(intent) }.isSuccess
}

/** Stable per-message selection key — id alone repeats across SMS and MMS. */
private fun selectionKey(m: ArchivedMessage): String = "${m.kind}:${m.id}"

/**
 * Renders the chosen records as clean, chronological plain text for the clipboard or the
 * system sheet. Order follows the loaded list (already oldest-first from the reader), and
 * the stamps are the same fixed-width stamps the transcript prints.
 */
private fun buildSelectionText(
    msgs: List<ArchivedMessage>?,
    selectedKeys: Set<String>,
    who: String
): String {
    if (msgs == null) return ""
    return msgs.filter { selectionKey(it) in selectedKeys }.joinToString("\n\n") { m ->
        val speaker = if (m.direction == "OUTBOUND") "Me" else who
        val body = when {
            m.body.isNotBlank() -> m.body
            m.attachmentCount > 0 -> "[${m.attachmentCount} attachment(s)]"
            else -> "[${m.kind}]"
        }
        "[${Format.timestamp(m.epochMillis)}] $speaker: $body"
    }
}

private fun shareText(context: android.content.Context, text: String, who: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Records — $who")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Extract records"))
}

/** A genuine failure: the archive exists but SQLite refuses to open it. */
@Composable
private fun UnreadableArchive() {
    MvErrorState(
        title = "ARCHIVE UNREADABLE",
        message = "The export was interrupted, so its database was left mid-write " +
            "(a leftover journal file). Run the export again to rebuild it."
    )
}

/** Not a failure: there is simply nothing archived yet. */
@Composable
private fun EmptyBrowse() {
    MvEmptyState(
        title = "NO ARCHIVE ON RECORD",
        message = "Run an export with SQLite included, then open it here."
    )
}
