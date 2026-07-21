/*
 * ✒ Metadata
 *     - Title: Browse Screen (Message Vault Edition - v1.0)
 *     - File Name: BrowseScreen.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/BrowseScreen.kt
 *     - Artifact Type: library
 *     - Version: 1.2.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
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
 *     The Browse destination: reads the latest exported archive.db back into a
 *     living reader so you can scroll conversations, open a thread as chat
 *     bubbles, search across every message body, and lift messages back out by
 *     copying or sharing a multi-selection. Entirely on-device and read-only,
 *     working from the database the app already produces.
 *
 * ✒ Key Features:
 *     - Conversation list: enumerates threads from the latest archive via ArchiveReader.
 *     - Thread drill-down: opens a thread as chat bubbles, guarded by a BackHandler so the system back gesture returns to the list.
 *     - Multi-select: long-press a bubble to start selecting, tap to toggle; Copy to clipboard or Share as plain text via the system sheet.
 *     - Debounced search: queries message bodies across the archive with keystroke debouncing.
 *     - Background reads: all DB access runs on Dispatchers.IO off the UI thread.
 *
 * ✒ Other Important Information:
 *     - Dependencies: Jetpack Compose (Material3); com.digispace.messagevault.storage.ArchiveReader; com.digispace.messagevault.util.Format; kotlinx.coroutines.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.digispace.messagevault.storage.ArchiveReader
import com.digispace.messagevault.storage.ArchivedMessage
import com.digispace.messagevault.storage.Conversation
import com.digispace.messagevault.storage.SearchHit
import com.digispace.messagevault.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

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
            // flag is what lets the list say "Searching…" rather than "No matches".
            hits = withContext(Dispatchers.IO) {
                runCatching { ArchiveReader.search(f, q) }.getOrDefault(emptyList())
            }
            searchBusy = false
        } else {
            hits = emptyList()
            searchBusy = false
        }
    }

    // Re-resolve the open thread from the reloaded list; fall back to the saved identity
    // so the thread still opens while the list is loading behind it.
    val sel = selectedId?.let { id ->
        convs.firstOrNull { it.threadId == id }
            ?: Conversation(id, selectedWho, 0, 0L, "", false, selectedAddress)
    }
    if (sel != null && dbFile != null) {
        BackHandler { clearSelection() }
        ThreadView(dbFile!!, sel, onBack = { clearSelection() })
        return
    }

    // One key for the four mutually exclusive states, so they cross-fade rather than snap.
    val phase = when {
        loading -> "loading"
        unreadable -> "unreadable"
        dbFile == null -> "empty"
        else -> "list"
    }
    Crossfade(targetState = phase, animationSpec = tween(260), label = "browse") { p ->
        when (p) {
            "loading" -> MvCentered { MvLoadingState("Opening your archive…") }
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
    onOpen: (Long, String, String?) -> Unit
) {
    val reader = Modifier.fillMaxWidth().widthIn(max = MvReaderWidth)
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pinned) {
            Text(
                "Viewing a saved archive",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = reader.padding(top = 10.dp, start = 4.dp)
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = reader.padding(vertical = 12.dp),
            singleLine = true,
            shape = MvShape.Control,
            label = { Text("Search messages") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )
        // One phase key, so an in-flight search is its own state rather than being
        // indistinguishable from a finished one that found nothing.
        val phase = when {
            searching && searchBusy -> "busy"
            searching && hits.isEmpty() -> "nohits"
            searching -> "hits"
            query.isNotEmpty() && query.trim().length < 2 -> "short"
            convs.isEmpty() -> "noconvs"
            else -> "convs"
        }
        Crossfade(targetState = phase, animationSpec = tween(200), label = "results") { p ->
            when (p) {
                "busy" -> MvCentered { MvLoadingState("Searching every message…") }
                "short" -> MvCentered {
                    MvEmptyState(
                        title = "Keep typing",
                        message = "Search needs at least two characters — a single letter " +
                            "would match most of the archive."
                    )
                }
                "nohits" -> MvCentered {
                    MvEmptyState(
                        title = "No matches",
                        message = "No message in this archive contains that text. " +
                            "Try a shorter or differently spelled search."
                    )
                }
                "hits" -> LazyColumn(reader) {
                    item {
                        Text(
                            "${hits.size} match${if (hits.size == 1) "" else "es"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 6.dp)
                        )
                    }
                    // No key: two hits can share a thread and a timestamp, and a
                    // duplicate key is a hard crash in LazyColumn.
                    items(hits) { h ->
                        Row3Line(
                            title = h.who,
                            subtitle = h.snippet,
                            trailing = Format.friendly(h.epochMillis),
                            onClick = { onOpen(h.threadId, h.who, h.address) },
                            leading = { ContactAvatar(seed = h.who, number = h.address, size = AvatarListSize) }
                        )
                    }
                }
                "noconvs" -> MvCentered {
                    MvEmptyState(
                        title = "No conversations",
                        message = "This archive holds no conversations. Run an export with " +
                            "SMS or MMS enabled and open it here."
                    )
                }
                else -> LazyColumn(reader) {
                    items(convs, key = { it.threadId }) { c ->
                        Row3Line(
                            title = c.who + if (c.hasMms) "  ·  MMS" else "",
                            subtitle = c.lastSnippet.ifEmpty { "(no text)" },
                            trailing = "${c.count} · ${Format.dateShort(c.lastEpochMillis)}",
                            onClick = { onOpen(c.threadId, c.who, c.address) },
                            leading = { ContactAvatar(seed = c.who, number = c.address, size = AvatarListSize) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadView(dbFile: File, conv: Conversation, onBack: () -> Unit) {
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

    LaunchedEffect(conv.threadId) {
        msgs = withContext(Dispatchers.IO) { ArchiveReader.messages(dbFile, conv.threadId) }
    }

    fun exitSelection() { selecting = false; selectedKeys = emptySet() }

    // While selecting, the system back gesture cancels the selection instead of leaving the thread.
    BackHandler(enabled = selecting) { exitSelection() }

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            SelectionBar(
                count = selectedKeys.size,
                onClose = { exitSelection() },
                onCopy = {
                    val text = buildSelectionText(msgs, selectedKeys, conv.who)
                    if (text.isNotEmpty()) {
                        clipboard.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied ${selectedKeys.size} message(s)", Toast.LENGTH_SHORT).show()
                    }
                    exitSelection()
                },
                onShare = {
                    val text = buildSelectionText(msgs, selectedKeys, conv.who)
                    if (text.isNotEmpty()) shareText(context, text, conv.who)
                    exitSelection()
                },
                onSelectAll = {
                    msgs?.let { all -> selectedKeys = all.map(::selectionKey).toSet() }
                }
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to conversations")
                }
                ContactAvatar(seed = conv.who, number = conv.address, size = AvatarHeaderSize)
                Spacer(Modifier.width(10.dp))
                Text(conv.who, style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        val list = msgs
        if (list == null) {
            MvCentered { MvLoadingState("Loading this conversation…") }
        } else if (list.isEmpty()) {
            // A thread row can outlive its messages if the archive was written mid-run.
            MvCentered {
                MvEmptyState(
                    title = "No messages here",
                    message = "This conversation has no messages in the archive you're reading."
                )
            }
        } else {
            val rows = remember(list) { buildThreadRows(list) }
            val listState = rememberLazyListState()
            // A thread opens at its newest message (the list is oldest-first).
            LaunchedEffect(rows) {
                if (rows.isNotEmpty()) listState.scrollToItem(rows.lastIndex)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rows.size) { i ->
                    when (val row = rows[i]) {
                        is ThreadRow.Day -> DayChip(row.label)
                        is ThreadRow.Msg -> {
                            val m = row.m
                            val key = selectionKey(m)
                            Bubble(
                                m = m,
                                selected = key in selectedKeys,
                                onLongPress = {
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
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
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

/** A thread renders as an interleaving of day headers and message bubbles. */
private sealed interface ThreadRow {
    data class Day(val label: String) : ThreadRow
    data class Msg(val m: ArchivedMessage) : ThreadRow
}

/** Insert a Day row whenever the calendar day changes (input is oldest-first). */
private fun buildThreadRows(msgs: List<ArchivedMessage>): List<ThreadRow> {
    val rows = ArrayList<ThreadRow>(msgs.size + 8)
    var lastDay: String? = null
    for (m in msgs) {
        val day = Format.day(m.epochMillis)
        if (day != lastDay) { rows.add(ThreadRow.Day(day)); lastDay = day }
        rows.add(ThreadRow.Msg(m))
    }
    return rows
}

@Composable
private fun DayChip(label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.Center) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSelectAll: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Cancel selection")
        }
        Text(
            "$count selected",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSelectAll) {
            Icon(Icons.Outlined.SelectAll, contentDescription = "Select all")
        }
        IconButton(onClick = onCopy, enabled = count > 0) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy selected")
        }
        IconButton(onClick = onShare, enabled = count > 0) {
            Icon(Icons.Outlined.Share, contentDescription = "Share selected")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Bubble(
    m: ArchivedMessage,
    selected: Boolean,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val outbound = m.direction == "OUTBOUND"
    val bubbleColor =
        if (outbound) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surface
    val shape = MvShape.Control
    // The selection wash fades in and out — a hard flip on every tap reads as a glitch.
    val selectionWash by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else Color.Transparent,
        tween(180),
        label = "wash"
    )
    Row(
        Modifier.fillMaxWidth().background(selectionWash),
        horizontalArrangement = if (outbound) Arrangement.End else Arrangement.Start
    ) {
        Column(
            Modifier
                .widthIn(max = 460.dp)
                .clip(shape)
                .background(bubbleColor)
                .then(
                    if (selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                    else Modifier
                )
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (m.body.isNotBlank()) {
                Text(m.body, style = MaterialTheme.typography.bodyMedium)
            }
            if (m.attachmentCount > 0) {
                // A real vector mark, not an emoji: it takes the theme's accent and
                // renders identically on every device.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "${m.attachmentCount} attachment" + if (m.attachmentCount == 1) "" else "s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            if (m.body.isBlank() && m.attachmentCount == 0 && m.kind == "MMS") {
                Text("[MMS]", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(3.dp))
            Text(
                Format.friendly(m.epochMillis),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

/** Stable per-message selection key — id alone repeats across SMS and MMS. */
private fun selectionKey(m: ArchivedMessage): String = "${m.kind}:${m.id}"

/**
 * Renders the chosen messages as clean, chronological plain text for the clipboard
 * or share sheet. Order follows the loaded list (already oldest-first from the reader).
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
        "[${Format.friendly(m.epochMillis)}] $speaker: $body"
    }
}

private fun shareText(context: android.content.Context, text: String, who: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Messages with $who")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share messages"))
}

@Composable
private fun Row3Line(
    title: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MvTouchTarget)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(trailing, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** A genuine failure: the archive exists but SQLite refuses to open it. */
@Composable
private fun UnreadableArchive() {
    MvErrorState(
        title = "This archive can't be opened",
        message = "The export was interrupted, so its database was left mid-write " +
            "(a leftover journal file). Run the export again to rebuild it."
    )
}

/** Not a failure: there is simply nothing archived yet. */
@Composable
private fun EmptyBrowse() {
    MvEmptyState(
        title = "Nothing to browse yet",
        message = "Run an export with SQLite enabled, then come back to read it here."
    )
}
