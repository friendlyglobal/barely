@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.minimallauncher

import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AddToHomeScreen
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import java.text.Normalizer
import java.util.Locale

@Composable
fun LauncherScreen(
    snapshot: LauncherSnapshot,
    favoriteKeys: Set<String>,
    isHomeRoleHeld: Boolean,
    isLoading: Boolean,
    showGestureCoach: Boolean,
    homeRequestId: Int,
    onRequestHomeRole: () -> Unit,
    onGestureCoachSeen: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onAppInfo: (LauncherApp) -> Unit,
    onUninstall: (LauncherApp) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { PAGE_COUNT })
    var searchVisible by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<LauncherApp?>(null) }
    val context = LocalContext.current
    val view = LocalView.current
    val wallpaperManager = remember(context) { WallpaperManager.getInstance(context) }
    var wallpaperSupportsDarkText by remember {
        mutableStateOf(
            supportsDarkText(
                wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM),
            ),
        )
    }
    val favorites = remember(snapshot.apps, favoriteKeys) {
        snapshot.apps.filter { it.key in favoriteKeys }
    }

    LaunchedEffect(homeRequestId) {
        if (homeRequestId > 0) {
            searchVisible = false
            selectedApp = null
            pagerState.scrollToPage(HOME_PAGE)
        }
    }

    LaunchedEffect(pagerState.settledPage, showGestureCoach) {
        if (showGestureCoach && pagerState.settledPage != HOME_PAGE) {
            onGestureCoachSeen()
        }
    }

    DisposableEffect(wallpaperManager) {
        val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                wallpaperSupportsDarkText = supportsDarkText(colors)
            }
        }
        wallpaperManager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        onDispose { wallpaperManager.removeOnColorsChangedListener(listener) }
    }

    val darkSystemBarIcons = if (pagerState.settledPage == HOME_PAGE && !searchVisible) {
        wallpaperSupportsDarkText
    } else {
        false
    }
    LaunchedEffect(darkSystemBarIcons, pagerState.settledPage) {
        val activity = context as? Activity ?: return@LaunchedEffect
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = darkSystemBarIcons
            isAppearanceLightNavigationBars = darkSystemBarIcons
        }
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { it },
        ) { page ->
            when (page) {
                FAVORITES_PAGE -> FavoritesPage(
                    favorites = favorites,
                    isLoading = isLoading,
                    onLaunchApp = onLaunchApp,
                    onLongPress = { selectedApp = it },
                )

                HOME_PAGE -> WallpaperPage(
                    showGestureCoach = showGestureCoach,
                    onSearch = {
                        onGestureCoachSeen()
                        searchVisible = true
                    },
                )

                APPS_PAGE -> AppsPage(
                    apps = snapshot.apps,
                    isLoading = isLoading,
                    isHomeRoleHeld = isHomeRoleHeld,
                    hasShortcutPermission = snapshot.hasShortcutPermission,
                    onRequestHomeRole = onRequestHomeRole,
                    onLaunchApp = onLaunchApp,
                    onLongPress = { selectedApp = it },
                )
            }
        }

        AnimatedVisibility(
            visible = searchVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 480f),
            ) + fadeOut(),
        ) {
            SearchPage(
                apps = snapshot.apps,
                shortcuts = snapshot.shortcuts,
                canSearchShortcuts = snapshot.hasShortcutPermission,
                onClose = { searchVisible = false },
                onLaunchApp = {
                    searchVisible = false
                    onLaunchApp(it)
                },
                onLongPress = { selectedApp = it },
                onLaunchShortcut = {
                    searchVisible = false
                    onLaunchShortcut(it)
                },
            )
        }
    }

    BackHandler(enabled = searchVisible) { searchVisible = false }

    selectedApp?.let { app ->
        AppActionsSheet(
            app = app,
            shortcuts = snapshot.shortcuts.filter {
                it.owner.user == app.user && it.owner.packageName == app.packageName
            },
            isFavorite = app.key in favoriteKeys,
            canReadShortcuts = snapshot.hasShortcutPermission,
            onDismiss = { selectedApp = null },
            onLaunchShortcut = {
                selectedApp = null
                onLaunchShortcut(it)
            },
            onToggleFavorite = {
                onToggleFavorite(app)
                selectedApp = null
            },
            onAppInfo = {
                selectedApp = null
                onAppInfo(app)
            },
            onUninstall = {
                selectedApp = null
                onUninstall(app)
            },
        )
    }
}

@Composable
private fun WallpaperPage(
    showGestureCoach: Boolean,
    onSearch: () -> Unit,
) {
    val threshold = with(LocalDensity.current) { 72.dp.toPx() }
    var dragDistance by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Tela inicial. Deslize para cima para pesquisar."
            }
            .pointerInput(threshold) {
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, amount ->
                        dragDistance += amount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance < -threshold) onSearch()
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f },
                )
            },
    ) {
        AnimatedVisibility(
            visible = showGestureCoach,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.68f),
                contentColor = Color.White,
                tonalElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 17.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GestureLabel("Favoritos", "←")
                    GestureLabel("Buscar", "↑")
                    GestureLabel("Apps", "→")
                }
            }
        }
    }
}

@Composable
private fun GestureLabel(label: String, arrow: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            arrow,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FavoritesPage(
    favorites: List<LauncherApp>,
    isLoading: Boolean,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
) {
    PageSurface(isLoading = isLoading) {
        PageHeader(
            title = "Favoritos",
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = Color.White.copy(alpha = 0.82f),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Nenhum favorito",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pressione um app e adicione aos favoritos.",
                        color = Color.White.copy(alpha = 0.68f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            AppGrid(
                apps = favorites,
                onLaunchApp = onLaunchApp,
                onLongPress = onLongPress,
            )
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<LauncherApp>,
    isLoading: Boolean,
    isHomeRoleHeld: Boolean,
    hasShortcutPermission: Boolean,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
) {
    PageSurface(isLoading = isLoading) {
        PageHeader(
            title = "Apps",
            trailing = apps.size.toString(),
        )

        if (!isHomeRoleHeld || !hasShortcutPermission) {
            HomeRoleCard(
                isHomeRoleHeld = isHomeRoleHeld,
                onRequestHomeRole = onRequestHomeRole,
            )
        }

        AppGrid(
            apps = apps,
            onLaunchApp = onLaunchApp,
            onLongPress = onLongPress,
        )
    }
}

@Composable
private fun PageSurface(
    isLoading: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f)),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.18f),
                    )
                } else {
                    Spacer(Modifier.height(2.dp))
                }
                content()
            }
        }
    }
}

@Composable
private fun PageHeader(
    title: String,
    trailing: String? = null,
) {
    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
            trailing?.let {
                Text(
                    it,
                    color = Color.White.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun HomeRoleCard(
    isHomeRoleHeld: Boolean,
    onRequestHomeRole: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.52f),
            contentColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.AddToHomeScreen,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isHomeRoleHeld) "Ativando atalhos…" else "Usar como tela inicial",
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (isHomeRoleHeld) {
                        "O Android está liberando os shortcuts."
                    } else {
                        "Necessário para o gesto Início e shortcuts."
                    },
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!isHomeRoleHeld) {
                FilledTonalButton(onClick = onRequestHomeRole) { Text("Definir") }
            }
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<LauncherApp>,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        items(apps, key = { it.key }) { app ->
            AppTile(
                app = app,
                onClick = { onLaunchApp(app) },
                onLongPress = { onLongPress(app) },
            )
        }
    }
}

@Composable
private fun AppTile(
    app: LauncherApp,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClickLabel = "Ações de ${app.label}",
                onLongClick = onLongPress,
            )
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun AppIcon(app: LauncherApp, modifier: Modifier = Modifier) {
    val icon = app.icon
    if (icon != null) {
        androidx.compose.foundation.Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = app.label,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                app.label.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SearchPage(
    apps: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    canSearchShortcuts: Boolean,
    onClose: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val normalizedQuery = remember(query) { query.normalized() }
    val matchingApps = remember(apps, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList() else apps.filter {
            it.label.normalized().contains(normalizedQuery) ||
                it.packageName.normalized().contains(normalizedQuery)
        }
    }
    val matchingShortcuts = remember(shortcuts, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList() else shortcuts.filter {
            it.label.normalized().contains(normalizedQuery) ||
                it.description.orEmpty().normalized().contains(normalizedQuery) ||
                it.owner.label.normalized().contains(normalizedQuery)
        }
    }

    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f)),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Fechar busca")
                }
                Text(
                    "Busca",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                placeholder = { Text("Apps e atalhos") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Limpar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.46f),
                    cursorColor = Color.White,
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.72f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.62f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.62f),
                ),
            )

            SearchResults(
                query = query,
                apps = matchingApps,
                shortcuts = matchingShortcuts,
                canSearchShortcuts = canSearchShortcuts,
                onLaunchApp = onLaunchApp,
                onLongPress = onLongPress,
                onLaunchShortcut = onLaunchShortcut,
            )
        }
        }
    }
}

@Composable
private fun SearchResults(
    query: String,
    apps: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    canSearchShortcuts: Boolean,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        if (apps.isNotEmpty()) {
            item { ResultHeader("Apps") }
            items(apps, key = { it.key }) { app ->
                ListItem(
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onLongPress(app) },
                    colors = minimalListItemColors(),
                    supportingContent = {
                        Text(
                            app.packageName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingContent = { AppIcon(app, Modifier.size(46.dp)) },
                ) { Text(app.label) }
            }
        }
        if (shortcuts.isNotEmpty()) {
            item { ResultHeader("Atalhos") }
            items(shortcuts, key = { "${it.owner.key}:${it.info.id}" }) { shortcut ->
                ListItem(
                    onClick = { onLaunchShortcut(shortcut) },
                    onLongClick = { onLongPress(shortcut.owner) },
                    enabled = shortcut.info.isEnabled,
                    colors = minimalListItemColors(),
                    supportingContent = { Text(shortcut.owner.label) },
                    leadingContent = {
                        Box {
                            AppIcon(shortcut.owner, Modifier.size(46.dp))
                            Icon(
                                Icons.Outlined.Bolt,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .padding(2.dp),
                                tint = Color.White,
                            )
                        }
                    },
                ) { Text(shortcut.label) }
            }
        }
        if (query.isBlank()) {
            item {
                SearchHint(
                    icon = Icons.Outlined.Search,
                    title = "Encontre qualquer coisa",
                    description = "Digite o nome de um app ou de uma ação publicada por ele.",
                )
            }
        } else if (apps.isEmpty() && shortcuts.isEmpty()) {
            item {
                SearchHint(
                    icon = Icons.Outlined.Bolt,
                    title = "Nada por aqui",
                    description = if (canSearchShortcuts) {
                        "Tente outro nome."
                    } else {
                        "Defina este launcher como padrão para incluir shortcuts."
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchHint(icon: ImageVector, title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.White.copy(alpha = 0.78f),
        )
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(
            description,
            color = Color.White.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        color = Color.White.copy(alpha = 0.68f),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun minimalListItemColors() = ListItemDefaults.colors(
    containerColor = Color.Transparent,
    headlineColor = Color.White,
    supportingColor = Color.White.copy(alpha = 0.62f),
    leadingIconColor = Color.White,
)

@Composable
private fun AppActionsSheet(
    app: LauncherApp,
    shortcuts: List<LauncherShortcut>,
    isFavorite: Boolean,
    canReadShortcuts: Boolean,
    onDismiss: () -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onToggleFavorite: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(app, Modifier.size(58.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(app.label, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Ações do app",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        when {
            shortcuts.isNotEmpty() -> {
                Text(
                    "Atalhos",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                shortcuts.forEach { shortcut ->
                    ListItem(
                        onClick = { onLaunchShortcut(shortcut) },
                        enabled = shortcut.info.isEnabled,
                        supportingContent = {
                            shortcut.description
                                ?.takeIf { it != shortcut.label }
                                ?.let { Text(it) }
                        },
                        leadingContent = { Icon(Icons.Outlined.Bolt, contentDescription = null) },
                    ) { Text(shortcut.label) }
                }
            }

            !canReadShortcuts -> Text(
                "Defina este launcher como padrão para ver os atalhos publicados.",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> Text(
                "Este app não publicou atalhos.",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        ActionItem(
            icon = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
            label = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
            onClick = onToggleFavorite,
        )
        ActionItem(Icons.Outlined.Info, "Informações do app", onAppInfo)
        ActionItem(
            icon = Icons.Outlined.DeleteOutline,
            label = "Desinstalar",
            onClick = onUninstall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    ListItem(
        onClick = onClick,
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
    ) { Text(label, color = color) }
}

private fun String.normalized(): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .lowercase(Locale.getDefault())

private fun supportsDarkText(colors: WallpaperColors?): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        colors?.colorHints?.and(WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0

private const val FAVORITES_PAGE = 0
private const val HOME_PAGE = 1
private const val APPS_PAGE = 2
private const val PAGE_COUNT = 3
