@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.minimallauncher

import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AddToHomeScreen
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.window.layout.FoldingFeature
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun LauncherScreen(
    snapshot: LauncherSnapshot,
    favoriteKeys: Set<String>,
    isHomeRoleHeld: Boolean,
    isLoading: Boolean,
    showGestureCoach: Boolean,
    homeRequestId: Int,
    widgetIds: List<Int>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    foldingFeature: FoldingFeature?,
    onRequestHomeRole: () -> Unit,
    onGestureCoachSeen: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onAppInfo: (LauncherApp) -> Unit,
    onUninstall: (LauncherApp) -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onLockScreen: () -> Unit,
    onOpenNotifications: () -> Unit,
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
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(
            dampingRatio = 0.88f,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
    val pagerContentAlpha by animateFloatAsState(
        targetValue = if (searchVisible) 0f else 1f,
        animationSpec = tween(120),
        label = "pagerContentAlpha",
    )

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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = pagerContentAlpha },
            beyondViewportPageCount = 1,
            flingBehavior = pagerFlingBehavior,
            key = { it },
        ) { page ->
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction
                        ).absoluteValue.coerceIn(0f, 1f)
                        alpha = 1f - (pageOffset * 0.1f)
                        scaleX = 1f - (pageOffset * 0.018f)
                        scaleY = scaleX
                        translationY = pageOffset * 10.dp.toPx()
                    },
            ) {
                when (page) {
                    FAVORITES_PAGE -> FavoritesPage(
                        favorites = favorites,
                        widgetIds = widgetIds,
                        widgetHost = widgetHost,
                        widgetManager = widgetManager,
                        foldingFeature = foldingFeature,
                        isLoading = isLoading,
                        onLaunchApp = onLaunchApp,
                        onLongPress = { selectedApp = it },
                        onAddWidget = onAddWidget,
                        onRemoveWidget = onRemoveWidget,
                    )

                    HOME_PAGE -> WallpaperPage(
                        showGestureCoach = showGestureCoach,
                        onLockScreen = onLockScreen,
                        onOpenNotifications = onOpenNotifications,
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
                        foldingFeature = foldingFeature,
                        onRequestHomeRole = onRequestHomeRole,
                        onLaunchApp = onLaunchApp,
                        onLongPress = { selectedApp = it },
                        onSearch = {
                            onGestureCoachSeen()
                            searchVisible = true
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = searchVisible,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(120)),
        ) {
            SearchPage(
                apps = snapshot.apps,
                shortcuts = snapshot.shortcuts,
                canSearchShortcuts = snapshot.hasShortcutPermission,
                foldingFeature = foldingFeature,
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
    onLockScreen: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val threshold = with(LocalDensity.current) { 72.dp.toPx() }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val homeContentDescription = stringResource(R.string.home_content_description)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = homeContentDescription
            }
            .pointerInput(onLockScreen) {
                detectTapGestures(onDoubleTap = { onLockScreen() })
            }
            .pointerInput(threshold) {
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, amount ->
                        dragDistance += amount
                        change.consume()
                    },
                    onDragEnd = {
                        when {
                            dragDistance < -threshold -> onSearch()
                            dragDistance > threshold -> onOpenNotifications()
                        }
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
                    GestureLabel(stringResource(R.string.favorites), "←")
                    GestureLabel(stringResource(R.string.search), "↑")
                    GestureLabel(stringResource(R.string.apps), "→")
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
    widgetIds: List<Int>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    foldingFeature: FoldingFeature?,
    isLoading: Boolean,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
) {
    var editingWidgets by remember { mutableStateOf(false) }
    LaunchedEffect(widgetIds.isEmpty()) {
        if (widgetIds.isEmpty()) editingWidgets = false
    }
    PageSurface(isLoading = isLoading) {
        PageHeader(
            title = stringResource(R.string.favorites),
        )
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val verticalFold = foldingFeature?.takeIf {
                it.orientation == FoldingFeature.Orientation.VERTICAL && it.isSeparating
            }
            val useTwoPanes = maxWidth >= 700.dp || verticalFold != null
            val paneGap = verticalFold?.let {
                with(LocalDensity.current) { it.bounds.width().toDp() }.coerceAtLeast(24.dp)
            } ?: 24.dp
            if (useTwoPanes) {
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp),
                    ) {
                        favoriteItems(favorites, onLaunchApp, onLongPress)
                    }
                    Spacer(Modifier.width(paneGap))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp),
                    ) {
                        widgetItems(
                            widgetIds = widgetIds,
                            widgetHost = widgetHost,
                            widgetManager = widgetManager,
                            editingWidgets = editingWidgets,
                            onToggleEditing = { editingWidgets = !editingWidgets },
                            onAddWidget = onAddWidget,
                            onRemoveWidget = onRemoveWidget,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp),
                ) {
                    favoriteItems(favorites, onLaunchApp, onLongPress)
                    widgetItems(
                        widgetIds = widgetIds,
                        widgetHost = widgetHost,
                        widgetManager = widgetManager,
                        editingWidgets = editingWidgets,
                        onToggleEditing = { editingWidgets = !editingWidgets },
                        onAddWidget = onAddWidget,
                        onRemoveWidget = onRemoveWidget,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.favoriteItems(
    favorites: List<LauncherApp>,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
) {
    if (favorites.isEmpty()) {
        item(key = "empty_favorites") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Outlined.StarOutline,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.White.copy(alpha = 0.78f),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.no_favorites),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    stringResource(R.string.favorites_empty_message),
                    color = Color.White.copy(alpha = 0.64f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    } else {
        items(favorites, key = { it.key }) { app ->
            AppTile(
                app = app,
                onClick = { onLaunchApp(app) },
                onLongPress = { onLongPress(app) },
            )
        }
    }
}

private fun LazyListScope.widgetItems(
    widgetIds: List<Int>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    editingWidgets: Boolean,
    onToggleEditing: () -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
) {
    item(key = "widget_header") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 22.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.widgets),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
            )
            if (widgetIds.isNotEmpty()) {
                IconButton(
                    onClick = onToggleEditing,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (editingWidgets) {
                                Color.White.copy(alpha = 0.14f)
                            } else {
                                Color.Transparent
                            },
                        ),
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.remove_widget),
                        tint = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            IconButton(onClick = onAddWidget) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_widget),
                )
            }
        }
    }

    if (widgetIds.isEmpty()) {
        item(key = "empty_widgets") {
            Text(
                stringResource(R.string.widgets_empty_message),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    } else {
        items(widgetIds, key = { "widget_$it" }) { widgetId ->
            HostedWidget(
                widgetId = widgetId,
                widgetHost = widgetHost,
                widgetManager = widgetManager,
                showRemove = editingWidgets,
                onRemove = { onRemoveWidget(widgetId) },
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HostedWidget(
    widgetId: Int,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    showRemove: Boolean,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val info = remember(widgetId) { widgetManager.getAppWidgetInfo(widgetId) }
    if (info == null) return

    val preferredHeight = with(density) { info.minHeight.toDp() }.coerceIn(110.dp, 360.dp)
    val hostView = remember(widgetId, info) {
        widgetHost.createView(context, widgetId, info)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(preferredHeight)
            .clip(RoundedCornerShape(28.dp)),
    ) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(1)
        val heightDp = preferredHeight.value.toInt().coerceAtLeast(1)
        AndroidView(
            factory = { hostView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.updateAppWidgetSize(
                    null,
                    widthDp,
                    heightDp,
                    widthDp,
                    heightDp,
                )
            },
        )
        AnimatedVisibility(
            visible = showRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp),
            enter = fadeIn(tween(120)) + scaleIn(tween(160), initialScale = 0.8f),
            exit = fadeOut(tween(100)),
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.68f)),
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.remove_widget),
                    modifier = Modifier.size(16.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<LauncherApp>,
    isLoading: Boolean,
    isHomeRoleHeld: Boolean,
    hasShortcutPermission: Boolean,
    foldingFeature: FoldingFeature?,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onSearch: () -> Unit,
) {
    PageSurface(isLoading = isLoading) {
        PageHeader(
            title = stringResource(R.string.apps),
            trailing = apps.size.toString(),
        )

        if (!isHomeRoleHeld || !hasShortcutPermission) {
            HomeRoleCard(
                isHomeRoleHeld = isHomeRoleHeld,
                onRequestHomeRole = onRequestHomeRole,
            )
        }

        Box(Modifier.weight(1f)) {
            AppGrid(
                apps = apps,
                onLaunchApp = onLaunchApp,
                onLongPress = onLongPress,
                foldingFeature = foldingFeature,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 8.dp,
                ),
            )
        }
        SearchLauncherBar(onClick = onSearch)
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
                    if (isHomeRoleHeld) {
                        stringResource(R.string.activating_shortcuts)
                    } else {
                        stringResource(R.string.use_as_home)
                    },
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (isHomeRoleHeld) {
                        stringResource(R.string.android_granting_shortcuts)
                    } else {
                        stringResource(R.string.home_role_required)
                    },
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!isHomeRoleHeld) {
                FilledTonalButton(onClick = onRequestHomeRole) {
                    Text(stringResource(R.string.set_default))
                }
            }
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<LauncherApp>,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    foldingFeature: FoldingFeature?,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val verticalFold = foldingFeature?.takeIf {
            it.orientation == FoldingFeature.Orientation.VERTICAL && it.isSeparating
        }
        val columnCount = when {
            maxWidth >= 1080.dp -> 3
            maxWidth >= 600.dp || verticalFold != null -> 2
            else -> 1
        }
        val columnGap = verticalFold?.let {
            with(LocalDensity.current) { it.bounds.width().toDp() }.coerceAtLeast(20.dp)
        } ?: 4.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(columnGap),
        ) {
            gridItems(apps, key = { it.key }) { app ->
                AppTile(
                    app = app,
                    onClick = { onLaunchApp(app) },
                    onLongPress = { onLongPress(app) },
                )
            }
        }
    }
}

@Composable
private fun SearchLauncherBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.Black.copy(alpha = 0.74f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = Color.White.copy(alpha = 0.78f),
            )
            Spacer(Modifier.width(13.dp))
            Text(
                stringResource(R.string.apps_and_shortcuts),
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyLarge,
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
    val appActionsLabel = stringResource(R.string.actions_for_app, app.label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClickLabel = appActionsLabel,
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
    foldingFeature: FoldingFeature?,
    onClose: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var contentVisible by remember { mutableStateOf(false) }
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    var dismissDragging by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val dismissThreshold = with(LocalDensity.current) { 76.dp.toPx() }
    val animatedDismissDrag by animateFloatAsState(
        targetValue = dismissDrag,
        animationSpec = if (dismissDragging) {
            snap()
        } else {
            spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium)
        },
        label = "searchDismissDrag",
    )
    val normalizedQuery = remember(query) { query.normalizedForSearch() }
    val rankedResults = remember(apps, shortcuts, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            buildList {
                apps.forEach { app ->
                    relevanceScore(
                        query = normalizedQuery,
                        terms = listOf(
                            SearchTerm(app.label),
                            SearchTerm(
                                app.packageName,
                                PACKAGE_MATCH_PENALTY,
                                allowFuzzy = false,
                            ),
                        ),
                    )?.let { score -> add(AppSearchResult(app, score)) }
                }
                shortcuts.forEach { shortcut ->
                    relevanceScore(
                        query = normalizedQuery,
                        terms = listOf(
                            SearchTerm(shortcut.label),
                            SearchTerm(shortcut.description.orEmpty(), DESCRIPTION_MATCH_PENALTY),
                            SearchTerm(
                                shortcut.info.id,
                                SHORTCUT_ID_MATCH_PENALTY,
                                allowFuzzy = false,
                            ),
                        ),
                    )?.let { score -> add(ShortcutSearchResult(shortcut, score)) }
                }
            }.sortedWith(
                compareBy<LauncherSearchResult> { it.score }
                    .thenBy { it.typePriority }
                    .thenBy { it.publisherRank }
                    .thenBy { it.label },
            ).take(MAX_SEARCH_RESULTS)
        }
    }

    fun launchBestResult() {
        keyboard?.hide()
        when (val result = rankedResults.firstOrNull()) {
            is AppSearchResult -> onLaunchApp(result.app)
            is ShortcutSearchResult -> onLaunchShortcut(result.shortcut)
            null -> Unit
        }
    }

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(90)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.64f)),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .graphicsLayer {
                        translationY = animatedDismissDrag * 0.62f
                        alpha = 1f - (
                            animatedDismissDrag / (dismissThreshold * 2f)
                        ).coerceIn(0f, 0.16f)
                    },
            ) {
                AnimatedVisibility(
                    visible = contentVisible,
                    modifier = Modifier.weight(1f),
                    enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it / 18 },
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .pointerInput(dismissThreshold) {
                                    detectVerticalDragGestures(
                                        onDragStart = {
                                            dismissDragging = true
                                            dismissDrag = 0f
                                        },
                                        onVerticalDrag = { change, amount ->
                                            dismissDrag = (dismissDrag + amount).coerceAtLeast(0f)
                                            change.consume()
                                        },
                                        onDragEnd = {
                                            dismissDragging = false
                                            if (dismissDrag >= dismissThreshold) {
                                                keyboard?.hide()
                                                onClose()
                                            } else {
                                                dismissDrag = 0f
                                            }
                                        },
                                        onDragCancel = {
                                            dismissDragging = false
                                            dismissDrag = 0f
                                        },
                                    )
                                },
                        ) {
                            IconButton(
                                onClick = {
                                    keyboard?.hide()
                                    onClose()
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 8.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = stringResource(R.string.close_search),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 10.dp)
                                    .size(width = 34.dp, height = 4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.42f)),
                            )
                        }

                        SearchResults(
                            modifier = Modifier.weight(1f),
                            query = query,
                            results = rankedResults,
                            canSearchShortcuts = canSearchShortcuts,
                            foldingFeature = foldingFeature,
                            onLaunchApp = onLaunchApp,
                            onLongPress = onLongPress,
                            onLaunchShortcut = onLaunchShortcut,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(160)) +
                        slideInVertically(tween(260)) { it / 2 } +
                        scaleIn(
                            animationSpec = tween(220),
                            initialScale = 0.96f,
                            transformOrigin = TransformOrigin(0.5f, 1f),
                        ),
                ) {
                    SearchInput(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = ::launchBestResult,
                        onClear = { query = "" },
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.Black.copy(alpha = 0.78f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = Color.White.copy(alpha = 0.78f),
            )
            Spacer(Modifier.width(13.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isBlank()) {
                            Text(
                                stringResource(R.string.apps_and_shortcuts),
                                color = Color.White.copy(alpha = 0.58f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.clear_search),
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun SearchResults(
    modifier: Modifier,
    query: String,
    results: List<LauncherSearchResult>,
    canSearchShortcuts: Boolean,
    foldingFeature: FoldingFeature?,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val verticalFold = foldingFeature?.takeIf {
            it.orientation == FoldingFeature.Orientation.VERTICAL && it.isSeparating
        }
        val columnCount = when {
            maxWidth >= 1080.dp -> 3
            maxWidth >= 600.dp || verticalFold != null -> 2
            else -> 1
        }
        val columnGap = verticalFold?.let {
            with(LocalDensity.current) { it.bounds.width().toDp() }.coerceAtLeast(20.dp)
        } ?: 4.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(columnGap),
        ) {
            gridItems(results, key = { it.key }) { result ->
                when (result) {
                    is AppSearchResult -> AppSearchRow(
                        app = result.app,
                        onLaunchApp = onLaunchApp,
                        onLongPress = onLongPress,
                    )

                    is ShortcutSearchResult -> ShortcutSearchRow(
                        shortcut = result.shortcut,
                        onLaunchShortcut = onLaunchShortcut,
                        onLongPress = onLongPress,
                    )
                }
            }
            if (query.isNotBlank() && results.isEmpty()) {
                item {
                    SearchHint(
                        title = stringResource(R.string.nothing_found),
                        description = if (canSearchShortcuts) {
                            stringResource(R.string.try_another_name)
                        } else {
                            stringResource(R.string.set_default_to_search_shortcuts)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSearchRow(
    app: LauncherApp,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
) {
    ListItem(
        onClick = { onLaunchApp(app) },
        onLongClick = { onLongPress(app) },
        colors = minimalListItemColors(),
        leadingContent = { AppIcon(app, Modifier.size(42.dp)) },
    ) { Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun ShortcutSearchRow(
    shortcut: LauncherShortcut,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    ListItem(
        onClick = { onLaunchShortcut(shortcut) },
        onLongClick = { onLongPress(shortcut.owner) },
        enabled = shortcut.info.isEnabled,
        colors = minimalListItemColors(),
        supportingContent = { Text(shortcut.owner.label) },
        leadingContent = {
            Box {
                AppIcon(shortcut.owner, Modifier.size(42.dp))
                Icon(
                    Icons.Outlined.Bolt,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .padding(2.dp),
                    tint = Color.White,
                )
            }
        },
    ) { Text(shortcut.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun SearchHint(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(
            description,
            color = Color.White.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
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
                    stringResource(R.string.app_actions),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        when {
            shortcuts.isNotEmpty() -> {
                Text(
                    stringResource(R.string.shortcuts),
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
                stringResource(R.string.set_default_to_view_shortcuts),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> Text(
                stringResource(R.string.app_has_no_shortcuts),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        ActionItem(
            icon = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
            label = if (isFavorite) {
                stringResource(R.string.remove_from_favorites)
            } else {
                stringResource(R.string.add_to_favorites)
            },
            onClick = onToggleFavorite,
        )
        ActionItem(Icons.Outlined.Info, stringResource(R.string.app_info), onAppInfo)
        ActionItem(
            icon = Icons.Outlined.DeleteOutline,
            label = stringResource(R.string.uninstall),
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

private fun supportsDarkText(colors: WallpaperColors?): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        colors?.colorHints?.and(WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0

private const val FAVORITES_PAGE = 0
private const val HOME_PAGE = 1
private const val APPS_PAGE = 2
private const val PAGE_COUNT = 3
private const val DESCRIPTION_MATCH_PENALTY = 8
private const val PACKAGE_MATCH_PENALTY = 70
private const val SHORTCUT_ID_MATCH_PENALTY = 90
private const val MAX_SEARCH_RESULTS = 50

private sealed interface LauncherSearchResult {
    val score: Int
    val key: String
    val label: String
    val typePriority: Int
    val publisherRank: Int
}

private data class AppSearchResult(
    val app: LauncherApp,
    override val score: Int,
) : LauncherSearchResult {
    override val key: String = "app:${app.key}"
    override val label: String = app.label
    override val typePriority: Int = 1
    override val publisherRank: Int = 0
}

private data class ShortcutSearchResult(
    val shortcut: LauncherShortcut,
    override val score: Int,
) : LauncherSearchResult {
    override val key: String = "shortcut:${shortcut.owner.key}:${shortcut.info.id}"
    override val label: String = shortcut.label
    override val typePriority: Int = 0
    override val publisherRank: Int = shortcut.info.rank
}
