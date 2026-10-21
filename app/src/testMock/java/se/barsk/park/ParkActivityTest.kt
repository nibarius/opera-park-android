package se.barsk.park

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.edit
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.settings.SettingsActivity
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
                ParkApp.getSharedPreferences(context).edit {
                    putBoolean(context.getString(R.string.key_privacy_on_boarding), true)
                }
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

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        actualIntent.component shouldBeEqualTo expectedIntent.component
    }

    @Test
    fun settingsMenuEntryNavigatesToSettings() {
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val settingsEntry = activity.getString(R.string.menu_settings_entry)

        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(settingsEntry, useUnmergedTree = true).performClick()

        val expectedIntent = Intent(activity, SettingsActivity::class.java)
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        actualIntent.component shouldBeEqualTo expectedIntent.component
    }

    @Test
    fun themeSettingAppliesNightMode() {
        se.barsk.park.utils.Utils.setTheme("dark")
        androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() shouldBeEqualTo androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

        se.barsk.park.utils.Utils.setTheme("light")
        androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() shouldBeEqualTo androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
    }
}
