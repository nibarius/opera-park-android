package se.barsk.park.managecars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.ParkApp
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.theme.ParkTheme


class ManageCarsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        // It's good practice to call this early in onCreate
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set the content directly with Compose
        setContent {
            // State management
            val selectedCars by selectedCarsState.collectAsState()
            val carList by ParkApp.carCollection.ownCarsFlow.collectAsStateWithLifecycle()

            var isActionMode by remember(selectedCars) { mutableStateOf(selectedCars.isNotEmpty()) }

            // State to control which dialog is shown
            var showAddDialog by remember {
                mutableStateOf(
                    intent.getBooleanExtra(
                        INTENT_EXTRA_ADD_CAR,
                        false
                    )
                )
            }
            var carToEdit by remember { mutableStateOf<OwnCar?>(null) }

            // Derived state to create the list entries for the UI
            val carsForUi by remember(carList, selectedCars) {
                derivedStateOf {
                    carList.map { car -> ManageCarsListEntry(car, car in selectedCars) }
                }
            }

            ParkTheme {
                Scaffold(
                    topBar = {
                        ManageCarsAppBar(
                            isActionMode = isActionMode,
                            selectedItemCount = selectedCars.size,
                            onCloseActionMode = {
                                clearSelection()
                                isActionMode = false
                            },
                            onDeleteSelected = { deleteSelectedItems() },
                            onNavigateUp = { finish() }, // Finishes the activity
                            onEnterSelectMode = { isActionMode = true },
                            onSelectAll = { selectAllItems() }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = !isActionMode,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            ManageCarsFab { showAddDialog = true }
                        }
                    }
                ) { innerPadding ->
                    ManageCarsScreen(
                        modifier = Modifier.padding(innerPadding),
                        cars = carsForUi,
                        onClick = { car ->
                            if (isActionMode) {
                                toggleSelection(car)
                            } else {
                                carToEdit = car // Show edit dialog by setting state
                            }
                        },
                        onLongClick = { car -> toggleSelection(car) }
                    )
                }

                // Show Add Car Dialog
                if (showAddDialog) {
                    AddCarDialog(
                        onDismissRequest = { showAddDialog = false },
                        onConfirmation = { newCar ->
                            lifecycleScope.launch {
                                ParkApp.carCollection.addCar(newCar)
                            }
                            showAddDialog = false
                        }
                    )
                }

                // Show Edit Car Dialog
                carToEdit?.let { car ->
                    EditCarDialog(
                        car = car,
                        onDismissRequest = { carToEdit = null },
                        onConfirmation = { updatedCar ->
                            lifecycleScope.launch {
                                ParkApp.carCollection.updateCar(updatedCar)
                            }
                            carToEdit = null
                            clearSelection() // As you had before
                        }
                    )
                }
            }
        }
    }

    private fun selectAllItems() {
        selectedCarsState.value = ParkApp.carCollection.getCars().toSet()
    }

    private fun deleteSelectedItems() {
        lifecycleScope.launch {
            ParkApp.carCollection.removeCars(selectedCarsState.value)
            clearSelection()
        }
    }

    private fun clearSelection() {
        selectedCarsState.value = emptySet()
    }

    // My new stuff down here:
    val selectedCarsState = MutableStateFlow<Set<OwnCar>>(emptySet())

    fun toggleSelection(car: OwnCar) {
        selectedCarsState.update { currentSelection ->
            val newSelection = currentSelection.toMutableSet()
            if (newSelection.contains(car)) {
                newSelection.remove(car)
            } else {
                newSelection.add(car)
            }
            newSelection
        }
    }
}
