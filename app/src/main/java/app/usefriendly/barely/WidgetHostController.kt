package app.usefriendly.barely

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Process
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.content.edit

data class WidgetPlacement(
    val widgetId: Int,
    val widthSpan: Int = MAX_WIDGET_SPAN,
    val heightDp: Int = AUTO_WIDGET_HEIGHT,
    val horizontalPosition: WidgetHorizontalPosition = WidgetHorizontalPosition.CENTER,
) {
    companion object {
        const val MIN_WIDGET_SPAN = 2
        const val MAX_WIDGET_SPAN = 4
        const val AUTO_WIDGET_HEIGHT = 0
    }
}

enum class WidgetHorizontalPosition {
    START,
    CENTER,
    END,
    ;

    fun next(): WidgetHorizontalPosition = entries[(ordinal + 1) % entries.size]
}

internal fun packWidgetRows(
    widgets: List<WidgetPlacement>,
): List<List<WidgetPlacement>> {
    if (widgets.isEmpty()) return emptyList()
    val rows = mutableListOf<MutableList<WidgetPlacement>>()
    var currentRow = mutableListOf<WidgetPlacement>()
    var occupiedSpans = 0
    widgets.forEach { widget ->
        val span = widget.widthSpan.coerceIn(
            WidgetPlacement.MIN_WIDGET_SPAN,
            WidgetPlacement.MAX_WIDGET_SPAN,
        )
        if (currentRow.isNotEmpty() && occupiedSpans + span > WidgetPlacement.MAX_WIDGET_SPAN) {
            rows += currentRow
            currentRow = mutableListOf()
            occupiedSpans = 0
        }
        currentRow += widget
        occupiedSpans += span
        if (occupiedSpans == WidgetPlacement.MAX_WIDGET_SPAN) {
            rows += currentRow
            currentRow = mutableListOf()
            occupiedSpans = 0
        }
    }
    if (currentRow.isNotEmpty()) rows += currentRow
    return rows
}

internal data class WidgetProviderSize(
    val widthDp: Int,
    val heightDp: Int,
)

/**
 * Keep the provider's RemoteViews at its last committed size while the user drags the frame.
 * Many third-party widgets reinflate their whole hierarchy whenever options change, which is the
 * source of the visible flashing that a per-pixel update causes.
 */
internal fun resolveWidgetProviderSize(
    committed: WidgetProviderSize,
    preview: WidgetProviderSize,
    resizeActive: Boolean,
): WidgetProviderSize = if (resizeActive) committed else preview

class WidgetHostController(context: Context) {
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val host: AppWidgetHost = BarelyAppWidgetHost(context, HOST_ID)

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun savedWidgets(): List<WidgetPlacement> {
        val saved = preferences.getString(WIDGET_IDS, null)
            ?.split(',')
            ?.mapNotNull(String::toIntOrNull)
            .orEmpty()
        val available = saved.filter { manager.getAppWidgetInfo(it) != null }
        val layouts = decodeLayouts()
        val widgets = available.map { widgetId ->
            layouts[widgetId] ?: WidgetPlacement(widgetId)
        }
        if (available != saved || widgets.any { layouts[it.widgetId] != it }) save(widgets)
        return widgets
    }

    fun allocateWidgetId(): Int = host.allocateAppWidgetId()

    fun availableProviders(): List<AppWidgetProviderInfo> = manager
        .getInstalledProvidersForProfile(Process.myUserHandle())
        .filter { provider ->
            val supportsHome = provider.widgetCategory == 0 ||
                provider.widgetCategory.and(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN) != 0
            val hidden = provider.widgetFeatures
                .and(AppWidgetProviderInfo.WIDGET_FEATURE_HIDE_FROM_PICKER) != 0
            supportsHome && !hidden
        }

    fun bindWidget(widgetId: Int, provider: AppWidgetProviderInfo): Boolean =
        manager.bindAppWidgetIdIfAllowed(
            widgetId,
            provider.profile,
            provider.provider,
            null,
        )

    fun addWidget(widgetId: Int): List<WidgetPlacement> {
        val current = savedWidgets()
        val updated = if (current.any { it.widgetId == widgetId }) {
            current
        } else {
            current + WidgetPlacement(widgetId)
        }
        save(updated)
        return updated
    }

    fun removeWidget(widgetId: Int): List<WidgetPlacement> {
        runCatching { host.deleteAppWidgetId(widgetId) }
        val updated = savedWidgets().filterNot { it.widgetId == widgetId }
        save(updated)
        return updated
    }

    fun updateWidget(
        widgetId: Int,
        widthSpan: Int,
        heightDp: Int,
        horizontalPosition: WidgetHorizontalPosition,
    ): List<WidgetPlacement> {
        val updated = savedWidgets().map { widget ->
            if (widget.widgetId == widgetId) {
                widget.copy(
                    widthSpan = widthSpan.coerceIn(
                        WidgetPlacement.MIN_WIDGET_SPAN,
                        WidgetPlacement.MAX_WIDGET_SPAN,
                    ),
                    heightDp = heightDp.coerceAtLeast(WidgetPlacement.AUTO_WIDGET_HEIGHT),
                    horizontalPosition = horizontalPosition,
                )
            } else {
                widget
            }
        }
        save(updated)
        return updated
    }

    fun moveWidget(widgetId: Int, direction: Int): List<WidgetPlacement> {
        val current = savedWidgets().toMutableList()
        val fromIndex = current.indexOfFirst { it.widgetId == widgetId }
        if (fromIndex == -1) return current
        val toIndex = (fromIndex + direction).coerceIn(current.indices)
        if (fromIndex != toIndex) {
            val widget = current.removeAt(fromIndex)
            current.add(toIndex, widget)
            save(current)
        }
        return current
    }

    fun discardWidgetId(widgetId: Int) {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            runCatching { host.deleteAppWidgetId(widgetId) }
        }
    }

    fun startListening() = host.startListening()

    fun stopListening() = host.stopListening()

    private fun decodeLayouts(): Map<Int, WidgetPlacement> = preferences
        .getString(WIDGET_LAYOUTS, null)
        ?.split(';')
        ?.mapNotNull { encoded ->
            val values = encoded.split(':')
            val widgetId = values.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val widthSpan = values.getOrNull(1)?.toIntOrNull()
                ?.coerceIn(
                    WidgetPlacement.MIN_WIDGET_SPAN,
                    WidgetPlacement.MAX_WIDGET_SPAN,
                )
                ?: WidgetPlacement.MAX_WIDGET_SPAN
            val heightDp = values.getOrNull(2)?.toIntOrNull()
                ?.coerceAtLeast(WidgetPlacement.AUTO_WIDGET_HEIGHT)
                ?: WidgetPlacement.AUTO_WIDGET_HEIGHT
            val horizontalPosition = values.getOrNull(3)
                ?.let { value -> runCatching { WidgetHorizontalPosition.valueOf(value) }.getOrNull() }
                ?: WidgetHorizontalPosition.CENTER
            WidgetPlacement(widgetId, widthSpan, heightDp, horizontalPosition)
        }
        ?.associateBy(WidgetPlacement::widgetId)
        .orEmpty()

    private fun save(widgets: List<WidgetPlacement>) {
        preferences.edit {
            putString(WIDGET_IDS, widgets.joinToString(",") { it.widgetId.toString() })
            putString(
                WIDGET_LAYOUTS,
                widgets.joinToString(";") { widget ->
                    listOf(
                        widget.widgetId,
                        widget.widthSpan,
                        widget.heightDp,
                        widget.horizontalPosition.name,
                    ).joinToString(":")
                },
            )
        }
    }

    private companion object {
        const val HOST_ID = 0x4C59
        const val PREFERENCES = "barely_widgets"
        const val WIDGET_IDS = "widget_ids"
        const val WIDGET_LAYOUTS = "widget_layouts"
    }
}

/**
 * App widgets are regular Android views embedded inside Compose. Scrollable RemoteViews must get
 * first refusal on a gesture; otherwise the surrounding LazyColumn or horizontal Home pager can
 * steal the stream as soon as touch slop is crossed.
 *
 * We protect a gesture only while a descendant can actually scroll in its direction. At an edge
 * the request is released so the Favorites page can continue scrolling naturally.
 */
private class BarelyAppWidgetHost(
    context: Context,
    hostId: Int,
) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo,
    ): AppWidgetHostView = GestureAwareAppWidgetHostView(context)
}

private class GestureAwareAppWidgetHostView(context: Context) : AppWidgetHostView(context) {
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val longPressDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onLongPress(event: MotionEvent) {
                suppressTouchUntilUp = performLongClick()
            }
        },
    )
    private var downX = 0f
    private var downY = 0f
    private var protectingGesture = false
    private var suppressTouchUntilUp = false
    private var cancelDelivered = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            suppressTouchUntilUp = false
            cancelDelivered = false
        }
        longPressDetector.onTouchEvent(event)
        if (suppressTouchUntilUp) {
            if (!cancelDelivered) {
                val cancelEvent = MotionEvent.obtain(event).apply {
                    action = MotionEvent.ACTION_CANCEL
                }
                super.dispatchTouchEvent(cancelEvent)
                cancelEvent.recycle()
                cancelDelivered = true
            }
            if (
                event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                suppressTouchUntilUp = false
                cancelDelivered = false
                protectingGesture = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
            return true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                protectingGesture = hasScrollableDescendant(this)
                parent?.requestDisallowInterceptTouchEvent(protectingGesture)
            }

            MotionEvent.ACTION_MOVE -> {
                if (protectingGesture) {
                    val deltaX = event.x - downX
                    val deltaY = event.y - downY
                    if (kotlin.math.max(kotlin.math.abs(deltaX), kotlin.math.abs(deltaY)) > touchSlop) {
                        val vertical = kotlin.math.abs(deltaY) >= kotlin.math.abs(deltaX)
                        val direction = if ((if (vertical) deltaY else deltaX) < 0f) 1 else -1
                        protectingGesture = if (vertical) {
                            canDescendantScrollVertically(this, direction)
                        } else {
                            canDescendantScrollHorizontally(this, direction)
                        }
                        parent?.requestDisallowInterceptTouchEvent(protectingGesture)
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                protectingGesture = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}

internal fun AppWidgetHostView.setBarelyWidgetLongClickListener(listener: (() -> Unit)?) {
    if (this !is GestureAwareAppWidgetHostView) return
    setOnLongClickListener(
        listener?.let { onLongClick ->
            View.OnLongClickListener {
                onLongClick()
                true
            }
        },
    )
    isLongClickable = listener != null
}

private fun hasScrollableDescendant(view: View): Boolean =
    view.canScrollVertically(-1) ||
        view.canScrollVertically(1) ||
        view.canScrollHorizontally(-1) ||
        view.canScrollHorizontally(1) ||
        (view as? ViewGroup)?.childrenAny(::hasScrollableDescendant) == true

private fun canDescendantScrollVertically(view: View, direction: Int): Boolean =
    view.canScrollVertically(direction) ||
        (view as? ViewGroup)?.childrenAny { child ->
            canDescendantScrollVertically(child, direction)
        } == true

private fun canDescendantScrollHorizontally(view: View, direction: Int): Boolean =
    view.canScrollHorizontally(direction) ||
        (view as? ViewGroup)?.childrenAny { child ->
            canDescendantScrollHorizontally(child, direction)
        } == true

private fun ViewGroup.childrenAny(predicate: (View) -> Boolean): Boolean {
    for (index in 0 until childCount) {
        if (predicate(getChildAt(index))) return true
    }
    return false
}
