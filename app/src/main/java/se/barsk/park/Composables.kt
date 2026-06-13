package se.barsk.park

import android.util.Patterns
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import se.barsk.park.theme.DarkAppBar
import se.barsk.park.utils.Utils

/**
 * A custom top app bar that uses a dark background with primary colored text
 * in dark mode, mimicking the old XML theme behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Determine which colors to use based on the theme
    val appBarColors = if (isSystemInDarkTheme()) {
        // In Dark Mode: Use a dark background and primary color for the title.
        TopAppBarDefaults.topAppBarColors(
            containerColor = DarkAppBar,
            scrolledContainerColor = DarkAppBar, // Keep color consistent when scrolling
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    } else {
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = { actions() },
        scrollBehavior = scrollBehavior,
        colors = appBarColors // Apply our custom colors
    )
}

/**
 * A dialog for entering a park server URL. Used both from the main screen (when no server
 * is configured) and from the settings screen. The save button is enabled while the entered
 * text is a valid URL (after [Utils.fixUrl] normalization), or empty (which clears the
 * configured server and returns the app to the no-server state).
 */
@Composable
fun ServerUrlDialog(
    currentUrl: String,
    title: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
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

    val isSaveEnabled by remember {
        derivedStateOf {
            textFieldValue.text.isBlank() ||
                Patterns.WEB_URL.matcher(Utils.fixUrl(textFieldValue.text)).matches()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text(stringResource(id = R.string.park_server_input_label)) },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.park_server_input_hint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
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
                        onDone = { if (isSaveEnabled) onSave(textFieldValue.text.trim()) }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(textFieldValue.text.trim()) },
                enabled = isSaveEnabled
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
