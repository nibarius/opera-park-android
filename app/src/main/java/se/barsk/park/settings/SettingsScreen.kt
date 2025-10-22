package se.barsk.park.settings

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import se.barsk.park.BuildConfig
import se.barsk.park.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onShowThirdPartyLicenses: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current

    val privacyUrl = stringResource(id = R.string.url_privacy_statement)
    val privacyIntent = remember(privacyUrl) {
        Intent(Intent.ACTION_VIEW, privacyUrl.toUri())
    }

    val isCrashReportingEnabled by viewModel.isCrashReportingEnabled.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val showServerDialog by viewModel.showServerDialog.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentInterval by viewModel.currentInterval.collectAsState()

    // Create the map of theme values to display names
    val themeEntries = remember {
        mapOf(
            "system" to context.getString(R.string.theme_system),
            "light" to context.getString(R.string.theme_light),
            "dark" to context.getString(R.string.theme_dark)
        )
    }

    // Create the map of interval values to display names
    val intervalEntries = remember {
        mapOf(
            "10" to context.getString(R.string.seconds10),
            "30" to context.getString(R.string.seconds30),
            "0" to context.getString(R.string.auto_refresh_disabled)
        )
    }

    // --- Show the dialog when state is true ---
    if (showServerDialog) {
        ServerUrlDialog(
            currentUrl = serverUrl,
            onDismiss = { viewModel.onDismissServerDialog() },
            onSave = { newUrl -> viewModel.onServerUrlChange(newUrl) }
        )
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- General Preferences ---
            PreferenceCategory(stringResource(id = R.string.settings_general_group_title))
            ListPreference(
                title = stringResource(id = R.string.refresh_interval),
                selectedValue = currentInterval.toString(),
                entries = intervalEntries,
                onValueChange = { newInterval -> viewModel.onIntervalChange(newInterval.toLong()) }
            )
            ListPreference(
                title = stringResource(id = R.string.theme),
                selectedValue = currentTheme,
                entries = themeEntries,
                onValueChange = { newTheme -> viewModel.onThemeChange(newTheme) }
            )
            Preference(
                title = stringResource(R.string.settings_server),
                summary = serverUrl.ifEmpty { stringResource(R.string.no_server_placeholder_text) },
                onClick = { viewModel.onShowServerDialog() }
            )
            PreferenceCategory(stringResource(id = R.string.settings_privacy_group_title))

            SwitchPreference(
                title = stringResource(R.string.settings_crash_reporting),
                summary = stringResource(R.string.settings_crash_reporting_summary),
                checked = isCrashReportingEnabled,
                onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
            )
            Preference(
                title = stringResource(R.string.settings_privacy_statement),
                onClick = { context.startActivity(privacyIntent) }
            )

            // --- About Preferences ---
            PreferenceCategory(
                stringResource(
                    R.string.settings_about_group_title,
                    stringResource(id = R.string.app_name)
                )
            )
            Preference(stringResource(id = R.string.settings_version, BuildConfig.VERSION_NAME))
            Preference(
                title = stringResource(id = R.string.notices_title),
                onClick = onShowThirdPartyLicenses
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable") // OK to do for the preview
@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        viewModel = SettingsViewModel(),
        onShowThirdPartyLicenses = {},
        onNavigateUp = {}
    )
}
