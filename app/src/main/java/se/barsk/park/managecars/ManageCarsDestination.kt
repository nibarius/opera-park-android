package se.barsk.park.managecars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.barsk.park.ParkApp
import se.barsk.park.datatypes.OwnCar

/**
 * Screen for adding, editing and removing the user's own cars.
 * @param initialAddCar if true the add car dialog is shown when the screen is first opened
 */
@Composable
fun ManageCarsDestination(
    initialAddCar: Boolean,
    onNavigateUp: () -> Unit,
    viewModel: ManageCarsViewModel = viewModel()
) {
    val selectedCars by viewModel.selectedCars.collectAsStateWithLifecycle()
    val carList by ParkApp.carCollection.ownCarsFlow.collectAsStateWithLifecycle()

    var isActionMode by remember(selectedCars) { mutableStateOf(selectedCars.isNotEmpty()) }

    // State to control which dialog is shown. rememberSaveable so the dialog state survives
    // configuration changes (e.g. toggling dark mode recreates the activity). Plain remember
    // would re-read initialAddCar on every recreation and wrongly reopen the dialog after it
    // had been dismissed.
    var showAddDialog by rememberSaveable { mutableStateOf(initialAddCar) }
    var carToEdit by remember { mutableStateOf<OwnCar?>(null) }

    // Derived state to create the list entries for the UI
    val carsForUi by remember(carList, selectedCars) {
        derivedStateOf {
            carList.map { car -> ManageCarsListEntry(car, car in selectedCars) }
        }
    }

    Scaffold(
        topBar = {
            ManageCarsAppBar(
                isActionMode = isActionMode,
                selectedItemCount = selectedCars.size,
                onCloseActionMode = {
                    viewModel.clearSelection()
                    isActionMode = false
                },
                onDeleteSelected = { viewModel.deleteSelected() },
                onNavigateUp = onNavigateUp,
                onEnterSelectMode = { isActionMode = true },
                onSelectAll = { viewModel.selectAll() }
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
                    viewModel.toggleSelection(car)
                } else {
                    carToEdit = car // Show edit dialog by setting state
                }
            },
            onLongClick = { car -> viewModel.toggleSelection(car) }
        )
    }

    if (showAddDialog) {
        AddCarDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirmation = { newCar ->
                viewModel.addCar(newCar)
                showAddDialog = false
            }
        )
    }

    carToEdit?.let { car ->
        EditCarDialog(
            car = car,
            onDismissRequest = { carToEdit = null },
            onConfirmation = { updatedCar ->
                viewModel.updateCar(updatedCar)
                carToEdit = null
                viewModel.clearSelection()
            }
        )
    }
}
