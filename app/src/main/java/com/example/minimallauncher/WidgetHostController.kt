package com.example.minimallauncher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context

class WidgetHostController(context: Context) {
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val host = AppWidgetHost(context, HOST_ID)

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun savedWidgetIds(): List<Int> {
        val saved = preferences.getString(WIDGET_IDS, null)
            ?.split(',')
            ?.mapNotNull(String::toIntOrNull)
            .orEmpty()
        val available = saved.filter { manager.getAppWidgetInfo(it) != null }
        if (available != saved) save(available)
        return available
    }

    fun allocateWidgetId(): Int = host.allocateAppWidgetId()

    fun addWidget(widgetId: Int): List<Int> {
        val updated = (savedWidgetIds() + widgetId).distinct()
        save(updated)
        return updated
    }

    fun removeWidget(widgetId: Int): List<Int> {
        runCatching { host.deleteAppWidgetId(widgetId) }
        val updated = savedWidgetIds().filterNot { it == widgetId }
        save(updated)
        return updated
    }

    fun discardWidgetId(widgetId: Int) {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            runCatching { host.deleteAppWidgetId(widgetId) }
        }
    }

    fun startListening() = host.startListening()

    fun stopListening() = host.stopListening()

    private fun save(widgetIds: List<Int>) {
        preferences.edit().putString(WIDGET_IDS, widgetIds.joinToString(",")).apply()
    }

    private companion object {
        const val HOST_ID = 0x4C59
        const val PREFERENCES = "launchly_widgets"
        const val WIDGET_IDS = "widget_ids"
    }
}
