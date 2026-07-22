package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherSettingsTest {
    @Test
    fun terminalAppearanceUsesTheDocumentedRestrainedDefaults() {
        val settings = LauncherSettings()

        assertEquals(LauncherHomeMode.TERMINAL, settings.homeMode)
        assertEquals(BarelyDefaults.TERMINAL_BACKGROUND_COLOR, settings.terminalBackgroundColor)
        assertEquals(
            BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
            settings.terminalBackgroundOpacity,
            0f,
        )
        assertFalse(settings.terminalTopActionBackdrop)
        assertFalse(settings.terminalAesthetic)
        assertEquals(12, settings.terminalCornerRadius)
        assertEquals(AssistantPreference.CHATGPT, settings.preferredAssistant)
        assertEquals(LauncherGestureAction.LOCK_SCREEN, settings.doubleTapAction)
        assertEquals(LauncherGestureAction.NOTIFICATIONS, settings.swipeDownAction)
        assertEquals(AppDrawerLayout.LIST, settings.appDrawerLayout)
        assertFalse(settings.showAppIcons)
        assertEquals(AppIconShape.ORIGINAL, settings.appIconShape)
        assertTrue(settings.showAppGridLabels)
        assertEquals(4, settings.appGridColumns)
        assertEquals(6, settings.appGridRows)
        assertEquals(FavoriteSortMode.MANUAL, settings.favoriteSortMode)
        assertEquals(0.5f, settings.frostedFallbackContrast, 0f)
    }

    @Test
    fun migratesLegacyGestureBooleansWithoutChangingUserIntent() {
        val migrated = decodeLauncherSettings(
            StoredLauncherSettings(
                schemaVersion = 1,
                legacyDoubleTapToLock = false,
                legacySwipeDownForNotifications = true,
            ),
        )

        assertEquals(LauncherGestureAction.NONE, migrated.doubleTapAction)
        assertEquals(LauncherGestureAction.NOTIFICATIONS, migrated.swipeDownAction)
    }

    @Test
    fun preservesCurrentGestureAndAssistantChoices() {
        val migrated = decodeLauncherSettings(
            StoredLauncherSettings(
                schemaVersion = CURRENT_SETTINGS_SCHEMA,
                doubleTapAction = LauncherGestureAction.APPS.name,
                swipeDownAction = LauncherGestureAction.NONE.name,
                preferredAssistant = AssistantPreference.CLAUDE.name,
            ),
        )

        assertEquals(LauncherGestureAction.APPS, migrated.doubleTapAction)
        assertEquals(LauncherGestureAction.NONE, migrated.swipeDownAction)
        assertEquals(AssistantPreference.CLAUDE, migrated.preferredAssistant)
    }

    @Test
    fun corruptedPreferenceValuesFallBackToSafeBoundedDefaults() {
        val migrated = decodeLauncherSettings(
            StoredLauncherSettings(
                schemaVersion = CURRENT_SETTINGS_SCHEMA,
                homeMode = "BROKEN",
                terminalBackgroundOpacity = Float.NaN,
                terminalCornerRadius = Int.MAX_VALUE,
                doubleTapAction = "DELETE_EVERYTHING",
                legacyDoubleTapToLock = false,
                preferredAssistant = "REMOTE_SERVER",
                appDrawerLayout = "INFINITE_CAROUSEL",
                appIconShape = "HEXAGON_FROM_CLOUD",
                appGridColumns = Int.MAX_VALUE,
                appGridRows = Int.MIN_VALUE,
                favoriteSortMode = "CLOUD_RANKING",
                frostedFallbackContrast = Float.POSITIVE_INFINITY,
            ),
        )

        assertEquals(LauncherHomeMode.TERMINAL, migrated.homeMode)
        assertTrue(migrated.terminalBackgroundOpacity.isFinite())
        assertEquals(BarelyDefaults.TERMINAL_BACKGROUND_OPACITY, migrated.terminalBackgroundOpacity)
        assertEquals(32, migrated.terminalCornerRadius)
        assertEquals(LauncherGestureAction.NONE, migrated.doubleTapAction)
        assertEquals(AssistantPreference.CHATGPT, migrated.preferredAssistant)
        assertEquals(AppDrawerLayout.LIST, migrated.appDrawerLayout)
        assertEquals(AppIconShape.ORIGINAL, migrated.appIconShape)
        assertEquals(MAX_APP_GRID_COLUMNS, migrated.appGridColumns)
        assertEquals(MIN_APP_GRID_ROWS, migrated.appGridRows)
        assertEquals(FavoriteSortMode.MANUAL, migrated.favoriteSortMode)
        assertEquals(0.5f, migrated.frostedFallbackContrast, 0f)
    }

    @Test
    fun preservesAppDrawerLayoutAndDensityChoices() {
        val migrated = decodeLauncherSettings(
            StoredLauncherSettings(
                schemaVersion = CURRENT_SETTINGS_SCHEMA,
                appDrawerLayout = AppDrawerLayout.GRID.name,
                showAppIcons = true,
                appIconShape = AppIconShape.SQUIRCLE.name,
                showAppGridLabels = false,
                appGridColumns = 5,
                appGridRows = 7,
            ),
        )

        assertEquals(AppDrawerLayout.GRID, migrated.appDrawerLayout)
        assertTrue(migrated.showAppIcons)
        assertEquals(AppIconShape.SQUIRCLE, migrated.appIconShape)
        assertFalse(migrated.showAppGridLabels)
        assertEquals(5, migrated.appGridColumns)
        assertEquals(7, migrated.appGridRows)
    }

    @Test
    fun preservesFavoriteOrderingAndBoundsFallbackContrast() {
        val migrated = decodeLauncherSettings(
            StoredLauncherSettings(
                schemaVersion = CURRENT_SETTINGS_SCHEMA,
                favoriteSortMode = FavoriteSortMode.MOST_USED.name,
                frostedFallbackContrast = 4f,
            ),
        )

        assertEquals(FavoriteSortMode.MOST_USED, migrated.favoriteSortMode)
        assertEquals(1f, migrated.frostedFallbackContrast, 0f)
    }
}
