package se.barsk.park.managecars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.barsk.park.ParkApp
import se.barsk.park.datatypes.OwnCar

class ManageCarsViewModel : ViewModel() {

    private val _selectedCars = MutableStateFlow<Set<OwnCar>>(emptySet())
    val selectedCars: StateFlow<Set<OwnCar>> = _selectedCars.asStateFlow()

    fun toggleSelection(car: OwnCar) {
        _selectedCars.update { currentSelection ->
            if (car in currentSelection) currentSelection - car else currentSelection + car
        }
    }

    fun selectAll() {
        _selectedCars.value = ParkApp.carCollection.getCars().toSet()
    }

    fun clearSelection() {
        _selectedCars.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            ParkApp.carCollection.removeCars(_selectedCars.value)
            clearSelection()
        }
    }

    fun addCar(car: OwnCar) {
        viewModelScope.launch {
            ParkApp.carCollection.addCar(car)
        }
    }

    fun updateCar(car: OwnCar) {
        viewModelScope.launch {
            ParkApp.carCollection.updateCar(car)
        }
    }
}
