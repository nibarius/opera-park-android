package se.barsk.park

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.managecars.ManageCarsActivity

@LooperMode(LooperMode.Mode.PAUSED)
class ManageCarsActivityIntentTest : RobolectricTest() {

    // 1. Use createEmptyComposeRule(). It does NOT launch any activity.
    // This gives us full manual control.
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        ParkApp.carCollection = MockCarCollection()
    }

    @Test
    fun startWithIntentTest() {
        // 2. Prepare the intent you want to test.
        val intent = Intent(context(), ManageCarsActivity::class.java).apply {
            putExtra(INTENT_EXTRA_ADD_CAR, true)
        }

        // 3. Launch the activity manually using ActivityScenario.
        ActivityScenario.launch<ManageCarsActivity>(intent).use { scenario ->
            // Now, the test will run against the activity started with the intent.
            lateinit var dialogTitle: String
            lateinit var fabDescription: String
            scenario.onActivity {
                dialogTitle = it.getString(R.string.add_car_dialog_title)
                fabDescription = it.getString(R.string.add_car_button)
            }

            // Then the dialog should be visible immediately
            composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()

            // And the FAB should still exist in the composition, even though it's obscured by the dialog's scrim.
            // This is the simplest, most robust assertion we can make.
            composeTestRule.onNodeWithContentDescription(fabDescription).assertExists()
        }
    }
}
