package se.barsk.park.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import se.barsk.park.ParkApp
import se.barsk.park.utils.Utils

class SettingsViewModel : ViewModel() {

    private val storageManager = ParkApp.storageManager

    // --- Crash Reporting State ---
    val isCrashReportingEnabled: StateFlow<Boolean> = storageManager.crashReportingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = storageManager.crashReportingEnabled()
    )

    fun setCrashReportingEnabled(isEnabled: Boolean) {
        storageManager.setCrashReportingEnabled(isEnabled)
    }

    // --- Server URL State ---
    val serverUrl: StateFlow<String> = storageManager.serverFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = storageManager.getServer()
    )

    private val _showServerDialog = MutableStateFlow(false)
    val showServerDialog = _showServerDialog.asStateFlow()

    fun onServerUrlChange(newUrl: String) {
        storageManager.setServer(newUrl)
        onDismissServerDialog()
    }

    fun onShowServerDialog() {
        _showServerDialog.update { true }
    }

    fun onDismissServerDialog() {
        _showServerDialog.update { false }
    }

    // --- Theme State ---
    val currentTheme: StateFlow<String> = storageManager.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = storageManager.getTheme()
    )

    fun onThemeChange(newTheme: String) {
        storageManager.setTheme(newTheme)
        Utils.setTheme(newTheme)
    }

    // --- Refresh Interval State ---
    val currentInterval: StateFlow<Long> = storageManager.automaticUpdateIntervalFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = storageManager.getAutomaticUpdateInterval()
    )

    fun onIntervalChange(newInterval: Long) {
        storageManager.setAutomaticUpdateInterval(newInterval)
    }
}
