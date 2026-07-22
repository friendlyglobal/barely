package app.usefriendly.barely

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeReturnPolicyTest {
    @Test
    fun `returning home does not restart the pager when already centered`() {
        assertFalse(needsHomePagerReset(currentPage = 1))
    }

    @Test
    fun `returning home snaps side pages back to the center`() {
        assertTrue(needsHomePagerReset(currentPage = 0))
        assertTrue(needsHomePagerReset(currentPage = 2))
    }
}
