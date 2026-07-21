package com.example.minimallauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
