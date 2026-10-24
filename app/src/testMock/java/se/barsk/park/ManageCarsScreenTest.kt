package se.barsk.park

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.managecars.ManageCarsDestination
import se.barsk.park.managecars.ManageCarsViewModel
import se.barsk.park.theme.ParkTheme

@LooperMode(LooperMode.Mode.PAUSED)
class ManageCarsScreenTest : RobolectricTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        // Reset the car collection at the test of each test to have a clean state.
        ParkApp.init(context())
        (ParkApp.carCollection as MockCarCollection).reset()
    }

    private fun showScreen(initialAddCar: Boolean = false) {
        val viewModel = ManageCarsViewModel()
        composeTestRule.setContent {
            ParkTheme {
                ManageCarsDestination(
                    initialAddCar = initialAddCar,
                    onNavigateUp = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @After
    fun tearDown() {
        // No particular cleanup is needed
    }

    @Test
    fun carListShownWithCarsTest() {
        showScreen()
        // 1. Verify both unique registration numbers are on the screen.
        composeTestRule.onNodeWithText("ALP 110").assertIsDisplayed()
        composeTestRule.onNodeWithText("MLB 803").assertIsDisplayed()

        // 2. Verify that there are exactly two instances of the owner's name.
        composeTestRule.onAllNodesWithText("Margaretha").assertCountEquals(2)

        val placeholderText =
            composeTestRule.activity.getString(R.string.manage_cars_list_placeholder)
        composeTestRule.onNodeWithText(placeholderText).assertDoesNotExist()
    }

    @Test
    fun placeholderShownWithNoCarsTest() {
        showScreen()
        val carCollection = ParkApp.carCollection as MockCarCollection
        carCollection.replaceContent(mutableListOf())

        val placeholderText =
            composeTestRule.activity.getString(R.string.manage_cars_list_placeholder)

        composeTestRule.onNodeWithText(placeholderText).assertIsDisplayed()
        composeTestRule.onNodeWithText("ALP 110").assertDoesNotExist()
    }

    @Test
    fun openAddDialogWhenClickingFabTest() {
        showScreen()
        val activity = composeTestRule.activity
        val fabDescription = activity.getString(R.string.add_car_button)
        val dialogTitle = activity.getString(R.string.add_car_dialog_title)
        composeTestRule.onNodeWithContentDescription(fabDescription).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }

    @Test
    fun cancelAddDialogFabShouldBeVisibleTest() {
        showScreen()
        val activity = composeTestRule.activity
        val fabDescription = activity.getString(R.string.add_car_button)
        val cancelButtonText = activity.getString(R.string.dialog_button_cancel)
        composeTestRule.onNodeWithContentDescription(fabDescription).performClick()
        composeTestRule.onNodeWithText(cancelButtonText).performClick()
        composeTestRule.onNodeWithContentDescription(fabDescription).assertIsDisplayed()
    }

    @Test
    fun disabledAddButtonInCreateDialogTest() {
        showScreen()
        val activity = composeTestRule.activity
        val fabDescription = activity.getString(R.string.add_car_button)
        val saveButtonText = activity.getString(R.string.dialog_button_save)
        val regNoLabel = activity.getString(R.string.manage_car_dialog_license_plate_label)
        val ownerLabel = activity.getString(R.string.manage_car_dialog_owner_label)

        composeTestRule.onNodeWithContentDescription(fabDescription).performClick()
        val saveButton = composeTestRule.onNodeWithText(saveButtonText)
        val regNoField = composeTestRule.onNodeWithText(regNoLabel)
        val ownerField = composeTestRule.onNodeWithText(ownerLabel)

        saveButton.assertIsNotEnabled()
        regNoField.performTextInput("REGNO")
        saveButton.assertIsNotEnabled()
        regNoField.performTextClearance()
        ownerField.performTextInput("owner")
        saveButton.assertIsNotEnabled()
        regNoField.performTextInput("REGNO")
        saveButton.assertIsEnabled()
    }

    @Test
    fun chooseMenuEntryTest() {
        showScreen()
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val selectOption = activity.getString(R.string.manage_cars_select_option)
        val contextualTitle = activity.getString(R.string.manage_cars_action_mode_title, 0)
        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(selectOption, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(contextualTitle).assertIsDisplayed()
        val normalTitle = activity.getString(R.string.manage_cars_activity_title)
        composeTestRule.onNodeWithText(normalTitle).assertDoesNotExist()
    }

    @Test
    fun selectAllMenuEntryTest() {
        showScreen()
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val selectAllOption = activity.getString(R.string.manage_cars_select_all_option)
        val carCount = ParkApp.carCollection.getCars().size
        val contextualTitle = activity.getString(R.string.manage_cars_action_mode_title, carCount)
        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(selectAllOption, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(contextualTitle).assertIsDisplayed()
    }

    @Test
    fun menuSelectionThenTogglingExitsActionMode() {
        showScreen()
        val activity = composeTestRule.activity
        val menuDescription = activity.getString(R.string.manage_cars_more_options)
        val selectOption = activity.getString(R.string.manage_cars_select_option)
        val carToSelect = "ALP 110"

        val titleZeroSelected = activity.getString(R.string.manage_cars_action_mode_title, 0)
        val titleOneSelected = activity.getString(R.string.manage_cars_action_mode_title, 1)
        val normalTitle = activity.getString(R.string.manage_cars_activity_title)

        // 1. Enter selection mode from the menu.
        composeTestRule.onNodeWithContentDescription(menuDescription).performClick()
        composeTestRule.onNodeWithText(selectOption, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(titleZeroSelected).assertIsDisplayed()

        // 2. Select one car.
        composeTestRule.onNodeWithText(carToSelect).performClick()
        composeTestRule.onNodeWithText(titleOneSelected).assertIsDisplayed()

        // 3. Deselect the same car.
        composeTestRule.onNodeWithText(carToSelect).performClick()

        // 4. Assert that action mode is exited and the normal title is displayed.
        composeTestRule.onNodeWithText(normalTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(titleOneSelected).assertDoesNotExist()
    }

    @Test
    fun clickingCarEntersEditModeTest() {
        showScreen()
        val carToClick = "ALP 110"
        val editDialogTitle = composeTestRule.activity.getString(R.string.edit_car_dialog_title)
        composeTestRule.onNodeWithText(carToClick).performClick()
        composeTestRule.onNodeWithText(editDialogTitle).assertIsDisplayed()
        // 3 nodes, the two list entries and the owner text field in the edit dialog
        composeTestRule.onAllNodesWithText("Margaretha").assertCountEquals(3)
        // 2 nodes, one list entry and one in the edit dialog
        composeTestRule.onAllNodesWithText("ALP 110").assertCountEquals(2)
    }

    @Test
    fun longClickEntersActionModeAndSelectsItem() {
        showScreen()
        val activity = composeTestRule.activity
        val carToLongClick = "MLB 803"
        val contextualTitleOneSelected =
            activity.getString(R.string.manage_cars_action_mode_title, 1)
        composeTestRule.onNodeWithText(carToLongClick).performTouchInput { longClick() }
        composeTestRule.onNodeWithText(contextualTitleOneSelected).assertIsDisplayed()
        val normalTitle = activity.getString(R.string.manage_cars_activity_title)
        composeTestRule.onNodeWithText(normalTitle).assertDoesNotExist()
        val fabDescription = activity.getString(R.string.add_car_button)
        composeTestRule.onNodeWithContentDescription(fabDescription).assertDoesNotExist()
    }

    @Test
    fun clickInActionModeTogglesSelection() {
        showScreen()
        val activity = composeTestRule.activity
        val car1 = "ALP 110"
        val car2 = "MLB 803"
        val titleOneSelected = activity.getString(R.string.manage_cars_action_mode_title, 1)
        val titleTwoSelected = activity.getString(R.string.manage_cars_action_mode_title, 2)
        val normalTitle = activity.getString(R.string.manage_cars_activity_title)

        // 1. Long click car1 to enter action mode with one selection
        composeTestRule.onNodeWithText(car1).performTouchInput { longClick() }
        composeTestRule.onNodeWithText(titleOneSelected).assertIsDisplayed()

        // 2. Click car2 to add it to the selection
        composeTestRule.onNodeWithText(car2).performClick()
        composeTestRule.onNodeWithText(titleTwoSelected).assertIsDisplayed()

        // 3. CORRECTED STEP: Click car2 again to deselect it, leaving one selected
        composeTestRule.onNodeWithText(car2).performClick()
        composeTestRule.onNodeWithText(titleOneSelected).assertIsDisplayed()

        // 4. CORRECTED STEP: Click car1 to deselect the last item, exiting action mode
        composeTestRule.onNodeWithText(car1).performClick()

        // 5. Assert that we are back to the normal state
        composeTestRule.onNodeWithText(normalTitle).assertIsDisplayed()
    }

    @Test
    fun closeButtonExitsActionMode() {
        showScreen()
        val activity = composeTestRule.activity
        val carToLongClick = "ALP 110"
        val closeButtonDescription = activity.getString(R.string.manage_cars_close)
        val normalTitle = activity.getString(R.string.manage_cars_activity_title)
        composeTestRule.onNodeWithText(carToLongClick).performTouchInput { longClick() }
        composeTestRule.onNodeWithContentDescription(closeButtonDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(closeButtonDescription).performClick()
        composeTestRule.onNodeWithText(normalTitle).assertIsDisplayed()
        val fabDescription = activity.getString(R.string.add_car_button)
        composeTestRule.onNodeWithContentDescription(fabDescription).assertIsDisplayed()
    }

    @Test
    fun initialAddCarShowsDialogTest() {
        showScreen(initialAddCar = true)
        val activity = composeTestRule.activity
        val dialogTitle = activity.getString(R.string.add_car_dialog_title)
        val fabDescription = activity.getString(R.string.add_car_button)

        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        // The FAB still exists in the composition, even though it's obscured by the dialog's scrim.
        composeTestRule.onNodeWithContentDescription(fabDescription).assertExists()
    }

    @Test
    fun dismissedAddDialogStaysClosedAfterRecreationTest() {
        val restorationTester = StateRestorationTester(composeTestRule)
        val viewModel = ManageCarsViewModel()
        restorationTester.setContent {
            ParkTheme {
                ManageCarsDestination(initialAddCar = true, onNavigateUp = {}, viewModel = viewModel)
            }
        }
        val activity = composeTestRule.activity
        val dialogTitle = activity.getString(R.string.add_car_dialog_title)
        val cancelButtonText = activity.getString(R.string.dialog_button_cancel)

        composeTestRule.onNodeWithText(cancelButtonText).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
    }
}
