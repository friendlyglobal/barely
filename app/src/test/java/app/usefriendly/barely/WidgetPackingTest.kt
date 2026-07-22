package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetPackingTest {
    @Test
    fun `two half width widgets share one row`() {
        val widgets = listOf(
            WidgetPlacement(widgetId = 1, widthSpan = 2),
            WidgetPlacement(widgetId = 2, widthSpan = 2),
        )

        assertEquals(listOf(listOf(1, 2)), packWidgetRows(widgets).ids())
    }

    @Test
    fun `full width widget stays on its own row`() {
        val widgets = listOf(
            WidgetPlacement(widgetId = 1, widthSpan = 4),
            WidgetPlacement(widgetId = 2, widthSpan = 2),
            WidgetPlacement(widgetId = 3, widthSpan = 2),
        )

        assertEquals(listOf(listOf(1), listOf(2, 3)), packWidgetRows(widgets).ids())
    }

    @Test
    fun `packing preserves sequence when spans do not fit`() {
        val widgets = listOf(
            WidgetPlacement(widgetId = 1, widthSpan = 3),
            WidgetPlacement(widgetId = 2, widthSpan = 2),
            WidgetPlacement(widgetId = 3, widthSpan = 2),
        )

        assertEquals(listOf(listOf(1), listOf(2, 3)), packWidgetRows(widgets).ids())
    }

    @Test
    fun `provider size remains committed during live resize`() {
        val committed = WidgetProviderSize(widthDp = 360, heightDp = 180)
        val preview = WidgetProviderSize(widthDp = 224, heightDp = 296)

        assertEquals(
            committed,
            resolveWidgetProviderSize(committed, preview, resizeActive = true),
        )
    }

    @Test
    fun `provider receives final preview after resize`() {
        val committed = WidgetProviderSize(widthDp = 360, heightDp = 180)
        val preview = WidgetProviderSize(widthDp = 224, heightDp = 296)

        assertEquals(
            preview,
            resolveWidgetProviderSize(committed, preview, resizeActive = false),
        )
    }

    private fun List<List<WidgetPlacement>>.ids(): List<List<Int>> =
        map { row -> row.map(WidgetPlacement::widgetId) }
}
