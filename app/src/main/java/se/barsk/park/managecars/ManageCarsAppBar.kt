package se.barsk.park.managecars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.barsk.park.R

@Composable
fun ManageCarsAppBar(
    isActionMode: Boolean,
    selectedItemCount: Int,
    onCloseActionMode: () -> Unit,
    onDeleteSelected: () -> Unit,
    onNavigateUp: () -> Unit,
    // callbacks for the menu entries
    onEnterSelectMode: () -> Unit,
    onSelectAll: () -> Unit
) {
    if (isActionMode) {
        ContextualTopAppBar(
            selectedItemCount = selectedItemCount,
            onCloseActionMode = onCloseActionMode,
            onDeleteSelected = onDeleteSelected
        )
    } else {
        NormalTopAppBar(
            onNavigateUp = onNavigateUp,
            onEnterSelectMode = onEnterSelectMode,
            onSelectAll = onSelectAll
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopAppBar(
    onNavigateUp: () -> Unit,
    onEnterSelectMode: () -> Unit,
    onSelectAll: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.manage_cars_activity_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.manage_cars_navigate_up)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary

        ),
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.manage_cars_more_options)
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.manage_cars_select_option)) },
                        onClick = {
                            onEnterSelectMode()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.manage_cars_select_all_option)) },
                        onClick = {
                            onSelectAll()
                            showMenu = false
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextualTopAppBar(
    selectedItemCount: Int,
    onCloseActionMode: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(
                    R.string.manage_cars_action_mode_title,
                    selectedItemCount
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseActionMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.manage_cars_close)
                )
            }
        },
        actions = {
            IconButton(onClick = onDeleteSelected) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.manage_cars_context_menu_delete)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Preview
@Composable
private fun NormalAppBarPreview() {
    NormalTopAppBar(onNavigateUp = {}, onEnterSelectMode = {}, onSelectAll = {})
}

@Preview
@Composable
private fun ContextualAppBarPreview() {
    ContextualTopAppBar(
        selectedItemCount = 2,
        onCloseActionMode = {},
        onDeleteSelected = {}
    )
}