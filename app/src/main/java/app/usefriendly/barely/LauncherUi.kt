@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package app.usefriendly.barely

import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.util.SizeF
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
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
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.core.view.WindowCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.window.layout.FoldingFeature
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun BarelyOnboarding(
    initialMode: LauncherHomeMode,
    initialAssistant: AssistantPreference,
    availableAssistants: List<AssistantPreference>,
    onComplete: (LauncherHomeMode, AssistantPreference) -> Unit,
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedAssistant by remember(availableAssistants) {
        mutableStateOf(
            initialAssistant.takeIf { it in availableAssistants }
                ?: availableAssistants.firstOrNull()
                ?: AssistantPreference.ASK_EVERY_TIME,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = BarelyVisualTokens.surfaceSubtle),
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.72f),
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = BarelyVisualTokens.screenHorizontalPadding,
                    vertical = 22.dp,
                ),
        ) {
            Text(
                text = "barely",
                color = Color.White.copy(alpha = BarelyVisualTokens.contentHigh),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.onboarding_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_summary),
                color = Color.White.copy(alpha = BarelyVisualTokens.contentSecondary),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            HomeModeChoice(
                mode = LauncherHomeMode.CLASSIC,
                title = stringResource(R.string.home_mode_classic),
                summary = stringResource(R.string.home_mode_classic_summary),
                selected = selectedMode == LauncherHomeMode.CLASSIC,
                onClick = { selectedMode = LauncherHomeMode.CLASSIC },
            )
            Spacer(Modifier.height(12.dp))
            HomeModeChoice(
                mode = LauncherHomeMode.TERMINAL,
                title = stringResource(R.string.home_mode_terminal),
                summary = stringResource(R.string.home_mode_terminal_summary),
                selected = selectedMode == LauncherHomeMode.TERMINAL,
                onClick = { selectedMode = LauncherHomeMode.TERMINAL },
            )
            if (availableAssistants.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                Text(
                    stringResource(R.string.settings_ai_assistant),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.onboarding_ai_assistant_summary),
                    color = Color.White.copy(alpha = BarelyVisualTokens.contentSecondary),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableAssistants.forEach { assistant ->
                        AssistantPreferenceChoice(
                            modifier = Modifier.weight(1f),
                            assistant = assistant,
                            selected = assistant == selectedAssistant,
                            onClick = { selectedAssistant = assistant },
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.onboarding_change_later),
                color = Color.White.copy(alpha = 0.56f),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(18.dp))
            FilledTonalButton(
                onClick = { onComplete(selectedMode, selectedAssistant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
    }
}

@Composable
private fun AssistantPreferenceChoice(
    modifier: Modifier,
    assistant: AssistantPreference,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(BarelyVisualTokens.compactRowShape)
            .combinedClickable(onClick = onClick)
            .semantics {
                this.selected = selected
                role = Role.RadioButton
            },
        shape = BarelyVisualTokens.compactRowShape,
        color = if (selected) Color.White.copy(alpha = 0.18f)
        else Color.Black.copy(alpha = BarelyVisualTokens.surfaceIdle),
        contentColor = Color.White,
        border = BorderStroke(
            1.dp,
            if (selected) Color.White.copy(alpha = 0.68f)
            else Color.White.copy(alpha = 0.14f),
        ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                assistantPreferenceLabel(assistant),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun assistantPreferenceLabel(preference: AssistantPreference): String = when (preference) {
    AssistantPreference.CHATGPT -> stringResource(R.string.assistant_chatgpt)
    AssistantPreference.GEMINI -> stringResource(R.string.assistant_gemini)
    AssistantPreference.CLAUDE -> stringResource(R.string.assistant_claude)
    AssistantPreference.ASK_EVERY_TIME -> stringResource(R.string.assistant_ask_every_time)
}

@Composable
private fun HomeModeChoice(
    mode: LauncherHomeMode,
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = BarelyVisualTokens.widgetShape
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onClick)
            .semantics {
                this.selected = selected
                role = Role.RadioButton
            },
        shape = shape,
        color = if (selected) {
            Color.White.copy(alpha = 0.16f)
        } else {
            Color.Black.copy(alpha = BarelyVisualTokens.surfaceIdle)
        },
        contentColor = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color.White.copy(alpha = 0.72f)
            else Color.White.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = BarelyVisualTokens.controlHorizontalPadding,
                vertical = 17.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (mode == LauncherHomeMode.TERMINAL) {
                    Icons.Outlined.Bolt
                } else {
                    Icons.Outlined.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.width(42.dp),
                tint = Color.White.copy(alpha = if (selected) 1f else 0.62f),
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    summary,
                    color = Color.White.copy(alpha = 0.64f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
fun LauncherScreen(
    snapshot: LauncherSnapshot,
    favoriteKeys: Set<String>,
    isHomeRoleHeld: Boolean,
    isLoading: Boolean,
    showGestureCoach: Boolean,
    homeRequestId: Int,
    widgets: List<WidgetPlacement>,
    widgetProviders: List<AppWidgetProviderInfo>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    foldingFeature: FoldingFeature?,
    backdropBlurEnabled: Boolean,
    launcherSettings: LauncherSettings,
    availableAssistants: List<AssistantPreference>,
    privateSpaceExpanded: Boolean,
    contacts: List<LauncherContact>,
    hasContactsPermission: Boolean,
    hasGestureAccess: Boolean,
    hasNotificationAccess: Boolean,
    notificationCounts: Map<String, Int>,
    nowPlaying: NowPlaying?,
    recommendedAppKeys: List<String>,
    recentAppSearches: List<String>,
    launcherSearchLearning: List<LauncherSearchLearning>,
    onRequestHomeRole: () -> Unit,
    onGestureCoachSeen: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onToggleShortcutFavorite: (LauncherShortcut) -> Unit,
    onAppInfo: (LauncherApp) -> Unit,
    onUninstall: (LauncherApp) -> Unit,
    onAddWidget: (AppWidgetProviderInfo) -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onUpdateWidget: (Int, Int, Int, WidgetHorizontalPosition) -> Unit,
    onMoveWidget: (Int, Int) -> Unit,
    onSetPrivateSpaceExpanded: (Boolean) -> Unit,
    onSetPrivateSpaceLocked: (LauncherProfile, Boolean) -> Unit,
    onExecuteCommand: (LauncherCommand) -> Unit,
    onMediaAction: (MediaAction) -> Unit,
    onLockScreen: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSettingsChanged: (LauncherSettings) -> Unit,
    onAppSearchCommitted: (String, LauncherApp) -> Unit,
    onShortcutSearchCommitted: (String, LauncherShortcut) -> Unit,
    onClearLocalHistory: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onConfigureContacts: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    onBackdropChanged: (LauncherBackdrop) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { PAGE_COUNT })
    var searchVisible by remember { mutableStateOf(false) }
    var initialSearchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LauncherApp?>(null) }
    var settingsVisible by remember { mutableStateOf(false) }
    var widgetPickerVisible by remember { mutableStateOf(false) }
    var terminalAppsVisible by remember { mutableStateOf(false) }
    var editingWidgets by remember { mutableStateOf(false) }
    val rootFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
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
    val favoriteShortcuts = remember(snapshot.shortcuts, favoriteKeys) {
        snapshot.shortcuts.filter { it.searchTargetKey in favoriteKeys }
    }
    val recommendedApps = remember(snapshot.apps, recommendedAppKeys, launcherSettings.localSuggestions) {
        if (!launcherSettings.localSuggestions) emptyList() else recommendedAppKeys.mapNotNull { key ->
            snapshot.apps.firstOrNull { it.key == key && !it.isPrivate }
        }
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
        animationSpec = tween(if (searchVisible) 160 else 220),
        label = "pagerContentAlpha",
    )

    fun openSearch(initialQuery: String = "") {
        initialSearchQuery = initialQuery
        onGestureCoachSeen()
        searchVisible = true
    }

    fun runHomeGesture(action: LauncherGestureAction) {
        when (action) {
            LauncherGestureAction.NONE -> Unit
            LauncherGestureAction.LOCK_SCREEN -> onLockScreen()
            LauncherGestureAction.NOTIFICATIONS -> onOpenNotifications()
            LauncherGestureAction.SEARCH -> {
                if (launcherSettings.homeMode == LauncherHomeMode.CLASSIC) openSearch()
            }
            LauncherGestureAction.APPS -> {
                if (launcherSettings.homeMode == LauncherHomeMode.TERMINAL) {
                    terminalAppsVisible = true
                } else {
                    scope.launch { pagerState.animateScrollToPage(APPS_PAGE) }
                }
            }
        }
    }

    LaunchedEffect(searchVisible, launcherSettings.homeMode) {
        if (!searchVisible && launcherSettings.homeMode == LauncherHomeMode.CLASSIC) {
            rootFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(pagerState, searchVisible, launcherSettings.homeMode, terminalAppsVisible) {
        snapshotFlow {
            when {
                launcherSettings.homeMode == LauncherHomeMode.TERMINAL -> {
                    if (terminalAppsVisible) LauncherBackdrop.FROSTED else LauncherBackdrop.CLEAR
                }
                searchVisible -> LauncherBackdrop.SEARCH
                pagerState.currentPage == HOME_PAGE &&
                    pagerState.currentPageOffsetFraction.absoluteValue < 0.04f -> {
                    LauncherBackdrop.CLEAR
                }
                else -> LauncherBackdrop.FROSTED
            }
        }.distinctUntilChanged().collect(onBackdropChanged)
    }

    LaunchedEffect(homeRequestId) {
        if (homeRequestId > 0) {
            searchVisible = false
            selectedApp = null
            settingsVisible = false
            widgetPickerVisible = false
            terminalAppsVisible = false
            editingWidgets = false
            pagerState.scrollToPage(HOME_PAGE)
        }
    }

    LaunchedEffect(pagerState.settledPage, showGestureCoach) {
        if (pagerState.settledPage != FAVORITES_PAGE) editingWidgets = false
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

    val terminalBackgroundColor = Color(launcherSettings.terminalBackgroundColor)
    val darkSystemBarIcons = if (
        launcherSettings.homeMode == LauncherHomeMode.TERMINAL && !terminalAppsVisible
    ) {
        if (launcherSettings.terminalBackgroundOpacity < 0.3f) {
            wallpaperSupportsDarkText
        } else {
            terminalBackgroundColor.luminance() > 0.58f
        }
    } else if (pagerState.settledPage == HOME_PAGE && !searchVisible) {
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

    Box(
        Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (
                    launcherSettings.homeMode == LauncherHomeMode.TERMINAL ||
                    searchVisible ||
                    event.type != KeyEventType.KeyDown
                ) {
                    return@onPreviewKeyEvent false
                }
                when {
                    event.key == Key.DirectionLeft -> {
                        scope.launch {
                            pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                        }
                        true
                    }

                    event.key == Key.DirectionRight -> {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage + 1).coerceAtMost(PAGE_COUNT - 1),
                            )
                        }
                        true
                    }

                    event.key == Key.DirectionUp ||
                        (event.isCtrlPressed && event.key == Key.K) ||
                        event.key == Key.Slash -> {
                        openSearch()
                        true
                    }

                    !event.isCtrlPressed -> {
                        val character = event.nativeKeyEvent.unicodeChar
                            .takeIf { it > 0 }
                            ?.let { codePoint -> String(Character.toChars(codePoint)) }
                        if (character?.firstOrNull()?.isLetterOrDigit() == true) {
                            openSearch(character)
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            },
    ) {
        if (launcherSettings.homeMode == LauncherHomeMode.CLASSIC) {
            HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = pagerContentAlpha },
            beyondViewportPageCount = 1,
            flingBehavior = pagerFlingBehavior,
            userScrollEnabled = !editingWidgets,
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
                        favoriteShortcuts = favoriteShortcuts,
                        widgets = widgets,
                        widgetHost = widgetHost,
                        widgetManager = widgetManager,
                        foldingFeature = foldingFeature,
                        isLoading = isLoading,
                        backdropBlurEnabled = backdropBlurEnabled,
                        notificationCounts = notificationCounts,
                        nowPlaying = nowPlaying,
                        onLaunchApp = onLaunchApp,
                        onLaunchShortcut = onLaunchShortcut,
                        onLongPress = { selectedApp = it },
                        onAddWidget = { widgetPickerVisible = true },
                        onRemoveWidget = onRemoveWidget,
                        onUpdateWidget = onUpdateWidget,
                        onMoveWidget = onMoveWidget,
                        onMediaAction = onMediaAction,
                        editingWidgets = editingWidgets,
                        onEditingWidgetsChanged = { editingWidgets = it },
                    )

                    HOME_PAGE -> WallpaperPage(
                        showGestureCoach = showGestureCoach,
                        doubleTapEnabled = launcherSettings.doubleTapAction !=
                            LauncherGestureAction.NONE,
                        swipeDownEnabled = launcherSettings.swipeDownAction !=
                            LauncherGestureAction.NONE,
                        onDoubleTap = {
                            runHomeGesture(launcherSettings.doubleTapAction)
                        },
                        onSwipeDown = {
                            runHomeGesture(launcherSettings.swipeDownAction)
                        },
                        onSearch = {
                            openSearch()
                        },
                    )

                    APPS_PAGE -> AppsPage(
                        apps = snapshot.apps,
                        privateSpace = snapshot.privateSpace,
                        privateSpaceExpanded = privateSpaceExpanded,
                        isLoading = isLoading,
                        backdropBlurEnabled = backdropBlurEnabled,
                        isHomeRoleHeld = isHomeRoleHeld,
                        hasShortcutPermission = snapshot.hasShortcutPermission,
                        notificationCounts = notificationCounts,
                        foldingFeature = foldingFeature,
                        searchCornerRadius = launcherSettings.terminalCornerRadius,
                        onRequestHomeRole = onRequestHomeRole,
                        onLaunchApp = onLaunchApp,
                        onLongPress = { selectedApp = it },
                        onSetPrivateSpaceExpanded = onSetPrivateSpaceExpanded,
                        onSetPrivateSpaceLocked = onSetPrivateSpaceLocked,
                        onOpenSettings = { settingsVisible = true },
                        onSearch = {
                            openSearch()
                        },
                    )
                }
            }
        }

            AnimatedVisibility(
                visible = searchVisible,
                enter = fadeIn(animationSpec = tween(220)) +
                    scaleIn(
                        animationSpec = tween(260),
                        initialScale = 0.985f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                    ),
                exit = fadeOut(animationSpec = tween(BarelyMotionTokens.standard)),
            ) {
                SearchPage(
                initialQuery = initialSearchQuery,
                apps = snapshot.apps.filterNot { app ->
                    app.isPrivate && snapshot.privateSpace?.isLocked != false
                },
                shortcuts = snapshot.shortcuts.filterNot { shortcut ->
                    shortcut.owner.isPrivate && snapshot.privateSpace?.isLocked != false
                },
                canSearchShortcuts = snapshot.hasShortcutPermission,
                contacts = contacts,
                hasContactsPermission = hasContactsPermission,
                hasNotificationAccess = hasNotificationAccess,
                notificationDotsEnabled = launcherSettings.notificationDots,
                mediaControlsEnabled = launcherSettings.mediaControls,
                preferredAssistant = launcherSettings.preferredAssistant,
                notificationCounts = notificationCounts,
                foldingFeature = foldingFeature,
                backdropBlurEnabled = backdropBlurEnabled,
                searchCornerRadius = launcherSettings.terminalCornerRadius,
                onClose = { searchVisible = false },
                onDismissToHome = {
                    searchVisible = false
                    scope.launch { pagerState.animateScrollToPage(HOME_PAGE) }
                },
                recommendedApps = recommendedApps,
                recentAppSearches = if (launcherSettings.localSuggestions) {
                    recentAppSearches
                } else {
                    emptyList()
                },
                launcherSearchLearning = if (launcherSettings.localSuggestions) {
                    launcherSearchLearning
                } else {
                    emptyList()
                },
                showSearchHint = launcherSettings.showSearchHint,
                onDismissSearchHint = {
                    onSettingsChanged(launcherSettings.copy(showSearchHint = false))
                },
                onAppSearchCommitted = onAppSearchCommitted,
                onShortcutSearchCommitted = onShortcutSearchCommitted,
                onLaunchApp = {
                    searchVisible = false
                    onLaunchApp(it)
                },
                onLongPress = { selectedApp = it },
                onLaunchShortcut = {
                    searchVisible = false
                    onLaunchShortcut(it)
                },
                onExecuteCommand = { command ->
                    if (
                        command.action !is LauncherCommandAction.CopyResult &&
                        command.action !is LauncherCommandAction.RequestContactsPermission
                    ) {
                        searchVisible = false
                    }
                    onExecuteCommand(command)
                },
                )
            }
        } else {
            AnimatedVisibility(
                visible = !terminalAppsVisible,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(BarelyMotionTokens.fast)),
                exit = fadeOut(tween(BarelyMotionTokens.instant)),
            ) {
                TerminalHomePage(
                    apps = snapshot.apps.filterNot { app ->
                        app.isPrivate && snapshot.privateSpace?.isLocked != false
                    },
                    shortcuts = snapshot.shortcuts.filterNot { shortcut ->
                        shortcut.owner.isPrivate && snapshot.privateSpace?.isLocked != false
                    },
                    contacts = contacts,
                    hasContactsPermission = hasContactsPermission,
                    hasNotificationAccess = hasNotificationAccess,
                    notificationDotsEnabled = launcherSettings.notificationDots,
                    mediaControlsEnabled = launcherSettings.mediaControls,
                    preferredAssistant = launcherSettings.preferredAssistant,
                    launcherSearchLearning = if (launcherSettings.localSuggestions) {
                        launcherSearchLearning
                    } else {
                        emptyList()
                    },
                    homeRequestId = homeRequestId,
                    backgroundColor = terminalBackgroundColor,
                    backgroundOpacity = launcherSettings.terminalBackgroundOpacity,
                    topActionBackdrop = launcherSettings.terminalTopActionBackdrop,
                    cornerRadius = launcherSettings.terminalCornerRadius,
                    terminalAesthetic = launcherSettings.terminalAesthetic,
                    doubleTapEnabled = launcherSettings.doubleTapAction !=
                        LauncherGestureAction.NONE,
                    swipeDownEnabled = launcherSettings.swipeDownAction !=
                        LauncherGestureAction.NONE,
                    onLaunchApp = onLaunchApp,
                    onLongPress = { selectedApp = it },
                    onLaunchShortcut = onLaunchShortcut,
                    onExecuteCommand = onExecuteCommand,
                    onAppSearchCommitted = onAppSearchCommitted,
                    onShortcutSearchCommitted = onShortcutSearchCommitted,
                    onClearLocalHistory = onClearLocalHistory,
                    onOpenApps = { terminalAppsVisible = true },
                    onOpenSettings = { settingsVisible = true },
                    onSwitchToClassic = {
                        onSettingsChanged(
                            launcherSettings.copy(homeMode = LauncherHomeMode.CLASSIC),
                        )
                    },
                    onDoubleTap = {
                        runHomeGesture(launcherSettings.doubleTapAction)
                    },
                    onSwipeDown = {
                        runHomeGesture(launcherSettings.swipeDownAction)
                    },
                )
            }
            AnimatedVisibility(
                visible = terminalAppsVisible,
                enter = fadeIn(tween(200)) +
                    scaleIn(
                        animationSpec = tween(BarelyMotionTokens.deliberate),
                        initialScale = 0.985f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                    ),
                exit = fadeOut(tween(160)),
            ) {
                TerminalAppsPage(
                    apps = snapshot.apps,
                    privateSpace = snapshot.privateSpace,
                    privateSpaceExpanded = privateSpaceExpanded,
                    isLoading = isLoading,
                    backdropBlurEnabled = backdropBlurEnabled,
                    notificationCounts = notificationCounts,
                    foldingFeature = foldingFeature,
                    searchCornerRadius = launcherSettings.terminalCornerRadius,
                    onBack = { terminalAppsVisible = false },
                    onLaunchApp = onLaunchApp,
                    onLongPress = { selectedApp = it },
                    onSetPrivateSpaceExpanded = onSetPrivateSpaceExpanded,
                    onSetPrivateSpaceLocked = onSetPrivateSpaceLocked,
                    onSearch = { terminalAppsVisible = false },
                )
            }
        }
    }

    BackHandler(enabled = searchVisible) { searchVisible = false }
    BackHandler(enabled = terminalAppsVisible) { terminalAppsVisible = false }

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
            favoriteShortcutKeys = favoriteShortcuts.mapTo(mutableSetOf()) {
                it.searchTargetKey
            },
            onToggleShortcutFavorite = { shortcut ->
                onToggleShortcutFavorite(shortcut)
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

    if (settingsVisible) {
        LauncherSettingsPage(
            settings = launcherSettings,
            availableAssistants = availableAssistants,
            isHomeRoleHeld = isHomeRoleHeld,
            hasGestureAccess = hasGestureAccess,
            hasNotificationAccess = hasNotificationAccess,
            hasContactsPermission = hasContactsPermission,
            onDismiss = { settingsVisible = false },
            onSettingsChanged = onSettingsChanged,
            onRequestHomeRole = onRequestHomeRole,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onOpenNotificationAccess = onOpenNotificationAccess,
            onConfigureContacts = onConfigureContacts,
            onExportSettings = onExportSettings,
            onImportSettings = onImportSettings,
            onClearLocalHistory = onClearLocalHistory,
        )
    }

    BackHandler(enabled = settingsVisible) { settingsVisible = false }

    if (widgetPickerVisible) {
        WidgetPickerSheet(
            providers = widgetProviders,
            widgetManager = widgetManager,
            onDismiss = { widgetPickerVisible = false },
            onSelect = { provider ->
                widgetPickerVisible = false
                onAddWidget(provider)
            },
        )
    }
}

@Composable
private fun WallpaperPage(
    showGestureCoach: Boolean,
    doubleTapEnabled: Boolean,
    swipeDownEnabled: Boolean,
    onSearch: () -> Unit,
    onDoubleTap: () -> Unit,
    onSwipeDown: () -> Unit,
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
            .pointerInput(onDoubleTap, doubleTapEnabled) {
                detectTapGestures(
                    onDoubleTap = if (doubleTapEnabled) {
                        { _: Offset -> onDoubleTap() }
                    } else {
                        null
                    },
                )
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
                            dragDistance > threshold && swipeDownEnabled -> {
                                onSwipeDown()
                            }
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
                shape = BarelyVisualTokens.sheetShape,
                color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceRaised),
                contentColor = Color.White,
                tonalElevation = BarelyVisualTokens.raisedElevation,
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
private fun TerminalHomePage(
    apps: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    contacts: List<LauncherContact>,
    hasContactsPermission: Boolean,
    hasNotificationAccess: Boolean,
    notificationDotsEnabled: Boolean,
    mediaControlsEnabled: Boolean,
    preferredAssistant: AssistantPreference,
    launcherSearchLearning: List<LauncherSearchLearning>,
    homeRequestId: Int,
    backgroundColor: Color,
    backgroundOpacity: Float,
    topActionBackdrop: Boolean,
    cornerRadius: Int,
    terminalAesthetic: Boolean,
    doubleTapEnabled: Boolean,
    swipeDownEnabled: Boolean,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onExecuteCommand: (LauncherCommand) -> Unit,
    onAppSearchCommitted: (String, LauncherApp) -> Unit,
    onShortcutSearchCommitted: (String, LauncherShortcut) -> Unit,
    onClearLocalHistory: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchToClassic: () -> Unit,
    onDoubleTap: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var notificationDrag by remember { mutableFloatStateOf(0f) }
    var historyTargetOpen by remember { mutableStateOf(false) }
    var historyDragProgress by remember { mutableFloatStateOf(0f) }
    var historyDragging by remember { mutableStateOf(false) }
    val rootFocusRequester = remember { FocusRequester() }
    val inputFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val dragThreshold = with(LocalDensity.current) { 76.dp.toPx() }
    val historyFlingThreshold = with(LocalDensity.current) { 900.dp.toPx() }
    val animatedHistoryProgress by animateFloatAsState(
        targetValue = if (historyDragging) {
            historyDragProgress
        } else if (historyTargetOpen) {
            1f
        } else {
            0f
        },
        animationSpec = if (historyDragging) {
            snap()
        } else {
            spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "terminalHistoryProgress",
    )
    val historyEntries = remember(apps, shortcuts, launcherSearchLearning) {
        launcherSearchLearning
            .sortedByDescending(LauncherSearchLearning::lastSelectedAt)
            .distinctBy(LauncherSearchLearning::targetKey)
            .mapNotNull { learning ->
                val result = apps.firstOrNull {
                    it.searchTargetKey == learning.targetKey
                }?.let { app -> AppSearchResult(app, score = 0) }
                    ?: shortcuts.firstOrNull {
                        it.searchTargetKey == learning.targetKey
                    }?.let { shortcut -> ShortcutSearchResult(shortcut, score = 0) }
                result?.let { TerminalHistoryEntry(learning.query, it) }
            }
    }
    val suggestions = remember(
        query,
        apps,
        shortcuts,
        contacts,
        hasContactsPermission,
        hasNotificationAccess,
        notificationDotsEnabled,
        mediaControlsEnabled,
        preferredAssistant,
        launcherSearchLearning,
    ) {
        if (query.trimStart().startsWith(':')) {
            terminalBuiltInSuggestions(context, query)
        } else {
            buildRankedLauncherResults(
                context = context,
                query = query,
                apps = apps,
                shortcuts = shortcuts,
                contacts = contacts,
                hasContactsPermission = hasContactsPermission,
                hasNotificationAccess = hasNotificationAccess,
                notificationDotsEnabled = notificationDotsEnabled,
                mediaControlsEnabled = mediaControlsEnabled,
                preferredAssistant = preferredAssistant,
                launcherSearchLearning = launcherSearchLearning,
                limit = MAX_TERMINAL_RESULTS,
            ).map(TerminalSuggestion::SearchResult)
        }
    }.take(MAX_TERMINAL_RESULTS)

    LaunchedEffect(query) { selectedIndex = 0 }
    LaunchedEffect(homeRequestId) {
        query = ""
        historyTargetOpen = false
        historyDragging = false
        historyDragProgress = 0f
        keyboard?.hide()
        delay(40)
        rootFocusRequester.requestFocus()
    }

    fun moveSelection(delta: Int) {
        if (suggestions.isEmpty()) return
        selectedIndex = (selectedIndex + delta).coerceIn(0, suggestions.lastIndex)
    }

    fun executeSuggestion(suggestion: TerminalSuggestion?) {
        when (suggestion) {
            is TerminalSuggestion.BuiltIn -> when (suggestion.action) {
                TerminalBuiltInAction.OPEN_APPS -> {
                    query = ""
                    keyboard?.hide()
                    onOpenApps()
                }
                TerminalBuiltInAction.OPEN_SETTINGS -> {
                    query = ""
                    keyboard?.hide()
                    onOpenSettings()
                }
                TerminalBuiltInAction.SWITCH_TO_CLASSIC -> {
                    query = ""
                    keyboard?.hide()
                    onSwitchToClassic()
                }
                TerminalBuiltInAction.SHOW_HISTORY -> {
                    query = ""
                    keyboard?.hide()
                    historyTargetOpen = true
                }
                TerminalBuiltInAction.CLEAR_HISTORY -> {
                    query = ""
                    keyboard?.hide()
                    onClearLocalHistory()
                    historyTargetOpen = true
                }
            }
            is TerminalSuggestion.SearchResult -> when (val result = suggestion.result) {
                is AppSearchResult -> {
                    onAppSearchCommitted(query, result.app)
                    onLaunchApp(result.app)
                }
                is ShortcutSearchResult -> {
                    onShortcutSearchCommitted(query, result.shortcut)
                    onLaunchShortcut(result.shortcut)
                }
                is CommandSearchResult -> {
                    onExecuteCommand(result.command)
                    if (
                        result.command.action !is LauncherCommandAction.CopyResult &&
                        result.command.action !is LauncherCommandAction.RequestContactsPermission
                    ) {
                        query = ""
                        keyboard?.hide()
                    }
                }
            }
            null -> Unit
        }
    }

    BackHandler(enabled = historyTargetOpen || animatedHistoryProgress > 0.01f) {
        historyTargetOpen = false
        historyDragging = false
        historyDragProgress = 0f
        rootFocusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.DirectionUp -> {
                        moveSelection(-1)
                        true
                    }
                    event.key == Key.DirectionDown -> {
                        moveSelection(1)
                        true
                    }
                    event.key == Key.Enter -> {
                        executeSuggestion(suggestions.getOrNull(selectedIndex))
                        true
                    }
                    event.key == Key.Escape -> {
                        query = ""
                        keyboard?.hide()
                        rootFocusRequester.requestFocus()
                        true
                    }
                    event.key == Key.Backspace && query.isNotEmpty() -> {
                        query = query.dropLast(1)
                        true
                    }
                    event.isCtrlPressed && event.key == Key.K -> {
                        query = ""
                        inputFocusRequester.requestFocus()
                        true
                    }
                    !event.isCtrlPressed -> {
                        val character = event.nativeKeyEvent.unicodeChar
                            .takeIf { it > 0 }
                            ?.let { codePoint -> String(Character.toChars(codePoint)) }
                        if (!character.isNullOrEmpty() && !character.first().isISOControl()) {
                            query += character
                            inputFocusRequester.requestFocus()
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            },
    ) {
        val historyTravelPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        fun beginHistoryDrag() {
            if (!historyDragging) {
                historyDragProgress = animatedHistoryProgress
                historyDragging = true
            }
        }

        fun dragHistory(delta: Float) {
            beginHistoryDrag()
            historyDragProgress = terminalHistoryProgressAfterDrag(
                currentProgress = historyDragProgress,
                delta = delta,
                travelDistance = historyTravelPx,
            )
        }

        fun settleHistory(velocity: Float) {
            val open = shouldOpenTerminalHistory(
                progress = historyDragProgress,
                velocity = velocity,
                flingThreshold = historyFlingThreshold,
            )
            historyDragging = false
            historyTargetOpen = open
            historyDragProgress = if (open) 1f else 0f
        }

        val homeDragState = rememberDraggableState { delta ->
            if (
                query.isBlank() &&
                (delta < 0f || historyTargetOpen || historyDragProgress > 0f)
            ) {
                dragHistory(delta)
            } else if (delta > 0f) {
                notificationDrag += delta
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = backgroundOpacity.coerceIn(0f, 1f)))
                .pointerInput(doubleTapEnabled, onDoubleTap) {
                    detectTapGestures(
                        onDoubleTap = { if (doubleTapEnabled) onDoubleTap() },
                    )
                }
                .draggable(
                    state = homeDragState,
                    orientation = Orientation.Vertical,
                    onDragStarted = { notificationDrag = 0f },
                    onDragStopped = { velocity ->
                        if (historyDragging) {
                            keyboard?.hide()
                            settleHistory(velocity)
                        } else if (
                            notificationDrag > dragThreshold &&
                            swipeDownEnabled
                        ) {
                            onSwipeDown()
                        }
                        notificationDrag = 0f
                    },
                ),
        )

        AnimatedVisibility(
            visible = animatedHistoryProgress < 0.999f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 18.dp)
                .graphicsLayer {
                    translationY = -animatedHistoryProgress * historyTravelPx * 0.06f
                    alpha = 1f - animatedHistoryProgress
                },
        ) {
            Surface(
                shape = RoundedCornerShape(cornerRadius.dp),
                color = if (topActionBackdrop) {
                    Color.Black.copy(alpha = BarelyVisualTokens.surfaceSelected)
                }
                else Color.Transparent,
                contentColor = Color.White.copy(alpha = if (topActionBackdrop) 0.7f else 0.82f),
                border = if (topActionBackdrop) {
                    BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = BarelyVisualTokens.outlineSubtle),
                    )
                } else {
                    null
                },
            ) {
                Row(
                    modifier = if (topActionBackdrop) {
                        Modifier.padding(horizontal = 5.dp, vertical = 4.dp)
                    } else {
                        Modifier
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TerminalTopCommand(
                        icon = Icons.Outlined.Apps,
                        contentDescription = stringResource(R.string.apps),
                        onClick = {
                            query = ""
                            keyboard?.hide()
                            onOpenApps()
                        },
                    )
                    TerminalTopCommand(
                        icon = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.launcher_settings),
                        onClick = {
                            query = ""
                            keyboard?.hide()
                            onOpenSettings()
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = animatedHistoryProgress < 0.999f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = -animatedHistoryProgress * historyTravelPx * 0.06f
                    alpha = 1f - animatedHistoryProgress
                },
            enter = fadeIn(tween(BarelyMotionTokens.standard)) +
                slideInVertically(tween(BarelyMotionTokens.deliberate)) { it / 4 },
            exit = fadeOut(tween(BarelyMotionTokens.quick)) +
                slideOutVertically(tween(BarelyMotionTokens.standard)) { it / 4 },
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        horizontal = BarelyVisualTokens.paneHorizontalPadding,
                        vertical = 20.dp,
                    ),
            ) {
                AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = fadeIn(tween(BarelyMotionTokens.fast)) +
                    slideInVertically(tween(BarelyMotionTokens.standard)) { it / 5 },
                exit = fadeOut(tween(BarelyMotionTokens.instant)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEachIndexed { index, suggestion ->
                        TerminalSuggestionRow(
                            suggestion = suggestion,
                            selected = index == selectedIndex,
                            terminalAesthetic = terminalAesthetic,
                            onClick = { executeSuggestion(suggestion) },
                            onLongPress = {
                                val result = (suggestion as? TerminalSuggestion.SearchResult)?.result
                                if (result is AppSearchResult) onLongPress(result.app)
                            },
                        )
                    }
                }
            }
                if (suggestions.isNotEmpty()) Spacer(Modifier.height(10.dp))
                Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius.dp),
                color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceRaised),
                contentColor = Color.White,
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = BarelyVisualTokens.outline),
                ),
                ) {
                    Row(
                    modifier = Modifier
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 17.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (terminalAesthetic) {
                        Text(
                            ">",
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                textDirection = TextDirection.Ltr,
                            ),
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it.take(MAX_TERMINAL_QUERY_LENGTH) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(inputFocusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontFamily = if (terminalAesthetic) {
                                androidx.compose.ui.text.font.FontFamily.Monospace
                            } else {
                                androidx.compose.ui.text.font.FontFamily.Default
                            },
                            fontWeight = FontWeight.Normal,
                        ),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                executeSuggestion(suggestions.getOrNull(selectedIndex))
                            },
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (query.isEmpty()) {
                                    Text(
                                        stringResource(R.string.terminal_prompt_hint),
                                        color = Color.White.copy(
                                            alpha = BarelyVisualTokens.contentFaint,
                                        ),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = if (terminalAesthetic) {
                                                androidx.compose.ui.text.font.FontFamily.Monospace
                                            } else {
                                                androidx.compose.ui.text.font.FontFamily.Default
                                            },
                                            fontWeight = FontWeight.Normal,
                                        ),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.clear_search),
                                modifier = Modifier.size(17.dp),
                                tint = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                    }
                }
            }
        }

        if (
            historyTargetOpen ||
            historyDragging ||
            animatedHistoryProgress > 0.001f
        ) {
            TerminalHistoryDrawer(
                entries = historyEntries,
                backgroundColor = backgroundColor,
                backgroundOpacity = backgroundOpacity,
                progress = animatedHistoryProgress,
                terminalAesthetic = terminalAesthetic,
                transitionDistancePx = historyTravelPx,
                flingThreshold = historyFlingThreshold,
                onDragStarted = ::beginHistoryDrag,
                onDrag = ::dragHistory,
                onDragStopped = ::settleHistory,
                onClear = onClearLocalHistory,
                onOpen = { entry ->
                    when (val result = entry.result) {
                        is AppSearchResult -> {
                            onAppSearchCommitted(entry.query, result.app)
                            onLaunchApp(result.app)
                        }
                        is ShortcutSearchResult -> {
                            onShortcutSearchCommitted(entry.query, result.shortcut)
                            onLaunchShortcut(result.shortcut)
                        }
                        is CommandSearchResult -> Unit
                    }
                },
            )
        }
    }
}

@Composable
private fun TerminalTopCommand(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .combinedClickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
            .heightIn(min = 48.dp)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun TerminalSuggestionRow(
    suggestion: TerminalSuggestion,
    selected: Boolean,
    terminalAesthetic: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val title = when (suggestion) {
        is TerminalSuggestion.BuiltIn -> suggestion.title
        is TerminalSuggestion.SearchResult -> suggestion.result.label
    }
    val subtitle = when (suggestion) {
        is TerminalSuggestion.BuiltIn -> suggestion.subtitle
        is TerminalSuggestion.SearchResult -> when (val result = suggestion.result) {
            is AppSearchResult -> stringResource(R.string.terminal_app_result)
            is ShortcutSearchResult -> result.shortcut.owner.label
            is CommandSearchResult -> result.command.subtitle
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BarelyVisualTokens.compactRowShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        shape = BarelyVisualTokens.compactRowShape,
        color = if (selected) {
            Color.Black.copy(alpha = BarelyVisualTokens.surfaceSelected)
        } else {
            Color.Black.copy(alpha = BarelyVisualTokens.surfaceIdle)
        },
        contentColor = Color.White,
        border = if (selected) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (terminalAesthetic) {
                Text(
                    if (selected) ">" else " ",
                    modifier = Modifier.width(22.dp),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        textDirection = TextDirection.Ltr,
                    ),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = if (terminalAesthetic) {
                            androidx.compose.ui.text.font.FontFamily.Monospace
                        } else {
                            androidx.compose.ui.text.font.FontFamily.Default
                        },
                    ),
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.52f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalHistoryDrawer(
    entries: List<TerminalHistoryEntry>,
    backgroundColor: Color,
    backgroundOpacity: Float,
    progress: Float,
    terminalAesthetic: Boolean,
    transitionDistancePx: Float,
    flingThreshold: Float,
    onDragStarted: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragStopped: (Float) -> Unit,
    onClear: () -> Unit,
    onOpen: (TerminalHistoryEntry) -> Unit,
) {
    val listState = rememberLazyListState()
    val currentProgress by rememberUpdatedState(progress)
    val currentTransitionDistance by rememberUpdatedState(transitionDistancePx)
    val currentFlingThreshold by rememberUpdatedState(flingThreshold)
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragStopped by rememberUpdatedState(onDragStopped)
    val historyDragState = rememberDraggableState { delta -> currentOnDrag(delta) }
    val closeConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    available.y < 0f &&
                    currentProgress < 1f &&
                    !listState.canScrollBackward
                ) {
                    currentOnDragStarted()
                    currentOnDrag(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    available.y > 0f &&
                    !listState.canScrollBackward
                ) {
                    currentOnDragStarted()
                    currentOnDrag(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (
                    !listState.canScrollBackward &&
                    (currentProgress < 1f || available.y >= currentFlingThreshold)
                ) {
                    currentOnDragStopped(available.y)
                    return available
                }
                return Velocity.Zero
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(closeConnection)
            .graphicsLayer {
                translationY = (1f - progress.coerceIn(0f, 1f)) * currentTransitionDistance
            }
            .background(backgroundColor.copy(alpha = backgroundOpacity.coerceIn(0f, 1f)))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.24f),
                        Color.Black.copy(alpha = 0.4f),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = BarelyVisualTokens.paneHorizontalPadding,
                    vertical = 20.dp,
                ),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        state = historyDragState,
                        orientation = Orientation.Vertical,
                        onDragStarted = { currentOnDragStarted() },
                        onDragStopped = { velocity -> currentOnDragStopped(velocity) },
                    ),
                shape = BarelyVisualTokens.controlShape,
                color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceRaised),
                contentColor = Color.White,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 54.dp)
                        .padding(start = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (terminalAesthetic) {
                            "> ${stringResource(R.string.recent_searches)}"
                        } else {
                            stringResource(R.string.recent_searches)
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = if (terminalAesthetic) {
                                androidx.compose.ui.text.font.FontFamily.Monospace
                            } else {
                                androidx.compose.ui.text.font.FontFamily.Default
                            },
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                    if (entries.isNotEmpty()) {
                        TerminalTopCommand(
                            icon = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(
                                R.string.settings_clear_local_history,
                            ),
                            onClick = onClear,
                        )
                    } else {
                        Spacer(Modifier.width(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.terminal_history_empty),
                        modifier = Modifier.padding(horizontal = 28.dp),
                        color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(entries, key = { "terminal_history_${it.result.key}" }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(BarelyVisualTokens.compactRowShape)
                                .combinedClickable(onClick = { onOpen(entry) })
                                .padding(
                                    horizontal = BarelyVisualTokens.controlHorizontalPadding,
                                    vertical = 12.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (terminalAesthetic) "> ${entry.query}" else entry.query,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = if (terminalAesthetic) {
                                            androidx.compose.ui.text.font.FontFamily.Monospace
                                        } else {
                                            androidx.compose.ui.text.font.FontFamily.Default
                                        },
                                    ),
                                )
                                Text(
                                    if (terminalAesthetic) {
                                        "  ↳ ${entry.result.label}"
                                    } else {
                                        entry.result.label
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White.copy(alpha = 0.52f),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = if (terminalAesthetic) {
                                            androidx.compose.ui.text.font.FontFamily.Monospace
                                        } else {
                                            androidx.compose.ui.text.font.FontFamily.Default
                                        },
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalAppsPage(
    apps: List<LauncherApp>,
    privateSpace: LauncherProfile?,
    privateSpaceExpanded: Boolean,
    isLoading: Boolean,
    backdropBlurEnabled: Boolean,
    notificationCounts: Map<String, Int>,
    foldingFeature: FoldingFeature?,
    searchCornerRadius: Int,
    onBack: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onSetPrivateSpaceExpanded: (Boolean) -> Unit,
    onSetPrivateSpaceLocked: (LauncherProfile, Boolean) -> Unit,
    onSearch: () -> Unit,
) {
    PageSurface(isLoading = isLoading, backdropBlurEnabled = backdropBlurEnabled) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 24.dp, top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.terminal_all_apps),
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                apps.size.toString(),
                color = Color.White.copy(alpha = 0.56f),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Box(Modifier.weight(1f)) {
            AppGrid(
                apps = apps,
                privateSpace = privateSpace,
                privateSpaceExpanded = privateSpaceExpanded,
                onLaunchApp = onLaunchApp,
                onLongPress = onLongPress,
                onSetPrivateSpaceExpanded = onSetPrivateSpaceExpanded,
                onSetPrivateSpaceLocked = onSetPrivateSpaceLocked,
                notificationCounts = notificationCounts,
                foldingFeature = foldingFeature,
            )
        }
        SearchLauncherBar(
            onClick = onSearch,
            cornerRadius = searchCornerRadius,
        )
    }
}

@Composable
private fun FavoritesPage(
    favorites: List<LauncherApp>,
    favoriteShortcuts: List<LauncherShortcut>,
    widgets: List<WidgetPlacement>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    foldingFeature: FoldingFeature?,
    isLoading: Boolean,
    backdropBlurEnabled: Boolean,
    notificationCounts: Map<String, Int>,
    nowPlaying: NowPlaying?,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onUpdateWidget: (Int, Int, Int, WidgetHorizontalPosition) -> Unit,
    onMoveWidget: (Int, Int) -> Unit,
    onMediaAction: (MediaAction) -> Unit,
    editingWidgets: Boolean,
    onEditingWidgetsChanged: (Boolean) -> Unit,
) {
    LaunchedEffect(widgets.isEmpty()) {
        if (widgets.isEmpty()) onEditingWidgetsChanged(false)
    }
    PageSurface(isLoading = isLoading, backdropBlurEnabled = backdropBlurEnabled) {
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
                        favoriteItems(
                            favorites,
                            favoriteShortcuts,
                            notificationCounts,
                            onLaunchApp,
                            onLaunchShortcut,
                            onLongPress,
                        )
                    }
                    Spacer(Modifier.width(paneGap))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp),
                    ) {
                        mediaItem(nowPlaying, onMediaAction)
                        widgetItems(
                            widgets = widgets,
                            widgetHost = widgetHost,
                            widgetManager = widgetManager,
                            editingWidgets = editingWidgets,
                            onToggleEditing = {
                                onEditingWidgetsChanged(!editingWidgets)
                            },
                            onAddWidget = onAddWidget,
                            onRemoveWidget = onRemoveWidget,
                            onUpdateWidget = onUpdateWidget,
                            onMoveWidget = onMoveWidget,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp),
                ) {
                    favoriteItems(
                        favorites,
                        favoriteShortcuts,
                        notificationCounts,
                        onLaunchApp,
                        onLaunchShortcut,
                        onLongPress,
                    )
                    mediaItem(nowPlaying, onMediaAction)
                    widgetItems(
                        widgets = widgets,
                        widgetHost = widgetHost,
                        widgetManager = widgetManager,
                        editingWidgets = editingWidgets,
                        onToggleEditing = {
                            onEditingWidgetsChanged(!editingWidgets)
                        },
                        onAddWidget = onAddWidget,
                        onRemoveWidget = onRemoveWidget,
                        onUpdateWidget = onUpdateWidget,
                        onMoveWidget = onMoveWidget,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.favoriteItems(
    favorites: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    notificationCounts: Map<String, Int>,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
) {
    if (favorites.isEmpty() && shortcuts.isEmpty()) {
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
                    tint = Color.White.copy(alpha = BarelyVisualTokens.contentPrimary),
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
                notificationCount = notificationCounts[app.packageName] ?: 0,
                onClick = { onLaunchApp(app) },
                onLongPress = { onLongPress(app) },
            )
        }
        items(shortcuts, key = { it.searchTargetKey }) { shortcut ->
            FavoriteShortcutTile(
                shortcut = shortcut,
                onClick = { onLaunchShortcut(shortcut) },
                onLongPress = { onLongPress(shortcut.owner) },
            )
        }
    }
}

@Composable
private fun FavoriteShortcutTile(
    shortcut: LauncherShortcut,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(BarelyVisualTokens.controlShape)
            .secondaryClickable(onLongPress)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                shortcut.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                shortcut.owner.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Icon(
            Icons.Outlined.Bolt,
            contentDescription = stringResource(R.string.shortcuts),
            modifier = Modifier.size(18.dp),
            tint = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
        )
    }
}

private fun LazyListScope.mediaItem(
    nowPlaying: NowPlaying?,
    onMediaAction: (MediaAction) -> Unit,
) {
    if (nowPlaying == null) return
    item(key = "now_playing:${nowPlaying.packageName}") {
        NowPlayingCard(nowPlaying = nowPlaying, onMediaAction = onMediaAction)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun NowPlayingCard(
    nowPlaying: NowPlaying,
    onMediaAction: (MediaAction) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        shape = BarelyVisualTokens.widgetShape,
        color = Color.Black.copy(alpha = 0.46f),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    nowPlaying.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                nowPlaying.artist?.let { artist ->
                    Text(
                        artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (nowPlaying.canSkipPrevious) {
                IconButton(onClick = { onMediaAction(MediaAction.PREVIOUS) }) {
                    Icon(
                        Icons.Outlined.SkipPrevious,
                        contentDescription = stringResource(R.string.media_previous),
                    )
                }
            }
            IconButton(onClick = { onMediaAction(MediaAction.PLAY_PAUSE) }) {
                Icon(
                    if (nowPlaying.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (nowPlaying.isPlaying) {
                        stringResource(R.string.media_pause)
                    } else {
                        stringResource(R.string.media_play)
                    },
                )
            }
            if (nowPlaying.canSkipNext) {
                IconButton(onClick = { onMediaAction(MediaAction.NEXT) }) {
                    Icon(
                        Icons.Outlined.SkipNext,
                        contentDescription = stringResource(R.string.media_next),
                    )
                }
            }
        }
    }
}

private fun LazyListScope.widgetItems(
    widgets: List<WidgetPlacement>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    editingWidgets: Boolean,
    onToggleEditing: () -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onUpdateWidget: (Int, Int, Int, WidgetHorizontalPosition) -> Unit,
    onMoveWidget: (Int, Int) -> Unit,
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
            if (widgets.isNotEmpty()) {
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
                        if (editingWidgets) Icons.Outlined.Done else Icons.Outlined.Edit,
                        contentDescription = stringResource(
                            if (editingWidgets) R.string.finish_editing_widgets
                            else R.string.edit_widgets,
                        ),
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

    if (widgets.isEmpty()) {
        item(key = "empty_widgets") {
            Text(
                stringResource(R.string.widgets_empty_message),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    } else {
        if (editingWidgets) {
            item(key = "widget_edit_hint") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    shape = BarelyVisualTokens.cardShape,
                    color = Color.Black.copy(alpha = 0.34f),
                    contentColor = Color.White.copy(alpha = BarelyVisualTokens.contentPrimary),
                ) {
                    Text(
                        stringResource(R.string.widget_edit_hint),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        if (editingWidgets) {
            items(widgets, key = { "widget_edit_${it.widgetId}" }) { widget ->
                val widgetIndex = widgets.indexOfFirst { it.widgetId == widget.widgetId }
                HostedWidget(
                    placement = widget,
                    widgetHost = widgetHost,
                    widgetManager = widgetManager,
                    editing = true,
                    canMoveUp = widgetIndex > 0,
                    canMoveDown = widgetIndex in 0 until widgets.lastIndex,
                    onRemove = { onRemoveWidget(widget.widgetId) },
                    onMove = { direction -> onMoveWidget(widget.widgetId, direction) },
                    onUpdate = { widthSpan, heightDp, position ->
                        onUpdateWidget(widget.widgetId, widthSpan, heightDp, position)
                    },
                )
                Spacer(Modifier.height(14.dp))
            }
        } else {
            val rows = packWidgetRows(widgets)
            items(rows, key = { row ->
                "widget_row_${row.joinToString("_") { it.widgetId.toString() }}"
            }) { row ->
                PackedWidgetRow(
                    widgets = row,
                    widgetHost = widgetHost,
                    widgetManager = widgetManager,
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun PackedWidgetRow(
    widgets: List<WidgetPlacement>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
) {
    val occupiedSpans = widgets.sumOf { widget ->
        widget.widthSpan.coerceIn(
            WidgetPlacement.MIN_WIDGET_SPAN,
            WidgetPlacement.MAX_WIDGET_SPAN,
        )
    }.coerceAtMost(WidgetPlacement.MAX_WIDGET_SPAN)
    val remainingSpans = WidgetPlacement.MAX_WIDGET_SPAN - occupiedSpans
    val rowPosition = widgets.singleOrNull()?.horizontalPosition
        ?: WidgetHorizontalPosition.CENTER
    val leadingSpans = when (rowPosition) {
        WidgetHorizontalPosition.START -> 0f
        WidgetHorizontalPosition.CENTER -> remainingSpans / 2f
        WidgetHorizontalPosition.END -> remainingSpans.toFloat()
    }
    val trailingSpans = remainingSpans - leadingSpans

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (leadingSpans > 0f) Spacer(Modifier.weight(leadingSpans))
        widgets.forEach { widget ->
            Box(
                modifier = Modifier.weight(
                    widget.widthSpan.coerceIn(
                        WidgetPlacement.MIN_WIDGET_SPAN,
                        WidgetPlacement.MAX_WIDGET_SPAN,
                    ).toFloat(),
                ),
            ) {
                HostedWidget(
                    placement = widget,
                    widgetHost = widgetHost,
                    widgetManager = widgetManager,
                    editing = false,
                    fillContainer = true,
                    canMoveUp = false,
                    canMoveDown = false,
                    onRemove = {},
                    onMove = { _ -> },
                    onUpdate = { _, _, _ -> },
                )
            }
        }
        if (trailingSpans > 0f) Spacer(Modifier.weight(trailingSpans))
    }
}

@Composable
private fun HostedWidget(
    placement: WidgetPlacement,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    editing: Boolean,
    fillContainer: Boolean = false,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRemove: () -> Unit,
    onMove: (Int) -> Unit,
    onUpdate: (Int, Int, WidgetHorizontalPosition) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val widgetId = placement.widgetId
    val info = remember(widgetId) { widgetManager.getAppWidgetInfo(widgetId) } ?: return
    val hostView = remember(widgetId, info) {
        widgetHost.createView(context, widgetId, info)
    }
    val resizeWidgetDescription = stringResource(R.string.resize_widget)
    val increaseWidgetWidth = stringResource(R.string.increase_widget_width)
    val decreaseWidgetWidth = stringResource(R.string.decrease_widget_width)
    val increaseWidgetHeight = stringResource(R.string.increase_widget_height)
    val decreaseWidgetHeight = stringResource(R.string.decrease_widget_height)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .animateContentSize(),
    ) {
        val supportsHorizontalResize =
            info.resizeMode.and(AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0
        val supportsVerticalResize =
            info.resizeMode.and(AppWidgetProviderInfo.RESIZE_VERTICAL) != 0
        val cellWidth = maxWidth / WidgetPlacement.MAX_WIDGET_SPAN
        val providerMinWidth = with(density) {
            (info.minResizeWidth.takeIf { it > 0 } ?: info.minWidth).toDp()
        }.coerceAtLeast(1.dp)
        val minimumWidthSpan = if (supportsHorizontalResize) {
            ceil(providerMinWidth.value / cellWidth.value).toInt().coerceIn(
                WidgetPlacement.MIN_WIDGET_SPAN,
                WidgetPlacement.MAX_WIDGET_SPAN,
            )
        } else {
            WidgetPlacement.MAX_WIDGET_SPAN
        }
        val providerMinHeight = with(density) {
            (info.minResizeHeight.takeIf { it > 0 } ?: info.minHeight).toDp()
        }.coerceIn(MIN_WIDGET_HEIGHT_DP.dp, MAX_WIDGET_HEIGHT_DP.dp)
        val preferredHeight = with(density) { info.minHeight.toDp() }
            .coerceIn(providerMinHeight, MAX_WIDGET_HEIGHT_DP.dp)
        var previewWidthDp by remember(
            widgetId,
            placement.widthSpan,
            maxWidth.value,
            minimumWidthSpan,
        ) {
            mutableFloatStateOf(
                if (fillContainer) {
                    maxWidth.value
                } else {
                    cellWidth.value * placement.widthSpan.coerceIn(
                        minimumWidthSpan,
                        WidgetPlacement.MAX_WIDGET_SPAN,
                    )
                },
            )
        }
        var previewHeightDp by remember(
            widgetId,
            placement.heightDp,
            preferredHeight.value,
        ) {
            mutableFloatStateOf(
                if (
                    supportsVerticalResize &&
                    placement.heightDp != WidgetPlacement.AUTO_WIDGET_HEIGHT
                ) {
                    placement.heightDp.toFloat()
                } else {
                    preferredHeight.value
                },
            )
        }
        val widthSpan = (previewWidthDp / cellWidth.value).roundToInt().coerceIn(
            minimumWidthSpan,
            WidgetPlacement.MAX_WIDGET_SPAN,
        )
        fun applyWidthSpan(targetSpan: Int): Boolean {
            val updatedSpan = targetSpan.coerceIn(
                minimumWidthSpan,
                WidgetPlacement.MAX_WIDGET_SPAN,
            )
            if (updatedSpan == widthSpan) return false
            previewWidthDp = cellWidth.value * updatedSpan
            onUpdate(
                updatedSpan,
                previewHeightDp.roundToInt(),
                placement.horizontalPosition,
            )
            return true
        }
        fun applyHeight(targetHeightDp: Float): Boolean {
            val updatedHeight = targetHeightDp.coerceIn(
                providerMinHeight.value,
                MAX_WIDGET_HEIGHT_DP.toFloat(),
            )
            if (updatedHeight.roundToInt() == previewHeightDp.roundToInt()) return false
            previewHeightDp = updatedHeight
            onUpdate(
                widthSpan,
                updatedHeight.roundToInt(),
                placement.horizontalPosition,
            )
            return true
        }
        val widgetWidth = if (fillContainer) {
            maxWidth
        } else {
            previewWidthDp.dp.coerceIn(cellWidth * minimumWidthSpan, maxWidth)
        }
        val widgetHeight = if (supportsVerticalResize) {
            previewHeightDp.dp.coerceIn(providerMinHeight, MAX_WIDGET_HEIGHT_DP.dp)
        } else {
            preferredHeight
        }
        val availableWidthDp = maxWidth.value
        val alignment = when (placement.horizontalPosition) {
            WidgetHorizontalPosition.START -> Alignment.CenterStart
            WidgetHorizontalPosition.CENTER -> Alignment.Center
            WidgetHorizontalPosition.END -> Alignment.CenterEnd
        }
        val shape = BarelyVisualTokens.widgetShape
        val widthDp = widgetWidth.value.coerceAtLeast(1f)
        val heightDp = widgetHeight.value.coerceAtLeast(1f)
        val reorderThresholdPx = with(density) { 36.dp.toPx() }
        var reorderDrag by remember(widgetId) { mutableFloatStateOf(0f) }

        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(widgetHeight),
            ) {
                Box(
                    modifier = Modifier
                        .width(widgetWidth)
                        .height(widgetHeight)
                        .align(alignment)
                        .clip(shape)
                        .then(
                            if (editing) {
                                Modifier.border(
                                    1.dp,
                                    Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                                    shape,
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    AndroidView(
                        factory = { hostView },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                view.updateAppWidgetSize(
                                    Bundle.EMPTY,
                                    listOf(SizeF(widthDp, heightDp)),
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                view.updateAppWidgetSize(
                                    null,
                                    widthDp.roundToInt(),
                                    heightDp.roundToInt(),
                                    widthDp.roundToInt(),
                                    heightDp.roundToInt(),
                                )
                            }
                        },
                    )

                    if (editing && supportsHorizontalResize) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(24.dp)
                                .padding(bottom = 18.dp)
                                .semantics {
                                    contentDescription = resizeWidgetDescription
                                    customActions = buildList {
                                        if (widthSpan > minimumWidthSpan) {
                                            add(
                                                CustomAccessibilityAction(decreaseWidgetWidth) {
                                                    applyWidthSpan(widthSpan - 1)
                                                },
                                            )
                                        }
                                        if (widthSpan < WidgetPlacement.MAX_WIDGET_SPAN) {
                                            add(
                                                CustomAccessibilityAction(increaseWidgetWidth) {
                                                    applyWidthSpan(widthSpan + 1)
                                                },
                                            )
                                        }
                                    }
                                }
                                .focusable()
                                .pointerInput(
                                    widgetId,
                                    placement.widthSpan,
                                    availableWidthDp,
                                ) {
                                    var startWidthDp = 0f
                                    var dragDistancePx = 0f
                                    detectDragGestures(
                                        onDragStart = {
                                            startWidthDp = previewWidthDp
                                            dragDistancePx = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragDistancePx += amount.x
                                            previewWidthDp = (
                                                startWidthDp + dragDistancePx / density.density
                                            ).coerceIn(
                                                cellWidth.value * minimumWidthSpan,
                                                availableWidthDp,
                                            )
                                        },
                                        onDragEnd = {
                                            val resizedSpan = (
                                                previewWidthDp / cellWidth.value
                                            ).roundToInt().coerceIn(
                                                minimumWidthSpan,
                                                WidgetPlacement.MAX_WIDGET_SPAN,
                                            )
                                            previewWidthDp = cellWidth.value * resizedSpan
                                            onUpdate(
                                                resizedSpan,
                                                previewHeightDp.roundToInt(),
                                                placement.horizontalPosition,
                                            )
                                        },
                                        onDragCancel = {
                                            previewWidthDp = cellWidth.value *
                                                placement.widthSpan.coerceIn(
                                                    minimumWidthSpan,
                                                    WidgetPlacement.MAX_WIDGET_SPAN,
                                                )
                                        },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .size(width = 4.dp, height = 48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.White.copy(alpha = BarelyVisualTokens.contentStrong),
                                    ),
                            )
                        }
                    }

                    if (editing && supportsVerticalResize) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(24.dp)
                                .padding(end = 18.dp)
                                .semantics {
                                    contentDescription = resizeWidgetDescription
                                    customActions = buildList {
                                        if (previewHeightDp > providerMinHeight.value) {
                                            add(
                                                CustomAccessibilityAction(decreaseWidgetHeight) {
                                                    applyHeight(
                                                        previewHeightDp - WIDGET_HEIGHT_STEP_DP,
                                                    )
                                                },
                                            )
                                        }
                                        if (previewHeightDp < MAX_WIDGET_HEIGHT_DP.toFloat()) {
                                            add(
                                                CustomAccessibilityAction(increaseWidgetHeight) {
                                                    applyHeight(
                                                        previewHeightDp + WIDGET_HEIGHT_STEP_DP,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                                .focusable()
                                .pointerInput(
                                    widgetId,
                                    placement.heightDp,
                                    providerMinHeight.value,
                                ) {
                                    var startHeightDp = 0f
                                    var dragDistancePx = 0f
                                    detectDragGestures(
                                        onDragStart = {
                                            startHeightDp = previewHeightDp
                                            dragDistancePx = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragDistancePx += amount.y
                                            previewHeightDp = (
                                                startHeightDp + dragDistancePx / density.density
                                            ).coerceIn(
                                                providerMinHeight.value,
                                                MAX_WIDGET_HEIGHT_DP.toFloat(),
                                            )
                                        },
                                        onDragEnd = {
                                            onUpdate(
                                                widthSpan,
                                                previewHeightDp.roundToInt(),
                                                placement.horizontalPosition,
                                            )
                                        },
                                        onDragCancel = {
                                            previewHeightDp = if (
                                                placement.heightDp !=
                                                WidgetPlacement.AUTO_WIDGET_HEIGHT
                                            ) {
                                                placement.heightDp.toFloat()
                                            } else {
                                                preferredHeight.value
                                            }
                                        },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .size(width = 48.dp, height = 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.White.copy(alpha = BarelyVisualTokens.contentStrong),
                                    ),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = editing,
                enter = fadeIn(tween(BarelyMotionTokens.fast)) +
                    slideInVertically(tween(BarelyMotionTokens.standard)) { -it / 3 },
                exit = fadeOut(tween(BarelyMotionTokens.instant)),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = BarelyVisualTokens.floatingPanelShape,
                    color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceControl),
                    contentColor = Color.White,
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = BarelyVisualTokens.outlineSubtle),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { onMove(-1) },
                            enabled = canMoveUp,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Outlined.ExpandLess,
                                contentDescription = stringResource(R.string.move_widget_up),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        Icon(
                            Icons.Outlined.DragHandle,
                            contentDescription = stringResource(R.string.move_widget),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(14.dp)
                                .pointerInput(widgetId, canMoveUp, canMoveDown) {
                                    detectVerticalDragGestures(
                                        onVerticalDrag = { change, amount ->
                                            change.consume()
                                            reorderDrag += amount
                                        },
                                        onDragEnd = {
                                            when {
                                                reorderDrag < -reorderThresholdPx && canMoveUp -> onMove(-1)
                                                reorderDrag > reorderThresholdPx && canMoveDown -> onMove(1)
                                            }
                                            reorderDrag = 0f
                                        },
                                        onDragCancel = { reorderDrag = 0f },
                                    )
                                },
                        )
                        IconButton(
                            onClick = { onMove(1) },
                            enabled = canMoveDown,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Outlined.ExpandMore,
                                contentDescription = stringResource(R.string.move_widget_down),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        Text(
                            stringResource(
                                R.string.widget_dimensions,
                                widthSpan,
                                ceil(widgetHeight.value / WIDGET_HEIGHT_STEP_DP).toInt(),
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        IconButton(
                            onClick = {
                                onUpdate(
                                    widthSpan,
                                    placement.heightDp,
                                    placement.horizontalPosition.next(),
                                )
                            },
                            enabled = widthSpan < WidgetPlacement.MAX_WIDGET_SPAN,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Outlined.SwapHoriz,
                                contentDescription = stringResource(R.string.position_widget),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.remove_widget),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<LauncherApp>,
    privateSpace: LauncherProfile?,
    privateSpaceExpanded: Boolean,
    isLoading: Boolean,
    backdropBlurEnabled: Boolean,
    isHomeRoleHeld: Boolean,
    hasShortcutPermission: Boolean,
    notificationCounts: Map<String, Int>,
    foldingFeature: FoldingFeature?,
    searchCornerRadius: Int,
    onRequestHomeRole: () -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onSetPrivateSpaceExpanded: (Boolean) -> Unit,
    onSetPrivateSpaceLocked: (LauncherProfile, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onSearch: () -> Unit,
) {
    PageSurface(isLoading = isLoading, backdropBlurEnabled = backdropBlurEnabled) {
        PageHeader(
            title = stringResource(R.string.apps),
            trailing = apps.size.toString(),
            onSettings = onOpenSettings,
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
                privateSpace = privateSpace,
                privateSpaceExpanded = privateSpaceExpanded,
                onLaunchApp = onLaunchApp,
                onLongPress = onLongPress,
                onSetPrivateSpaceExpanded = onSetPrivateSpaceExpanded,
                onSetPrivateSpaceLocked = onSetPrivateSpaceLocked,
                notificationCounts = notificationCounts,
                foldingFeature = foldingFeature,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 8.dp,
                ),
            )
        }
        SearchLauncherBar(
            onClick = onSearch,
            cornerRadius = searchCornerRadius,
        )
    }
}

@Composable
private fun PageSurface(
    isLoading: Boolean,
    backdropBlurEnabled: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = if (backdropBlurEnabled) {
        Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimTopWithBlur),
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimMiddleWithBlur),
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimBottomWithBlur),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimTopFallback),
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimMiddleFallback),
                Color.Black.copy(alpha = BarelyVisualTokens.pageScrimBottomFallback),
            ),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdrop),
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
                        trackColor = Color.White.copy(alpha = BarelyVisualTokens.outline),
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
    onSettings: (() -> Unit)? = null,
) {
    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
            trailing?.let {
                Text(
                    it,
                    color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            onSettings?.let { openSettings ->
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = openSettings,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.launcher_settings),
                        modifier = Modifier.size(21.dp),
                        tint = Color.White.copy(alpha = BarelyVisualTokens.contentHigh),
                    )
                }
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
        modifier = Modifier.padding(
            horizontal = BarelyVisualTokens.contentHorizontalPadding,
            vertical = 4.dp,
        ),
        shape = BarelyVisualTokens.dialogShape,
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
                    color = Color.White.copy(alpha = BarelyVisualTokens.contentSecondary),
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
    privateSpace: LauncherProfile?,
    privateSpaceExpanded: Boolean,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onSetPrivateSpaceExpanded: (Boolean) -> Unit,
    onSetPrivateSpaceLocked: (LauncherProfile, Boolean) -> Unit,
    notificationCounts: Map<String, Int>,
    foldingFeature: FoldingFeature?,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
) {
    val regularApps = remember(apps) { apps.filterNot(LauncherApp::isPrivate) }
    val privateApps = remember(apps) { apps.filter(LauncherApp::isPrivate) }
    val showPrivateEntryPoint = privateSpace != null && !(
        privateSpace.isLocked && privateSpace.hideEntryPointWhenLocked
    )
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
            gridItems(regularApps, key = { it.key }) { app ->
                AppTile(
                    app = app,
                    notificationCount = notificationCounts[app.packageName] ?: 0,
                    onClick = { onLaunchApp(app) },
                    onLongPress = { onLongPress(app) },
                )
            }
            if (showPrivateEntryPoint) {
                item(
                    key = "private_space_header",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    PrivateSpaceHeader(
                        profile = privateSpace,
                        expanded = privateSpaceExpanded,
                        onSetExpanded = onSetPrivateSpaceExpanded,
                        onSetLocked = onSetPrivateSpaceLocked,
                    )
                }
                if (privateSpaceExpanded) {
                    if (privateSpace.isLocked) {
                        item(
                            key = "private_space_locked",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            Text(
                                stringResource(R.string.private_space_locked_message),
                                modifier = Modifier.padding(
                                    horizontal = BarelyVisualTokens.contentHorizontalPadding,
                                    vertical = 14.dp,
                                ),
                                color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else {
                        gridItems(privateApps, key = { "private:${it.key}" }) { app ->
                            AppTile(
                                app = app,
                                notificationCount = notificationCounts[app.packageName] ?: 0,
                                onClick = { onLaunchApp(app) },
                                onLongPress = { onLongPress(app) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateSpaceHeader(
    profile: LauncherProfile,
    expanded: Boolean,
    onSetExpanded: (Boolean) -> Unit,
    onSetLocked: (LauncherProfile, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .clip(BarelyVisualTokens.cardShape)
            .background(Color.Black.copy(alpha = 0.26f))
            .animateContentSize()
            .combinedClickable(onClick = { onSetExpanded(!expanded) })
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (profile.isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color.White.copy(alpha = BarelyVisualTokens.contentHigh),
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.private_space),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (profile.isLocked) {
                    stringResource(R.string.private_space_locked)
                } else {
                    stringResource(R.string.private_space_unlocked)
                },
                color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        IconButton(onClick = { onSetLocked(profile, !profile.isLocked) }) {
            Icon(
                if (profile.isLocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                contentDescription = if (profile.isLocked) {
                    stringResource(R.string.unlock_private_space)
                } else {
                    stringResource(R.string.lock_private_space)
                },
            )
        }
        IconButton(onClick = { onSetExpanded(!expanded) }) {
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.hide_private_space_apps)
                } else {
                    stringResource(R.string.show_private_space_apps)
                },
            )
        }
    }
}

@Composable
private fun SearchLauncherBar(
    onClick: () -> Unit,
    cornerRadius: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(
                    horizontal = BarelyVisualTokens.controlHorizontalPadding,
                    vertical = 10.dp,
                )
                .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                .fillMaxWidth()
                .height(56.dp)
                .combinedClickable(onClick = onClick),
            shape = RoundedCornerShape(cornerRadius.dp),
            color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceSelected),
            contentColor = Color.White,
            border = BorderStroke(
                1.dp,
                Color.White.copy(alpha = BarelyVisualTokens.outline),
            ),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = BarelyVisualTokens.contentHorizontalPadding,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = Color.White.copy(alpha = BarelyVisualTokens.contentPrimary),
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
}

@Composable
private fun AppTile(
    app: LauncherApp,
    notificationCount: Int = 0,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val appActionsLabel = stringResource(R.string.actions_for_app, app.label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(BarelyVisualTokens.controlShape)
            .secondaryClickable(onLongPress)
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
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
        if (notificationCount > 0) {
            Box(
                Modifier
                    .padding(start = 10.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.86f)),
            )
        }
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

private fun buildRankedLauncherResults(
    context: android.content.Context,
    query: String,
    apps: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    contacts: List<LauncherContact>,
    hasContactsPermission: Boolean,
    hasNotificationAccess: Boolean,
    notificationDotsEnabled: Boolean,
    mediaControlsEnabled: Boolean,
    preferredAssistant: AssistantPreference,
    launcherSearchLearning: List<LauncherSearchLearning> = emptyList(),
    limit: Int,
): List<LauncherSearchResult> {
    val normalizedQuery = query.normalizedForSearch()
    if (normalizedQuery.isBlank()) return emptyList()
    val installedPackages = apps.mapTo(mutableSetOf(), LauncherApp::packageName)
    val ranked = buildList {
        buildLauncherCommands(
            context = context,
            query = query,
            installedPackages = installedPackages,
            contacts = contacts,
            hasContactsPermission = hasContactsPermission,
            hasNotificationAccess = hasNotificationAccess,
            notificationDotsEnabled = notificationDotsEnabled,
            mediaControlsEnabled = mediaControlsEnabled,
            preferredAssistant = preferredAssistant,
        ).forEach { command -> add(CommandSearchResult(command)) }
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
            )?.let { score ->
                val learnedBoost = learnedSearchBoost(
                    normalizedQuery = normalizedQuery,
                    targetKey = app.searchTargetKey,
                    learning = launcherSearchLearning,
                )
                add(AppSearchResult(app, score - learnedBoost))
            }
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
            )?.let { score ->
                val learnedBoost = learnedSearchBoost(
                    normalizedQuery = normalizedQuery,
                    targetKey = shortcut.searchTargetKey,
                    learning = launcherSearchLearning,
                )
                add(ShortcutSearchResult(shortcut, score - learnedBoost))
            }
        }
    }.sortedWith(
        compareBy<LauncherSearchResult> { it.score }
            .thenBy { it.typePriority }
            .thenBy { it.publisherRank }
            .thenBy { it.label },
    )
    if (ranked.isNotEmpty()) return ranked.take(limit)
    if (normalizedQuery.length < 2 || query.trimStart().startsWith(':')) return emptyList()
    return buildAssistantCommands(
        context = context,
        prompt = query.trim(),
        installedPackages = installedPackages,
        preferredAssistant = preferredAssistant,
    ).map(::CommandSearchResult).take(limit)
}

@Composable
private fun SearchPage(
    initialQuery: String,
    apps: List<LauncherApp>,
    shortcuts: List<LauncherShortcut>,
    canSearchShortcuts: Boolean,
    contacts: List<LauncherContact>,
    hasContactsPermission: Boolean,
    hasNotificationAccess: Boolean,
    notificationDotsEnabled: Boolean,
    mediaControlsEnabled: Boolean,
    preferredAssistant: AssistantPreference,
    notificationCounts: Map<String, Int>,
    foldingFeature: FoldingFeature?,
    backdropBlurEnabled: Boolean,
    searchCornerRadius: Int,
    onClose: () -> Unit,
    onDismissToHome: () -> Unit,
    recommendedApps: List<LauncherApp>,
    recentAppSearches: List<String>,
    launcherSearchLearning: List<LauncherSearchLearning>,
    showSearchHint: Boolean,
    onDismissSearchHint: () -> Unit,
    onAppSearchCommitted: (String, LauncherApp) -> Unit,
    onShortcutSearchCommitted: (String, LauncherShortcut) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onExecuteCommand: (LauncherCommand) -> Unit,
) {
    var query by remember { mutableStateOf(initialQuery) }
    var selectedResultIndex by remember { mutableIntStateOf(0) }
    var contentVisible by remember { mutableStateOf(false) }
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    var dismissDragging by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val searchPaneTitle = stringResource(R.string.search)
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
    val rankedResults = remember(
        apps,
        shortcuts,
        contacts,
        hasContactsPermission,
        hasNotificationAccess,
        notificationDotsEnabled,
        mediaControlsEnabled,
        preferredAssistant,
        launcherSearchLearning,
        normalizedQuery,
    ) {
        buildRankedLauncherResults(
            context = context,
            query = query,
            apps = apps,
            shortcuts = shortcuts,
            contacts = contacts,
            hasContactsPermission = hasContactsPermission,
            hasNotificationAccess = hasNotificationAccess,
            notificationDotsEnabled = notificationDotsEnabled,
            mediaControlsEnabled = mediaControlsEnabled,
            preferredAssistant = preferredAssistant,
            launcherSearchLearning = launcherSearchLearning,
            limit = MAX_SEARCH_RESULTS,
        )
    }

    LaunchedEffect(normalizedQuery) {
        selectedResultIndex = 0
    }

    fun moveSelection(delta: Int) {
        if (rankedResults.isEmpty()) return
        selectedResultIndex = (selectedResultIndex + delta)
            .coerceIn(0, rankedResults.lastIndex)
    }

    fun launchBestResult() {
        keyboard?.hide()
        when (val result = rankedResults.getOrNull(selectedResultIndex)) {
            is CommandSearchResult -> onExecuteCommand(result.command)
            is AppSearchResult -> {
                onAppSearchCommitted(query, result.app)
                onLaunchApp(result.app)
            }
            is ShortcutSearchResult -> {
                onShortcutSearchCommitted(query, result.shortcut)
                onLaunchShortcut(result.shortcut)
            }
            null -> Unit
        }
    }

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(35)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = searchPaneTitle }
            .background(
                Brush.verticalGradient(
                    if (backdropBlurEnabled) {
                        listOf(
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimTopWithBlur),
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimMiddleWithBlur),
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimBottomWithBlur),
                        )
                    } else {
                        listOf(
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimTopFallback),
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimMiddleFallback),
                            Color.Black.copy(alpha = BarelyVisualTokens.searchScrimBottomFallback),
                        )
                    },
                ),
            ),
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
                    enter = fadeIn(tween(BarelyMotionTokens.standard)) +
                        slideInVertically(tween(220)) { -it / 18 },
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
                                                focusManager.clearFocus(force = true)
                                                keyboard?.hide()
                                                onDismissToHome()
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
                                    focusManager.clearFocus(force = true)
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
                            selectedResultIndex = selectedResultIndex,
                            canSearchShortcuts = canSearchShortcuts,
                            foldingFeature = foldingFeature,
                            recommendedApps = recommendedApps,
                            recentAppSearches = recentAppSearches,
                            showSearchHint = showSearchHint,
                            onDismissSearchHint = onDismissSearchHint,
                            onRecentSearch = { query = it },
                            onLaunchApp = { app ->
                                onAppSearchCommitted(query, app)
                                onLaunchApp(app)
                            },
                            onLongPress = onLongPress,
                            onLaunchShortcut = { shortcut ->
                                onShortcutSearchCommitted(query, shortcut)
                                onLaunchShortcut(shortcut)
                            },
                            onExecuteCommand = onExecuteCommand,
                            notificationCounts = notificationCounts,
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
                        onMoveSelection = ::moveSelection,
                        onClose = onClose,
                        onClear = { query = "" },
                        cornerRadius = searchCornerRadius,
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
    onMoveSelection: (Int) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    cornerRadius: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = BarelyVisualTokens.contentHorizontalPadding,
                    vertical = 8.dp,
                ),
            shape = RoundedCornerShape(cornerRadius.dp),
            color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceControl),
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .padding(start = 16.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = Color.White.copy(alpha = BarelyVisualTokens.contentPrimary),
                )
                Spacer(Modifier.width(13.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = modifier
                        .weight(1f)
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) {
                                false
                            } else {
                                when {
                                    event.key == Key.DirectionUp ||
                                        (event.key == Key.Tab && !event.isShiftPressed) -> {
                                        onMoveSelection(1)
                                        true
                                    }

                                    event.key == Key.DirectionDown ||
                                        (event.key == Key.Tab && event.isShiftPressed) -> {
                                        onMoveSelection(-1)
                                        true
                                    }

                                    event.key == Key.Enter -> {
                                        onSearch()
                                        true
                                    }

                                    event.key == Key.Escape -> {
                                        onClose()
                                        true
                                    }

                                    else -> false
                                }
                            }
                        },
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
                                    color = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
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
}

@Composable
private fun ZeroQueryContent(
    modifier: Modifier,
    recommendedApps: List<LauncherApp>,
    recentAppSearches: List<String>,
    showHint: Boolean,
    onDismissHint: () -> Unit,
    onRecentSearch: (String) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    notificationCounts: Map<String, Int>,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
        if (showHint) {
            Surface(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                shape = BarelyVisualTokens.floatingPanelShape,
                color = Color.Black.copy(alpha = BarelyVisualTokens.surfaceSubtle),
                contentColor = Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.command_palette_hint),
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = BarelyVisualTokens.contentSecondary),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    IconButton(onClick = onDismissHint) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.dismiss_search_hint),
                            modifier = Modifier.size(18.dp),
                            tint = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
        if (recommendedApps.isNotEmpty()) {
            Text(
                stringResource(R.string.recommended_apps),
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelLarge,
            )
            recommendedApps.take(4).forEach { app ->
                AppSearchRow(
                    app = app,
                    notificationCount = notificationCounts[app.packageName] ?: 0,
                    selected = false,
                    onLaunchApp = onLaunchApp,
                    onLongPress = onLongPress,
                )
            }
        }
        if (recentAppSearches.isNotEmpty()) {
            Text(
                stringResource(R.string.recent_searches),
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelLarge,
            )
            recentAppSearches.take(3).forEach { recent ->
                ListItem(
                    onClick = { onRecentSearch(recent) },
                    colors = minimalListItemColors(),
                    leadingContent = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    },
                ) {
                    Text(recent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        }
    }
}

@Composable
private fun SearchResults(
    modifier: Modifier,
    query: String,
    results: List<LauncherSearchResult>,
    selectedResultIndex: Int,
    canSearchShortcuts: Boolean,
    foldingFeature: FoldingFeature?,
    recommendedApps: List<LauncherApp>,
    recentAppSearches: List<String>,
    showSearchHint: Boolean,
    onDismissSearchHint: () -> Unit,
    onRecentSearch: (String) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    onExecuteCommand: (LauncherCommand) -> Unit,
    notificationCounts: Map<String, Int>,
) {
    if (query.isBlank()) {
        ZeroQueryContent(
            modifier = modifier,
            recommendedApps = recommendedApps,
            recentAppSearches = recentAppSearches,
            showHint = showSearchHint,
            onDismissHint = onDismissSearchHint,
            onRecentSearch = onRecentSearch,
            onLaunchApp = onLaunchApp,
            onLongPress = onLongPress,
            notificationCounts = notificationCounts,
        )
        return
    }
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
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(columnGap),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
        ) {
            gridItemsIndexed(results, key = { _, result -> result.key }) { index, result ->
                when (result) {
                    is CommandSearchResult -> CommandSearchRow(
                        command = result.command,
                        selected = index == selectedResultIndex,
                        onExecute = onExecuteCommand,
                    )

                    is AppSearchResult -> AppSearchRow(
                        app = result.app,
                        notificationCount = notificationCounts[result.app.packageName] ?: 0,
                        selected = index == selectedResultIndex,
                        onLaunchApp = onLaunchApp,
                        onLongPress = onLongPress,
                    )

                    is ShortcutSearchResult -> ShortcutSearchRow(
                        shortcut = result.shortcut,
                        notificationCount = notificationCounts[result.shortcut.owner.packageName] ?: 0,
                        selected = index == selectedResultIndex,
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
private fun CommandSearchRow(
    command: LauncherCommand,
    selected: Boolean,
    onExecute: (LauncherCommand) -> Unit,
) {
    val icon = when (command.icon) {
        CommandIcon.CALCULATE -> Icons.Outlined.Calculate
        CommandIcon.CONVERT -> Icons.Outlined.SwapHoriz
        CommandIcon.SETTINGS -> Icons.Outlined.Settings
        CommandIcon.CONTACT -> Icons.Outlined.PersonOutline
        CommandIcon.ASSISTANT -> Icons.Outlined.SmartToy
        CommandIcon.NOTIFICATIONS -> Icons.Outlined.NotificationsNone
        CommandIcon.MEDIA -> Icons.Outlined.PlayArrow
    }
    ListItem(
        onClick = { onExecute(command) },
        modifier = Modifier.clip(BarelyVisualTokens.controlShape),
        colors = minimalListItemColors(selected),
        supportingContent = {
            Text(command.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = { Icon(icon, contentDescription = null) },
    ) {
        Text(command.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AppSearchRow(
    app: LauncherApp,
    notificationCount: Int,
    selected: Boolean,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
) {
    ListItem(
        onClick = { onLaunchApp(app) },
        onLongClick = { onLongPress(app) },
        modifier = Modifier
            .clip(BarelyVisualTokens.controlShape)
            .secondaryClickable { onLongPress(app) },
        colors = minimalListItemColors(selected),
        leadingContent = { AppIcon(app, Modifier.size(42.dp)) },
        trailingContent = { NotificationDot(notificationCount) },
    ) { Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun ShortcutSearchRow(
    shortcut: LauncherShortcut,
    notificationCount: Int,
    selected: Boolean,
    onLongPress: (LauncherApp) -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
) {
    ListItem(
        onClick = { onLaunchShortcut(shortcut) },
        onLongClick = { onLongPress(shortcut.owner) },
        enabled = shortcut.info.isEnabled,
        modifier = Modifier
            .clip(BarelyVisualTokens.controlShape)
            .secondaryClickable { onLongPress(shortcut.owner) },
        colors = minimalListItemColors(selected),
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
        trailingContent = { NotificationDot(notificationCount) },
    ) { Text(shortcut.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun NotificationDot(count: Int) {
    if (count <= 0) return
    Box(
        Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.86f)),
    )
}

private fun Modifier.secondaryClickable(onClick: () -> Unit): Modifier = pointerInput(onClick) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                event.changes.forEach { it.consume() }
                onClick()
            }
        }
    }
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
            color = Color.White.copy(alpha = BarelyVisualTokens.contentSecondary),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun minimalListItemColors(selected: Boolean = false) = ListItemDefaults.colors(
    containerColor = if (selected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
    headlineColor = Color.White,
    supportingColor = Color.White.copy(alpha = BarelyVisualTokens.contentMuted),
    leadingIconColor = Color.White,
)

private data class WidgetAppGroup(
    val packageName: String,
    val label: String,
    val icon: android.graphics.Bitmap?,
    val providers: List<AppWidgetProviderInfo>,
)

@Composable
private fun WidgetPickerSheet(
    providers: List<AppWidgetProviderInfo>,
    widgetManager: AppWidgetManager,
    onDismiss: () -> Unit,
    onSelect: (AppWidgetProviderInfo) -> Unit,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var query by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    val groups = remember(providers, query) {
        providers.groupBy { it.provider.packageName }.map { (packageName, appProviders) ->
            val applicationInfo = runCatching {
                packageManager.getApplicationInfo(packageName, 0)
            }.getOrNull()
            val appLabel = applicationInfo?.let { packageManager.getApplicationLabel(it).toString() }
                ?: packageName
            val icon = applicationInfo?.let {
                runCatching { packageManager.getApplicationIcon(it).toBitmap(112, 112) }.getOrNull()
            }
            WidgetAppGroup(
                packageName = packageName,
                label = appLabel,
                icon = icon,
                providers = appProviders.sortedBy { it.loadLabel(packageManager) },
            )
        }.filter { group ->
            query.isBlank() || group.label.contains(query, ignoreCase = true) ||
                group.providers.any {
                    it.loadLabel(packageManager).contains(query, ignoreCase = true)
                }
        }.sortedBy { it.label.lowercase() }
    }
    val selectedGroup = groups.firstOrNull { it.packageName == selectedPackage }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = BarelyVisualTokens.surfaceSubtle),
        dragHandle = { BarelySheetDragHandle() },
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedPackage != null) {
                    IconButton(onClick = { selectedPackage = null }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.widget_apps),
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        selectedGroup?.label ?: stringResource(R.string.choose_widget),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        if (selectedGroup == null) {
                            stringResource(R.string.widget_picker_summary)
                        } else {
                            resourcesQuantityString(
                                R.plurals.widget_count,
                                selectedGroup.providers.size,
                                selectedGroup.providers.size,
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (selectedGroup == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = BarelyVisualTokens.contentHorizontalPadding,
                            vertical = 10.dp,
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isBlank()) {
                                    Text(
                                        stringResource(R.string.search_widgets),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                }
            }
            }

            if (selectedGroup == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    items(groups, key = WidgetAppGroup::packageName) { group ->
                        ListItem(
                            onClick = { selectedPackage = group.packageName },
                            leadingContent = {
                                WidgetBitmapIcon(group.icon, group.label, 50)
                            },
                            supportingContent = {
                                Text(
                                    resourcesQuantityString(
                                        R.plurals.widget_count,
                                        group.providers.size,
                                        group.providers.size,
                                    ),
                                )
                            },
                            trailingContent = {
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                            },
                        ) {
                            Text(group.label)
                        }
                    }
                    if (groups.isEmpty()) {
                        item {
                            SearchHint(
                                title = stringResource(R.string.no_widgets_found),
                                description = stringResource(R.string.try_another_name),
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(selectedGroup.providers, key = { it.provider.flattenToString() }) { provider ->
                        WidgetPreviewCard(
                            provider = provider,
                            widgetManager = widgetManager,
                            onClick = { onSelect(provider) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    provider: AppWidgetProviderInfo,
    widgetManager: AppWidgetManager,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val density = LocalDensity.current
    val resourceDensity = resources.displayMetrics.density
    val densityDpi = resources.displayMetrics.densityDpi
    val packageManager = context.packageManager
    val label = remember(provider.provider) { provider.loadLabel(packageManager) }
    val cells = remember(provider.provider, resourceDensity) {
        val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && provider.targetCellWidth > 0) {
            provider.targetCellWidth
        } else {
            ((provider.minWidth / resourceDensity) / 70f)
                .toInt().coerceAtLeast(1)
        }
        val height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && provider.targetCellHeight > 0) {
            provider.targetCellHeight
        } else {
            ((provider.minHeight / resourceDensity) / 70f)
                .toInt().coerceAtLeast(1)
        }
        width.coerceIn(1, 6) to height.coerceIn(1, 6)
    }
    val previewBitmap = remember(provider.provider, densityDpi) {
        runCatching {
            provider.loadPreviewImage(context, densityDpi)
                ?.toBitmap()
        }.getOrNull()
    }
    val remotePreview = remember(provider.provider) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
                provider.generatedPreviewCategories.and(
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                ) != 0 -> runCatching {
                widgetManager.getWidgetPreview(
                    provider.provider,
                    provider.profile,
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                )
            }.getOrNull()

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && provider.previewLayout != 0 ->
                RemoteViews(provider.provider.packageName, provider.previewLayout)

            else -> null
        }
    }
    val aspectRatio = (cells.first.toFloat() / cells.second.toFloat()).coerceIn(0.75f, 2.2f)
    val previewHeight = (220f / aspectRatio).coerceIn(130f, 220f).dp

    Card(onClick = onClick, shape = BarelyVisualTokens.dialogShape) {
        Column(Modifier.padding(14.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                when {
                    previewBitmap != null -> Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                    )

                    remotePreview != null -> AndroidView(
                        factory = { previewContext ->
                            FrameLayout(previewContext).apply {
                                clipChildren = true
                                clipToPadding = true
                                runCatching {
                                    val previewView = remotePreview.apply(previewContext, this)
                                    addView(
                                        previewView,
                                        FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                        ),
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                    )

                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val icon = remember(provider.provider, densityDpi) {
                            runCatching {
                                provider.loadIcon(context, densityDpi)
                                    .toBitmap(112, 112)
                            }.getOrNull()
                        }
                        WidgetBitmapIcon(icon, label, 64)
                    }
                }
            }
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.widget_dimensions, cells.first, cells.second),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun WidgetBitmapIcon(bitmap: android.graphics.Bitmap?, label: String, sizeDp: Int) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(sizeDp.dp),
        )
    } else {
        Surface(
            modifier = Modifier.size(sizeDp.dp),
            shape = RoundedCornerShape((sizeDp / 3).dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(label.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun resourcesQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
    LocalResources.current.getQuantityString(id, quantity, *formatArgs)

private enum class GesturePicker {
    DOUBLE_TAP,
    SWIPE_DOWN,
}

@Composable
private fun gestureActionLabel(action: LauncherGestureAction): String = when (action) {
    LauncherGestureAction.NONE -> stringResource(R.string.gesture_action_none)
    LauncherGestureAction.LOCK_SCREEN -> stringResource(R.string.gesture_action_lock)
    LauncherGestureAction.NOTIFICATIONS -> stringResource(R.string.gesture_action_notifications)
    LauncherGestureAction.SEARCH -> stringResource(R.string.gesture_action_search)
    LauncherGestureAction.APPS -> stringResource(R.string.gesture_action_apps)
}

@Composable
private fun LauncherSettingsPage(
    settings: LauncherSettings,
    availableAssistants: List<AssistantPreference>,
    isHomeRoleHeld: Boolean,
    hasGestureAccess: Boolean,
    hasNotificationAccess: Boolean,
    hasContactsPermission: Boolean,
    onDismiss: () -> Unit,
    onSettingsChanged: (LauncherSettings) -> Unit,
    onRequestHomeRole: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onConfigureContacts: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    onClearLocalHistory: () -> Unit,
) {
    var assistantPickerVisible by remember { mutableStateOf(false) }
    var gesturePicker by remember { mutableStateOf<GesturePicker?>(null) }
    val assistantOptions = remember(availableAssistants) {
        buildList {
            addAll(availableAssistants)
            if (availableAssistants.size > 1) add(AssistantPreference.ASK_EVERY_TIME)
        }
    }
    val selectedAssistant = settings.preferredAssistant.takeIf { it in assistantOptions }
        ?: assistantOptions.firstOrNull()
        ?: AssistantPreference.ASK_EVERY_TIME

    if (assistantPickerVisible) {
        AlertDialog(
            onDismissRequest = { assistantPickerVisible = false },
            title = { Text(stringResource(R.string.settings_ai_assistant)) },
            text = {
                Column {
                    assistantOptions.forEach { assistant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(BarelyVisualTokens.compactRowShape)
                                .combinedClickable {
                                    onSettingsChanged(
                                        settings.copy(preferredAssistant = assistant),
                                    )
                                    assistantPickerVisible = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = assistant == selectedAssistant,
                                onClick = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(assistantPreferenceLabel(assistant))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { assistantPickerVisible = false }) {
                    Text(stringResource(R.string.close_search))
                }
            },
        )
    }

    gesturePicker?.let { picker ->
        val options = if (picker == GesturePicker.DOUBLE_TAP) {
            buildList {
                add(LauncherGestureAction.NONE)
                add(LauncherGestureAction.LOCK_SCREEN)
                if (settings.homeMode == LauncherHomeMode.CLASSIC) {
                    add(LauncherGestureAction.SEARCH)
                }
                add(LauncherGestureAction.APPS)
            }
        } else {
            buildList {
                add(LauncherGestureAction.NONE)
                add(LauncherGestureAction.NOTIFICATIONS)
                if (settings.homeMode == LauncherHomeMode.CLASSIC) {
                    add(LauncherGestureAction.SEARCH)
                }
                add(LauncherGestureAction.APPS)
            }
        }
        val selected = if (picker == GesturePicker.DOUBLE_TAP) {
            settings.doubleTapAction
        } else {
            settings.swipeDownAction
        }
        AlertDialog(
            onDismissRequest = { gesturePicker = null },
            title = {
                Text(
                    stringResource(
                        if (picker == GesturePicker.DOUBLE_TAP) {
                            R.string.settings_double_tap
                        } else {
                            R.string.settings_swipe_down
                        },
                    ),
                )
            },
            text = {
                Column {
                    options.forEach { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(BarelyVisualTokens.compactRowShape)
                                .combinedClickable {
                                    val updated = if (picker == GesturePicker.DOUBLE_TAP) {
                                        settings.copy(doubleTapAction = action)
                                    } else {
                                        settings.copy(swipeDownAction = action)
                                    }
                                    onSettingsChanged(updated)
                                    gesturePicker = null
                                    if (
                                        !hasGestureAccess &&
                                        action in setOf(
                                            LauncherGestureAction.LOCK_SCREEN,
                                            LauncherGestureAction.NOTIFICATIONS,
                                        )
                                    ) {
                                        onOpenAccessibilitySettings()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = action == selected, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(gestureActionLabel(action))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { gesturePicker = null }) {
                    Text(stringResource(R.string.close_search))
                }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = BarelyVisualTokens.readableContentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 28.dp),
            ) {
            item(key = "settings_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 24.dp, top = 8.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            stringResource(R.string.launcher_settings),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            stringResource(R.string.barely_tagline),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item(key = "settings_gestures_header") {
                SettingsSectionTitle(stringResource(R.string.settings_gestures))
            }
            item(key = "double_tap_lock") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_double_tap),
                    summary = stringResource(R.string.settings_double_tap_action_summary),
                    status = gestureActionLabel(settings.doubleTapAction),
                    onClick = { gesturePicker = GesturePicker.DOUBLE_TAP },
                )
            }
            item(key = "swipe_notifications") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_swipe_down),
                    summary = stringResource(R.string.settings_swipe_down_action_summary),
                    status = gestureActionLabel(settings.swipeDownAction),
                    onClick = { gesturePicker = GesturePicker.SWIPE_DOWN },
                )
            }

            item(key = "settings_appearance_header") {
                SettingsSectionTitle(stringResource(R.string.settings_appearance))
            }
            item(key = "home_mode") {
                HomeModeSettingsPicker(
                    selectedMode = settings.homeMode,
                    onModeSelected = { homeMode ->
                        onSettingsChanged(settings.copy(homeMode = homeMode))
                    },
                )
            }
            if (settings.homeMode == LauncherHomeMode.TERMINAL) {
                item(key = "terminal_background") {
                    TerminalBackgroundSettings(
                        color = settings.terminalBackgroundColor,
                        opacity = settings.terminalBackgroundOpacity,
                        cornerRadius = settings.terminalCornerRadius,
                        onColorChanged = { selectedColor ->
                            onSettingsChanged(
                                settings.copy(terminalBackgroundColor = selectedColor),
                            )
                        },
                        onOpacityChanged = { selectedOpacity ->
                            onSettingsChanged(
                                settings.copy(terminalBackgroundOpacity = selectedOpacity),
                            )
                        },
                        onCornerRadiusChanged = { radius ->
                            onSettingsChanged(settings.copy(terminalCornerRadius = radius))
                        },
                        onReset = {
                            onSettingsChanged(
                                settings.copy(
                                    terminalBackgroundColor =
                                        BarelyDefaults.TERMINAL_BACKGROUND_COLOR,
                                    terminalBackgroundOpacity =
                                        BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
                                    terminalTopActionBackdrop =
                                        BarelyDefaults.TERMINAL_TOP_ACTION_BACKDROP,
                                    terminalCornerRadius =
                                        BarelyDefaults.TERMINAL_CORNER_RADIUS,
                                    terminalAesthetic =
                                        BarelyDefaults.TERMINAL_AESTHETIC,
                                ),
                            )
                        },
                    )
                }
                item(key = "terminal_top_action_backdrop") {
                    SettingsSwitchItem(
                        title = stringResource(R.string.settings_terminal_action_backdrop),
                        summary = stringResource(
                            R.string.settings_terminal_action_backdrop_summary,
                        ),
                        checked = settings.terminalTopActionBackdrop,
                        onCheckedChange = { enabled ->
                            onSettingsChanged(
                                settings.copy(terminalTopActionBackdrop = enabled),
                            )
                        },
                    )
                }
                item(key = "terminal_aesthetic") {
                    SettingsSwitchItem(
                        title = stringResource(R.string.settings_terminal_aesthetic),
                        summary = stringResource(
                            R.string.settings_terminal_aesthetic_summary,
                        ),
                        checked = settings.terminalAesthetic,
                        onCheckedChange = { enabled ->
                            onSettingsChanged(settings.copy(terminalAesthetic = enabled))
                        },
                    )
                }
            }
            item(key = "frosted_wallpaper") {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_frosted_wallpaper),
                    summary = stringResource(R.string.settings_frosted_wallpaper_summary),
                    checked = settings.frostedWallpaper,
                    onCheckedChange = { enabled ->
                        onSettingsChanged(settings.copy(frostedWallpaper = enabled))
                    },
                )
            }

            item(key = "settings_search_header") {
                SettingsSectionTitle(stringResource(R.string.settings_search))
            }
            item(key = "preferred_assistant") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_ai_assistant),
                    summary = stringResource(R.string.settings_ai_assistant_summary),
                    status = if (assistantOptions.isEmpty()) {
                        stringResource(R.string.assistant_not_installed)
                    } else {
                        assistantPreferenceLabel(selectedAssistant)
                    },
                    onClick = {
                        if (assistantOptions.isNotEmpty()) assistantPickerVisible = true
                    },
                )
            }
            item(key = "local_suggestions") {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_local_suggestions),
                    summary = stringResource(R.string.settings_local_suggestions_summary),
                    checked = settings.localSuggestions,
                    onCheckedChange = { enabled ->
                        onSettingsChanged(settings.copy(localSuggestions = enabled))
                    },
                )
            }
            item(key = "search_hint") {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_show_search_hint),
                    summary = stringResource(R.string.settings_show_search_hint_summary),
                    checked = settings.showSearchHint,
                    onCheckedChange = { enabled ->
                        onSettingsChanged(settings.copy(showSearchHint = enabled))
                    },
                )
            }
            item(key = "clear_local_history") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_clear_local_history),
                    summary = stringResource(R.string.settings_clear_local_history_summary),
                    status = "",
                    onClick = onClearLocalHistory,
                )
            }
            item(key = "settings_optional_header") {
                SettingsSectionTitle(stringResource(R.string.settings_optional_modules))
            }
            item(key = "notification_dots") {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_notification_dots),
                    summary = stringResource(R.string.command_notification_dots_subtitle),
                    checked = settings.notificationDots,
                    onCheckedChange = { enabled ->
                        onSettingsChanged(settings.copy(notificationDots = enabled))
                        if (enabled && !hasNotificationAccess) onOpenNotificationAccess()
                    },
                )
            }
            item(key = "media_controls") {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_media_controls),
                    summary = stringResource(R.string.command_media_controls_subtitle),
                    checked = settings.mediaControls,
                    onCheckedChange = { enabled ->
                        onSettingsChanged(settings.copy(mediaControls = enabled))
                        if (enabled && !hasNotificationAccess) onOpenNotificationAccess()
                    },
                )
            }

            item(key = "settings_portability_header") {
                SettingsSectionTitle(stringResource(R.string.settings_portability))
            }
            item(key = "export_settings") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_export),
                    summary = stringResource(R.string.settings_export_summary),
                    status = "",
                    onClick = onExportSettings,
                )
            }
            item(key = "import_settings") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_import),
                    summary = stringResource(R.string.settings_import_summary),
                    status = "",
                    onClick = onImportSettings,
                )
            }

            item(key = "settings_access_header") {
                SettingsSectionTitle(stringResource(R.string.settings_system_access))
            }
            item(key = "home_role") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_default_home),
                    summary = stringResource(R.string.home_role_required),
                    status = stringResource(
                        if (isHomeRoleHeld) R.string.settings_active else R.string.settings_set_up,
                    ),
                    onClick = onRequestHomeRole,
                )
            }
            item(key = "gesture_access") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_gesture_access),
                    summary = stringResource(R.string.settings_gesture_access_summary),
                    status = stringResource(
                        if (hasGestureAccess) R.string.settings_allowed else R.string.settings_set_up,
                    ),
                    onClick = onOpenAccessibilitySettings,
                )
            }
            item(key = "notification_access") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_notification_access),
                    summary = stringResource(R.string.command_notification_access_warning),
                    status = stringResource(
                        if (hasNotificationAccess) R.string.settings_allowed else R.string.settings_optional,
                    ),
                    onClick = onOpenNotificationAccess,
                )
            }
            item(key = "contacts_access") {
                SettingsActionItem(
                    title = stringResource(R.string.settings_contact_search),
                    summary = stringResource(R.string.command_contacts_private),
                    status = stringResource(
                        if (hasContactsPermission) R.string.settings_allowed else R.string.settings_optional,
                    ),
                    onClick = onConfigureContacts,
                )
            }
            item(key = "settings_privacy_note") {
                Text(
                    stringResource(R.string.settings_privacy_note),
                    modifier = Modifier.padding(
                        horizontal = BarelyVisualTokens.screenHorizontalPadding,
                        vertical = 18.dp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            }
        }
    }
}

@Composable
private fun HomeModeSettingsPicker(
    selectedMode: LauncherHomeMode,
    onModeSelected: (LauncherHomeMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            stringResource(R.string.settings_home_mode),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            stringResource(R.string.settings_home_mode_summary),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeModeSettingOption(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.StarOutline,
                title = stringResource(R.string.home_mode_classic),
                selected = selectedMode == LauncherHomeMode.CLASSIC,
                onClick = { onModeSelected(LauncherHomeMode.CLASSIC) },
            )
            HomeModeSettingOption(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Bolt,
                title = stringResource(R.string.home_mode_terminal),
                selected = selectedMode == LauncherHomeMode.TERMINAL,
                onClick = { onModeSelected(LauncherHomeMode.TERMINAL) },
            )
        }
    }
}

@Composable
private fun HomeModeSettingOption(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = BarelyVisualTokens.cardShape
    Surface(
        modifier = modifier
            .clip(shape)
            .combinedClickable(onClick = onClick)
            .semantics { this.selected = selected },
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.54f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun TerminalBackgroundSettings(
    color: Int,
    opacity: Float,
    cornerRadius: Int,
    onColorChanged: (Int) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onCornerRadiusChanged: (Int) -> Unit,
    onReset: () -> Unit,
) {
    var previewOpacity by remember(opacity) { mutableFloatStateOf(opacity) }
    var previewCornerRadius by remember(cornerRadius) {
        mutableFloatStateOf(cornerRadius.toFloat())
    }
    val palette = remember {
        listOf(
            0xFF000000.toInt(),
            0xFF17191F.toInt(),
            0xFF17233A.toInt(),
            0xFF18332D.toInt(),
            0xFF3A2028.toInt(),
            0xFFF3EFE7.toInt(),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = BarelyVisualTokens.screenHorizontalPadding,
                vertical = 10.dp,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.settings_terminal_background),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.settings_terminal_reset))
            }
        }
        Text(
            stringResource(R.string.settings_terminal_background_summary),
            modifier = Modifier.padding(top = 3.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            palette.forEachIndexed { index, swatch ->
                val selected = swatch == color
                val colorDescription = stringResource(
                    R.string.settings_terminal_color_option,
                    index + 1,
                    palette.size,
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .semantics(mergeDescendants = true) {
                            contentDescription = colorDescription
                            this.selected = selected
                        }
                        .combinedClickable(
                            onClickLabel = colorDescription,
                            onClick = { onColorChanged(swatch) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = Color(swatch),
                        border = BorderStroke(
                            if (selected) 3.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {}
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.settings_terminal_opacity),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "${(previewOpacity.coerceIn(0f, 1f) * 100).roundToInt()}%",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(
                onClick = {
                    previewOpacity = BarelyDefaults.TERMINAL_BACKGROUND_OPACITY
                    onOpacityChanged(BarelyDefaults.TERMINAL_BACKGROUND_OPACITY)
                },
                enabled = previewOpacity != BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
            ) {
                Text(stringResource(R.string.settings_terminal_reset))
            }
        }
        Slider(
            value = previewOpacity.coerceIn(0f, 1f),
            onValueChange = { previewOpacity = it },
            onValueChangeFinished = { onOpacityChanged(previewOpacity) },
            valueRange = 0f..1f,
            steps = 9,
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.settings_terminal_wallpaper),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_terminal_solid),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.settings_terminal_corner_radius),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "${previewCornerRadius.roundToInt()} dp",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(
                onClick = {
                    previewCornerRadius = BarelyDefaults.TERMINAL_CORNER_RADIUS.toFloat()
                    onCornerRadiusChanged(BarelyDefaults.TERMINAL_CORNER_RADIUS)
                },
                enabled = previewCornerRadius.roundToInt() !=
                    BarelyDefaults.TERMINAL_CORNER_RADIUS,
            ) {
                Text(stringResource(R.string.settings_terminal_reset))
            }
        }
        Slider(
            value = previewCornerRadius.coerceIn(0f, 32f),
            onValueChange = { previewCornerRadius = it },
            onValueChangeFinished = {
                onCornerRadiusChanged(previewCornerRadius.roundToInt())
            },
            valueRange = 0f..32f,
            steps = 7,
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp)
            .semantics { heading() },
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        onClick = { onCheckedChange(!checked) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    ) {
        Text(title)
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    summary: String,
    status: String,
    onClick: () -> Unit,
) {
    ListItem(
        onClick = onClick,
        supportingContent = { Text(summary) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    status,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Text(title)
    }
}

@Composable
private fun AppActionsSheet(
    app: LauncherApp,
    shortcuts: List<LauncherShortcut>,
    isFavorite: Boolean,
    canReadShortcuts: Boolean,
    onDismiss: () -> Unit,
    onLaunchShortcut: (LauncherShortcut) -> Unit,
    favoriteShortcutKeys: Set<String>,
    onToggleShortcutFavorite: (LauncherShortcut) -> Unit,
    onToggleFavorite: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        SheetValue.Hidden,
        setOf(SheetValue.PartiallyExpanded),
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics { paneTitle = app.label },
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = BarelyVisualTokens.surfaceSubtle),
        dragHandle = { BarelySheetDragHandle() },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item(key = "app_header") {
                Row(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        top = 2.dp,
                        end = 20.dp,
                        bottom = 16.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(app, Modifier.size(54.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            app.label,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(R.string.app_actions),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item(key = "primary_actions") {
                Surface(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Column(Modifier.padding(4.dp)) {
                        ActionItem(
                            icon = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                            label = if (isFavorite) {
                                stringResource(R.string.remove_from_favorites)
                            } else {
                                stringResource(R.string.add_to_favorites)
                            },
                            onClick = onToggleFavorite,
                        )
                        SheetRowDivider()
                        ActionItem(
                            Icons.Outlined.Info,
                            stringResource(R.string.app_info),
                            onAppInfo,
                        )
                        SheetRowDivider()
                        ActionItem(
                            icon = Icons.Outlined.DeleteOutline,
                            label = stringResource(R.string.uninstall),
                            onClick = onUninstall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            item(key = "shortcut_divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = BarelyVisualTokens.contentHorizontalPadding,
                        vertical = 16.dp,
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f),
                )
            }
            when {
                shortcuts.isNotEmpty() -> {
                    item(key = "shortcut_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.shortcuts),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                shortcuts.size.toString(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    items(
                        items = shortcuts,
                        key = { shortcut ->
                            "shortcut_${shortcut.owner.key}_${shortcut.info.id}"
                        },
                    ) { shortcut ->
                        AppShortcutItem(
                            shortcut = shortcut,
                            onClick = { onLaunchShortcut(shortcut) },
                            isFavorite = shortcut.searchTargetKey in favoriteShortcutKeys,
                            onToggleFavorite = {
                                onToggleShortcutFavorite(shortcut)
                            },
                        )
                    }
                }

                !canReadShortcuts -> item(key = "shortcut_permission") {
                    Text(
                        stringResource(R.string.set_default_to_view_shortcuts),
                        modifier = Modifier.padding(
                            horizontal = BarelyVisualTokens.screenHorizontalPadding,
                            vertical = 14.dp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> item(key = "no_shortcuts") {
                    Text(
                        stringResource(R.string.app_has_no_shortcuts),
                        modifier = Modifier.padding(
                            horizontal = BarelyVisualTokens.screenHorizontalPadding,
                            vertical = 14.dp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BarelyVisualTokens.compactRowShape)
            .combinedClickable(onClick = onClick)
            .heightIn(min = 54.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            tint = color,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SheetRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 49.dp, end = 10.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f),
    )
}

@Composable
private fun AppShortcutItem(
    shortcut: LauncherShortcut,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    val enabled = shortcut.info.isEnabled
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .clip(BarelyVisualTokens.compactRowShape)
            .combinedClickable(enabled = enabled, onClick = onClick)
            .graphicsLayer(alpha = if (enabled) 1f else 0.42f)
            .heightIn(min = 54.dp)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Bolt,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                shortcut.label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            shortcut.description
                ?.takeIf { it != shortcut.label }
                ?.let { description ->
                    Text(
                        description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                contentDescription = stringResource(
                    if (isFavorite) {
                        R.string.remove_from_favorites
                    } else {
                        R.string.add_to_favorites
                    },
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarelySheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 8.dp)
            .width(36.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)),
    )
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
private const val WIDGET_HEIGHT_STEP_DP = 56
private const val MIN_WIDGET_HEIGHT_DP = 112
private const val MAX_WIDGET_HEIGHT_DP = 480
private const val MAX_TERMINAL_RESULTS = 3
private const val MAX_TERMINAL_QUERY_LENGTH = 96

private enum class TerminalBuiltInAction {
    OPEN_APPS,
    OPEN_SETTINGS,
    SWITCH_TO_CLASSIC,
    SHOW_HISTORY,
    CLEAR_HISTORY,
}

private data class TerminalHistoryEntry(
    val query: String,
    val result: LauncherSearchResult,
)

private sealed interface TerminalSuggestion {
    data class SearchResult(val result: LauncherSearchResult) : TerminalSuggestion

    data class BuiltIn(
        val command: String,
        val title: String,
        val subtitle: String,
        val action: TerminalBuiltInAction,
    ) : TerminalSuggestion
}

private fun terminalBuiltInSuggestions(
    context: android.content.Context,
    query: String,
): List<TerminalSuggestion> {
    val normalizedQuery = query.trim().lowercase()
    return listOf(
        TerminalSuggestion.BuiltIn(
            command = ":apps",
            title = context.getString(R.string.terminal_command_apps),
            subtitle = context.getString(R.string.terminal_command_apps_summary),
            action = TerminalBuiltInAction.OPEN_APPS,
        ),
        TerminalSuggestion.BuiltIn(
            command = ":settings",
            title = context.getString(R.string.terminal_command_settings),
            subtitle = context.getString(R.string.terminal_command_settings_summary),
            action = TerminalBuiltInAction.OPEN_SETTINGS,
        ),
        TerminalSuggestion.BuiltIn(
            command = ":classic",
            title = context.getString(R.string.terminal_command_classic),
            subtitle = context.getString(R.string.terminal_command_classic_summary),
            action = TerminalBuiltInAction.SWITCH_TO_CLASSIC,
        ),
        TerminalSuggestion.BuiltIn(
            command = ":history",
            title = context.getString(R.string.recent_searches),
            subtitle = context.getString(R.string.settings_local_suggestions_summary),
            action = TerminalBuiltInAction.SHOW_HISTORY,
        ),
        TerminalSuggestion.BuiltIn(
            command = ":clearhistory",
            title = context.getString(R.string.settings_clear_local_history),
            subtitle = context.getString(R.string.settings_clear_local_history_summary),
            action = TerminalBuiltInAction.CLEAR_HISTORY,
        ),
    ).filter { suggestion ->
        suggestion.command.startsWith(normalizedQuery) ||
            suggestion.title.normalizedForSearch().startsWith(
                normalizedQuery.removePrefix(":"),
            )
    }
}

private sealed interface LauncherSearchResult {
    val score: Int
    val key: String
    val label: String
    val typePriority: Int
    val publisherRank: Int
}

internal fun terminalHistoryProgressAfterDrag(
    currentProgress: Float,
    delta: Float,
    travelDistance: Float,
): Float = (
    currentProgress - delta / travelDistance.coerceAtLeast(1f)
).coerceIn(0f, 1f)

internal fun shouldOpenTerminalHistory(
    progress: Float,
    velocity: Float,
    flingThreshold: Float,
): Boolean = when {
    velocity <= -flingThreshold -> true
    velocity >= flingThreshold -> false
    else -> progress >= 0.42f
}

private data class AppSearchResult(
    val app: LauncherApp,
    override val score: Int,
) : LauncherSearchResult {
    override val key: String = app.searchTargetKey
    override val label: String = app.label
    override val typePriority: Int = 1
    override val publisherRank: Int = 0
}

private data class CommandSearchResult(
    val command: LauncherCommand,
) : LauncherSearchResult {
    override val score: Int = command.score
    override val key: String = command.key
    override val label: String = command.title
    override val typePriority: Int = 0
    override val publisherRank: Int = 0
}

private data class ShortcutSearchResult(
    val shortcut: LauncherShortcut,
    override val score: Int,
) : LauncherSearchResult {
    override val key: String = shortcut.searchTargetKey
    override val label: String = shortcut.label
    override val typePriority: Int = 0
    override val publisherRank: Int = shortcut.info.rank
}
