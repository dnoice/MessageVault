/*
 * ✒ Metadata
 *     - Title: App Navigation (Message Vault Edition - v1.0)
 *     - File Name: AppNav.kt
 *     - Relative Path: app/src/main/java/com/digispace/messagevault/ui/AppNav.kt
 *     - Artifact Type: library
 *     - Version: 1.1.0
 *     - Date: 2026-07-20
 *     - Update: Monday, July 20, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.1.0 (2026-07-20) [Anthropic - Claude Opus 4.8] — Polish pass: destinations fade into each other instead of cutting, the top-bar title cross-fades with them, the drawer's section label uses the shared treatment, and the drawer items and footer theme toggle are labelled and sized for a thumb.
 *     - 1.0.2 (2026-07-20) [Anthropic - Claude Opus 4.8] — De-awkward the footer: drop the large three-cell theme block for a single compact theme-cycle icon on the version line; Settings › Appearance stays the canonical control. Add HOME as the start destination.
 *     - 1.0.1 (2026-07-20) [Anthropic - Claude Opus 4.8] — Drawer overhaul: brand-dot header, NAVIGATE section label, and a bottom-pinned footer carrying a one-tap theme quick-toggle, the live storage location, and version/signature.
 *     - 1.0.0 (2026-06-21) [Anthropic - Claude Opus 4.8 (1M context)] — Initial drawer + top-bar + NavHost shell.
 *
 * ✒ Description:
 *     The app shell: a navigation drawer, top app bar, and nav graph that turns the
 *     single Export screen into a real multi-destination app (Export, History,
 *     Browse, Settings). Use it as the top of the Compose tree under the theme; the
 *     hamburger opens the drawer, the bar shows the current destination, and the
 *     NavHost swaps content while preserving a back stack.
 *
 * ✒ Key Features:
 *     - Modal drawer: four destinations driven by the Dest enum (route, label, icon).
 *     - Back-stack-safe routing: popUpTo/launchSingleTop/restoreState on navigation.
 *     - State injection: MainActivity owns all state + callbacks; each route gets only what it needs.
 *     - Bottom-pinned drawer footer: one-tap SYSTEM/LIGHT/DARK theme toggle, live export location, version + signature — no trip to Settings.
 *     - Export route hosts the existing ArchiveScreen unchanged.
 *
 * ✒ Other Important Information:
 *     - Dependencies: androidx.navigation:navigation-compose; androidx.compose.material3; com.digispace.messagevault.export.ArchiveConfig; com.digispace.messagevault.ui.theme.ThemeMode.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
package com.digispace.messagevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.digispace.messagevault.R
import com.digispace.messagevault.export.ArchiveConfig
import com.digispace.messagevault.security.VaultSettings
import com.digispace.messagevault.ui.theme.ThemeMode
import kotlinx.coroutines.launch

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Outlined.Home),
    EXPORT("export", "Export", Icons.Outlined.Backup),
    HISTORY("history", "History", Icons.Outlined.History),
    BROWSE("browse", "Browse", Icons.Outlined.Forum),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    state: UiState,
    hasSmsPermission: Boolean,
    hasAllFilesAccess: Boolean,
    locationLabel: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onRequestPermission: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onShareCurrent: () -> Unit,
    onCopyCurrent: () -> Unit,
    onShareDir: (String) -> Unit,
    onCopyDir: (String) -> Unit,
    onConfigChange: ((ArchiveConfig) -> ArchiveConfig) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    vault: VaultSettings,
    onVaultChange: (VaultSettings) -> Unit
) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val current = Dest.entries.firstOrNull { it.route == currentRoute } ?: Dest.HOME

    // Which archive Browse should open: a specific run (set by a History card) or,
    // when null, the latest. Navigating to Browse from the drawer/Home resets to latest.
    var browseTarget by remember { mutableStateOf<String?>(null) }

    // One back-stack-safe hop, shared by the drawer and Home's quick actions.
    fun navigateTo(dest: Dest) {
        if (dest == Dest.BROWSE) browseTarget = null
        if (dest.route != currentRoute) {
            nav.navigate(dest.route) {
                popUpTo(Dest.HOME.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxHeight()) {
                    DrawerHeader()
                    Spacer(Modifier.height(6.dp))
                    SectionLabel("NAVIGATE")
                    Dest.entries.forEach { d ->
                        NavigationDrawerItem(
                            icon = { Icon(d.icon, contentDescription = null) },
                            label = { Text(d.label) },
                            selected = d == current,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTo(d)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    // Push the footer to the bottom of the sheet.
                    Spacer(Modifier.weight(1f))
                    DrawerFooter(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        locationLabel = locationLabel
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        // The title rides the same fade as the destination beneath it.
                        Crossfade(current.label, animationSpec = tween(220), label = "title") {
                            Text(it)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { padding ->
            // A plain fade, both directions: these are five peer destinations, not a
            // hierarchy, so a directional slide would imply a depth that isn't there.
            NavHost(
                navController = nav,
                startDestination = Dest.HOME.route,
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(160)) }
            ) {
                composable(Dest.HOME.route) {
                    HomeScreen(
                        locationLabel = locationLabel,
                        onGo = { dest -> navigateTo(dest) }
                    )
                }
                composable(Dest.EXPORT.route) {
                    ArchiveScreen(
                        state = state,
                        hasSmsPermission = hasSmsPermission,
                        hasAllFilesAccess = hasAllFilesAccess,
                        onRequestPermission = onRequestPermission,
                        onRequestAllFilesAccess = onRequestAllFilesAccess,
                        onShareRun = onShareCurrent,
                        onCopyRun = onCopyCurrent,
                        onConfigChange = onConfigChange,
                        onStart = onStart,
                        onCancel = onCancel,
                    )
                }
                composable(Dest.HISTORY.route) {
                    HistoryScreen(
                        onShareDir = onShareDir,
                        onCopyDir = onCopyDir,
                        onBrowseDir = { dir ->
                            browseTarget = dir
                            if (currentRoute != Dest.BROWSE.route) {
                                nav.navigate(Dest.BROWSE.route) {
                                    popUpTo(Dest.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
                composable(Dest.BROWSE.route) { BrowseScreen(archiveDirPath = browseTarget) }
                composable(Dest.SETTINGS.route) {
                    SettingsScreen(
                        hasAllFilesAccess = hasAllFilesAccess,
                        locationLabel = locationLabel,
                        onRequestAllFilesAccess = onRequestAllFilesAccess,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        config = state.config,
                        onConfigChange = onConfigChange,
                        vault = vault,
                        onVaultChange = onVaultChange
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // The Message Vault mark — bubble + vault dial.
        Image(
            painter = painterResource(R.drawable.mv_mark),
            contentDescription = "Message Vault",
            modifier = Modifier.size(44.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Message Vault",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "digiSpace · SMS / MMS archival",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    MvSectionLabel(text, Modifier.padding(start = 28.dp, end = 28.dp, top = 6.dp, bottom = 4.dp))
}

/**
 * Bottom-of-drawer utility strip: where exports land, the build stamp, and a single
 * compact theme-cycle icon. The full theme control lives in Settings › Appearance;
 * this is just a quick tap-through — Auto → Light → Dark — without leaving the page.
 */
@Composable
private fun DrawerFooter(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    locationLabel: String
) {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }
    val (modeIcon, modeLabel) = when (themeMode) {
        ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness to "Auto"
        ThemeMode.LIGHT -> Icons.Outlined.LightMode to "Light"
        ThemeMode.DARK -> Icons.Outlined.DarkMode to "Dark"
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Text(
                locationLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "v$version · ︻デ═─── ✦ ✦ ✦",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onThemeModeChange(nextThemeMode(themeMode)) }) {
                Icon(
                    modeIcon,
                    contentDescription = "Theme: $modeLabel — tap to change",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Cycle order for the drawer's quick theme tap-through. */
private fun nextThemeMode(m: ThemeMode): ThemeMode = when (m) {
    ThemeMode.SYSTEM -> ThemeMode.LIGHT
    ThemeMode.LIGHT -> ThemeMode.DARK
    ThemeMode.DARK -> ThemeMode.SYSTEM
}
