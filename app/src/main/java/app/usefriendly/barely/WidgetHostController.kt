package app.usefriendly.barely

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Process

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

class WidgetHostController(context: Context) {
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val host = AppWidgetHost(context, HOST_ID)

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
            val hidden = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                provider.widgetFeatures.and(AppWidgetProviderInfo.WIDGET_FEATURE_HIDE_FROM_PICKER) != 0
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
        preferences.edit()
            .putString(WIDGET_IDS, widgets.joinToString(",") { it.widgetId.toString() })
            .putString(
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
            .apply()
    }

    private companion object {
        const val HOST_ID = 0x4C59
        const val PREFERENCES = "barely_widgets"
        const val WIDGET_IDS = "widget_ids"
        const val WIDGET_LAYOUTS = "widget_layouts"
    }
}
