package se.barsk.park.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import se.barsk.park.ParkApp
import se.barsk.park.utils.Utils

class SettingsViewModel : ViewModel() {

    // Get a reference to the StorageManager
    private val storageManager = ParkApp.storageManager

    // --- Crash Reporting State ---
    private val _isCrashReportingEnabled = MutableStateFlow(storageManager.crashReportingEnabled())
    val isCrashReportingEnabled = _isCrashReportingEnabled.asStateFlow()

    fun setCrashReportingEnabled(isEnabled: Boolean) {
        // Update the SharedPreferences via the StorageManager
        storageManager.setCrashReportingEnabled(isEnabled)
        _isCrashReportingEnabled.update { isEnabled }
    }

    // --- Server URL State ---
    private val _serverUrl = MutableStateFlow(storageManager.getServer())
    val serverUrl = _serverUrl.asStateFlow()

    private val _showServerDialog = MutableStateFlow(false)
    val showServerDialog = _showServerDialog.asStateFlow()

    fun onServerUrlChange(newUrl: String) {
        storageManager.setServer(newUrl)
        _serverUrl.update { newUrl }
        // Close the dialog after saving
        onDismissServerDialog()
    }

    fun onShowServerDialog() {
        _showServerDialog.update { true }
    }

    fun onDismissServerDialog() {
        _showServerDialog.update { false }
    }

    // --- Theme State ---
    private val _currentTheme = MutableStateFlow(storageManager.getTheme())
    val currentTheme = _currentTheme.asStateFlow()

    fun onThemeChange(newTheme: String) {
        // Save the new theme via the StorageManager.
        storageManager.setTheme(newTheme)
        _currentTheme.update { newTheme }
        Utils.setTheme(newTheme) // Apply the theme immediately
    }

    // --- Refresh Interval State ---
    private val _currentInterval = MutableStateFlow(storageManager.getAutomaticUpdateInterval())
    val currentInterval = _currentInterval.asStateFlow()

    fun onIntervalChange(newInterval: Long) {
        storageManager.setAutomaticUpdateInterval(newInterval)
        _currentInterval.update { newInterval }
    }
}
