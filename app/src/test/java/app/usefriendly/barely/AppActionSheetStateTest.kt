@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package app.usefriendly.barely

import androidx.compose.material3.SheetValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppActionSheetStateTest {
    @Test
    fun `app actions sheet enables its initial and required expanded states`() {
        val enabledValues = appActionSheetEnabledValues()

        assertEquals(
            setOf(SheetValue.Hidden, SheetValue.Expanded),
            enabledValues,
        )
        assertFalse(SheetValue.PartiallyExpanded in enabledValues)
    }
}
