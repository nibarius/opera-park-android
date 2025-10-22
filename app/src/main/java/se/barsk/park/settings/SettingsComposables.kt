package se.barsk.park.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import se.barsk.park.ParkTopAppBar
import se.barsk.park.R

/**
 * A Composable that mimics a PreferenceCategory.
 */
@Composable
fun PreferenceCategory(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/**
 * A Composable that mimics a basic Preference item.
 */
@Composable
fun Preference(
    title: String,
    summary: String? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        if (summary != null) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A Composable that mimics a SwitchPreference.
 */
@Composable
fun SwitchPreference(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) } // Toggle when the whole row is clicked
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes the switch to the end
    ) {
        // Column for title and summary
        Column(modifier = Modifier.weight(1f)) { // Use weight to take up available space
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // The actual switch
        Switch(
            checked = checked,
            onCheckedChange = null, // Let the parent Row handle the click
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * A Composable that mimics a ListPreference, showing a DropdownMenu on click.
 */
@Composable
fun ListPreference(
    title: String,
    selectedValue: String,
    entries: Map<String, String>, // Map of entry values to entry titles
    onValueChange: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Box is used to anchor the DropdownMenu to our Preference
    Box {
        // Use the standard Preference composable for the UI
        Preference(
            title = title,
            summary = entries[selectedValue],
            onClick = { isMenuExpanded = true }
        )

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            offset = DpOffset(x = 32.dp, y = 0.dp)
        ) {
            entries.forEach { (value, entryTitle) ->
                DropdownMenuItem(
                    text = { Text(entryTitle) },
                    onClick = {
                        onValueChange(value)
                        isMenuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ServerUrlDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // Internal state for the text field
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentUrl,
                selection = TextRange(currentUrl.length) // Place cursor at the end
            )
        )
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.settings_server)) },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text(stringResource(id = R.string.park_server_input_label)) },
                    placeholder = { Text(stringResource(id = R.string.park_server_input_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            // https://stackoverflow.com/a/75104192/1730966
                            if (!textFieldLoaded) {
                                focusRequester.requestFocus()
                                textFieldLoaded = true
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave(textFieldValue.text.trim()) }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(textFieldValue.text.trim()) }
            ) {
                Text(stringResource(id = R.string.dialog_button_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(id = R.string.dialog_button_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateUp: () -> Unit
) {
    ParkTopAppBar(
        title = { Text(stringResource(id = R.string.settings_activity_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.manage_cars_navigate_up)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
