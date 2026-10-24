package se.barsk.park.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.LooperMode
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.RobolectricTest
import se.barsk.park.storage.MockStorageManager
import se.barsk.park.theme.ParkTheme

@LooperMode(LooperMode.Mode.PAUSED)
class SettingsScreenTest : RobolectricTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        // Set the initial state for all settings before each test runs.
        // Most settings are picked up from shared preferences which have
        // an unknown state when the first test is run.
        ParkApp.init(context())
        with(ParkApp.storageManager as MockStorageManager) {
            reset()
            setTheme("system")
            setAutomaticUpdateInterval(10L) // Use the real value
            setCrashReportingEnabled(true)
        }
        // Create the view model after the storage has been set up so that it picks up the
        // initial values.
        val viewModel = SettingsViewModel()
        composeTestRule.setContent {
            ParkTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onShowThirdPartyLicenses = { },
                    onNavigateUp = { }
                )
            }
        }
    }

    @After
    fun tearDown() {
        // Reset all settings after the test finishes to leave shared
        // preferences in a known state when the tests have finished.
        with(ParkApp.storageManager as MockStorageManager) {
            reset()
            setTheme("system")
            setAutomaticUpdateInterval(10L)
            setCrashReportingEnabled(true)
        }
    }

    @Test
    fun serverSetting() {
        val activity = composeTestRule.activity
        val serverLabel = activity.getString(R.string.settings_server)
        val serverInputLabel = activity.getString(R.string.park_server_input_label)
        val saveButtonText = activity.getString(R.string.dialog_button_save)
        val newServerUrl = "https://new.server.net/"

        composeTestRule.onNodeWithText("https://park.example.com/").assertIsDisplayed()
        composeTestRule.onNodeWithText(serverLabel).performClick()
        val serverTextField = composeTestRule.onNodeWithText(serverInputLabel)
        serverTextField.assertIsDisplayed()
        serverTextField.performTextClearance()
        serverTextField.performTextInput(newServerUrl)
        composeTestRule.onNodeWithText(saveButtonText).performClick()
        serverTextField.assertDoesNotExist()
        composeTestRule.onNodeWithText(newServerUrl).assertIsDisplayed()
    }

    @Test
    fun themeSetting() {
        val activity = composeTestRule.activity
        val themeLabel = activity.getString(R.string.theme)
        val initialTheme = activity.getString(R.string.theme_system)
        val newTheme = activity.getString(R.string.theme_dark)
        val newThemeValue = "dark"

        composeTestRule.onRoot().printToString().also { println(it) }
        composeTestRule.onNodeWithText(initialTheme).assertIsDisplayed()

        composeTestRule.onNodeWithText(themeLabel).performClick()
        composeTestRule.onNodeWithText(newTheme).performClick()
        composeTestRule.onNodeWithText(initialTheme).assertDoesNotExist()
        composeTestRule.onNodeWithText(newTheme).assertIsDisplayed()
        assert(ParkApp.storageManager.getTheme() == newThemeValue)
    }

    @Test
    fun automaticRefreshSetting() {
        val activity = composeTestRule.activity
        val refreshLabel = activity.getString(R.string.refresh_interval)
        val initialRefresh = activity.getString(R.string.seconds10)
        val newRefresh = activity.getString(R.string.seconds30)
        val newRefreshValue = 30L
        composeTestRule.onNodeWithText(initialRefresh).assertIsDisplayed()
        composeTestRule.onNodeWithText(refreshLabel).performClick()
        composeTestRule.onNodeWithText(newRefresh).performClick()
        composeTestRule.onNodeWithText(initialRefresh).assertDoesNotExist()
        composeTestRule.onNodeWithText(newRefresh).assertIsDisplayed()
        assert(ParkApp.storageManager.getAutomaticUpdateInterval() == newRefreshValue)
    }


    @Test
    fun crashReportingSetting() {
        val activity = composeTestRule.activity
        val crashLabel = activity.getString(R.string.settings_crash_reporting)

        assert(ParkApp.storageManager.crashReportingEnabled())
        composeTestRule.onNodeWithText(crashLabel).performClick()
        assert(!ParkApp.storageManager.crashReportingEnabled())
        composeTestRule.onNodeWithText(crashLabel).performClick()
        assert(ParkApp.storageManager.crashReportingEnabled())
    }
}
