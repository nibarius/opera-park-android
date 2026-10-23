package se.barsk.park.storage

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A mock implementation of the storage manager with a dummy server set automatically
 */
class MockStorageManager(context: Context) : StorageManager(context) {
    private val defaultServer = "https://park.example.com/"
    private var server = defaultServer
    private var crashReporting = true
    private var automaticUpdateInterval = 10L
    private var currentTheme = "system"
    private var privacyOnBoardingSeen = true

    private val _serverFlow = MutableStateFlow(defaultServer)
    override val serverFlow: Flow<String> get() = _serverFlow

    private val _crashReportingFlow = MutableStateFlow(crashReporting)
    override val crashReportingFlow: Flow<Boolean> get() = _crashReportingFlow

    private val _automaticUpdateIntervalFlow = MutableStateFlow(automaticUpdateInterval)
    override val automaticUpdateIntervalFlow: Flow<Long> get() = _automaticUpdateIntervalFlow

    private val _themeFlow = MutableStateFlow(currentTheme)
    override val themeFlow: Flow<String> get() = _themeFlow

    private val _privacyOnBoardingFlow = MutableStateFlow(privacyOnBoardingSeen)
    override val privacyOnBoardingFlow: Flow<Boolean> get() = _privacyOnBoardingFlow

    override fun hasServer() = server.isNotEmpty()

    override fun getServer() = server
    override fun setServer(server: String) {
        this.server = server
        _serverFlow.value = server
    }

    override fun crashReportingEnabled(): Boolean = crashReporting

    override fun setCrashReportingEnabled(isEnabled: Boolean) {
        this.crashReporting = isEnabled
        _crashReportingFlow.value = isEnabled
    }

    override fun getAutomaticUpdateInterval(): Long = automaticUpdateInterval

    override fun setAutomaticUpdateInterval(interval: Long) {
        this.automaticUpdateInterval = interval
        _automaticUpdateIntervalFlow.value = interval
    }

    override fun getTheme(): String = currentTheme

    override fun setTheme(theme: String) {
        this.currentTheme = theme
        _themeFlow.value = theme
    }

    override fun hasSeenPrivacyOnBoarding(): Boolean = privacyOnBoardingSeen

    override fun setPrivacyOnBoardingSeen() {
        this.privacyOnBoardingSeen = true
        _privacyOnBoardingFlow.value = true
    }

    fun reset() {
        server = defaultServer
        crashReporting = true
        automaticUpdateInterval = 10L
        currentTheme = "system"
        privacyOnBoardingSeen = true

        _serverFlow.value = defaultServer
        _crashReportingFlow.value = true
        _automaticUpdateIntervalFlow.value = 10L
        _themeFlow.value = "system"
        _privacyOnBoardingFlow.value = true
    }
}
