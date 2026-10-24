package se.barsk.park

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.storage.MockStorageManager

@LooperMode(LooperMode.Mode.PAUSED)
class ParkActivityTest : RobolectricTest() {

    private val composeTestRule = createAndroidComposeRule<ParkActivity>()

    // Mark privacy onboarding as seen before the activity launches so the modal privacy
    // dialog doesn't show on first launch and block interaction with the app bar. The flag is
    // read in onResume, which runs before @Before, so it must be set in a rule that runs before
    // the activity is created.
    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(object : ExternalResource() {
            override fun before() {
                val context = context()
                ParkApp.init(context)
                ParkApp.storageManager.setPrivacyOnBoardingSeen()
            }
        })
        .around(composeTestRule)

    @After
    fun tearDown() {
        ParkApp.carCollection = MockCarCollection()
        ParkApp.networkManager = MockNetworkManager()
        (ParkApp.storageManager as MockStorageManager).reset()
    }

    @Test
    fun manageCarsMenuEntryNavigatesToManageCars() {
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val manageCarsEntry = activity.getString(R.string.menu_manage_cars_entry)

        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(manageCarsEntry, useUnmergedTree = true).performClick()

        val manageCarsTitle = activity.getString(R.string.manage_cars_activity_title)
        val addCarDescription = activity.getString(R.string.add_car_button)
        composeTestRule.onNodeWithText(manageCarsTitle).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(addCarDescription).assertIsDisplayed()
    }

    @Test
    fun settingsMenuEntryNavigatesToSettings() {
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val settingsEntry = activity.getString(R.string.menu_settings_entry)

        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(settingsEntry, useUnmergedTree = true).performClick()

        val settingsTitle = activity.getString(R.string.settings_activity_title)
        val serverLabel = activity.getString(R.string.settings_server)
        composeTestRule.onNodeWithText(settingsTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(serverLabel).assertIsDisplayed()
    }

    @Test
    fun backFromSettingsReturnsToParkedCars() {
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val settingsEntry = activity.getString(R.string.menu_settings_entry)
        val serverLabel = activity.getString(R.string.settings_server)

        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(settingsEntry, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(serverLabel).assertIsDisplayed()

        composeTestRule.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }

        composeTestRule.onNodeWithText(serverLabel).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(menuDescription).assertIsDisplayed()
    }

    @Test
    fun addCarPlaceholderOpensManageCarsWithAddDialog() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val activity = composeTestRule.activity
        val addCarButtonText = activity.getString(R.string.add_car_button).uppercase()
        val dialogTitle = activity.getString(R.string.add_car_dialog_title)

        composeTestRule.onNodeWithText(addCarButtonText).performClick()

        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }

    @Test
    fun themeSettingAppliesNightMode() {
        se.barsk.park.utils.Utils.setTheme("dark")
        androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() shouldBeEqualTo androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

        se.barsk.park.utils.Utils.setTheme("light")
        androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() shouldBeEqualTo androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
    }
}
