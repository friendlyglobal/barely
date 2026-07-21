package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandPaletteTest {
    @Test
    fun evaluatesArithmeticWithoutExecutingCode() {
        assertEquals("14", calculate("2 + 3 * 4")?.result)
        assertEquals("20", calculate("(2 + 3) * 4")?.result)
        assertEquals("512", calculate("2 ^ 3 ^ 2")?.result)
        assertNull(calculate("Runtime.exec()"))
    }

    @Test
    fun convertsCommonUnitsLocally() {
        assertEquals("6.2137119224 mi", convert("10 km to mi")?.result)
        assertEquals("212 °F", convert("100 c em f")?.result)
        assertEquals("1024 MB", convert("1.024 gb para mb")?.result)
    }

    @Test
    fun rejectsConversionsAcrossDimensions() {
        assertNull(convert("10 kg to km"))
        assertNull(convert("10 km to km"))
    }

    @Test
    fun choosesOnlyTheInstalledPreferredAssistant() {
        val installed = setOf("com.openai.chatgpt", "com.anthropic.claude")

        assertEquals(
            listOf("com.anthropic.claude"),
            selectAssistantPackages(installed, AssistantPreference.CLAUDE),
        )
        assertEquals(
            listOf("com.openai.chatgpt", "com.anthropic.claude"),
            selectAssistantPackages(installed, AssistantPreference.ASK_EVERY_TIME),
        )
    }

    @Test
    fun fallsBackSafelyWhenTheSavedAssistantWasRemoved() {
        val installed = setOf("com.google.android.apps.bard")

        assertEquals(
            listOf("com.google.android.apps.bard"),
            selectAssistantPackages(installed, AssistantPreference.CHATGPT),
        )
        assertTrue(
            selectAssistantPackages(
                installed,
                AssistantPreference.ASK_EVERY_TIME,
                targetPackage = "com.anthropic.claude",
            ).isEmpty(),
        )
    }
}
