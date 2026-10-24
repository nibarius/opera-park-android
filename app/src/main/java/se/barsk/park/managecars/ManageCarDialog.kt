package se.barsk.park.managecars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

// A private, reusable base composable for both adding and editing a car.
@Composable
private fun BaseManageCarDialog(
    title: String,
    initialCar: OwnCar?,
    onDismissRequest: () -> Unit,
    onConfirmation: (OwnCar) -> Unit
) {
    // Initialize state with the car's current data, or empty if it's a new car.
    var regNo by rememberSaveable { mutableStateOf(initialCar?.regNo ?: "") }
    var owner by rememberSaveable { mutableStateOf(initialCar?.owner ?: "") }

    // Derived state to control button enabled/disabled status.
    val isFormValid by remember(regNo, owner) {
        mutableStateOf(regNo.isNotBlank() && owner.isNotBlank())
    }

    fun save() {
        val finalRegNo = regNo.trim()
        val finalOwner = owner.trim()
        @Suppress("IfThenToElvis") // Suppressing elvis makes the code more clear
        val resultCar = if (initialCar != null) {
            // Editing an existing car, preserve its ID
            initialCar.copy(regNo = finalRegNo, owner = finalOwner)
        } else {
            // Creating a new car, let the ID be generated
            OwnCar(regNo = finalRegNo, owner = finalOwner)
        }
        onConfirmation(resultCar)
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            // Hardware keyboard Enter saves the car from any field when the form is valid, like
            // the default button in a desktop dialog. Otherwise it moves on to the next field.
            // The soft keyboard keeps its normal Next / Done actions. The focus manager must be
            // read inside the dialog since the dialog has its own window.
            val focusManager = LocalFocusManager.current
            val hardwareEnterModifier = Modifier.onPreviewKeyEvent { event ->
                if (event.key != Key.Enter && event.key != Key.NumPadEnter) {
                    false
                } else {
                    if (event.type == KeyEventType.KeyUp) {
                        if (isFormValid) save() else focusManager.moveFocus(FocusDirection.Next)
                    }
                    // Consume both key down and key up so the text field doesn't also handle Enter
                    true
                }
            }
            Column {
                OutlinedTextField(
                    modifier = hardwareEnterModifier,
                    value = regNo,
                    onValueChange = { regNo = it.uppercase() },
                    label = { Text(stringResource(R.string.manage_car_dialog_license_plate_label)) },
                    placeholder = { Text(stringResource(R.string.manage_car_dialog_license_plate_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    modifier = hardwareEnterModifier.padding(top = 8.dp),
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text(stringResource(R.string.manage_car_dialog_owner_label)) },
                    placeholder = { Text(stringResource(R.string.manage_car_dialog_owner_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isFormValid) save() }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { save() },
                enabled = isFormValid
            ) {
                Text(stringResource(R.string.dialog_button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

@Composable
fun AddCarDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (OwnCar) -> Unit
) {
    BaseManageCarDialog(
        title = stringResource(id = R.string.add_car_dialog_title),
        initialCar = null, // No initial car data for "add"
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation
    )
}

@Composable
fun EditCarDialog(
    car: OwnCar,
    onDismissRequest: () -> Unit,
    onConfirmation: (OwnCar) -> Unit
) {
    BaseManageCarDialog(
        title = stringResource(id = R.string.edit_car_dialog_title),
        initialCar = car, // Pass the existing car data for "edit"
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation
    )
}