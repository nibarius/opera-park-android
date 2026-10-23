package se.barsk.park.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.utils.TimeUtils
import se.barsk.park.utils.Utils

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context,
                context.getString(R.string.shared_prefs_file_name)
            )
        )
    }
)

/**
 * Class for handling interactions with Preferences DataStore.
 */
class DataStoreManager(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    val firstVersionCodeKey = stringPreferencesKey(context.getString(R.string.key_first_version_code))
    val firstVersionNameKey = stringPreferencesKey(context.getString(R.string.key_first_version_name))
    val currentVersionCodeKey = stringPreferencesKey(context.getString(R.string.key_current_version_code))
    val previousVersionCodeKey = stringPreferencesKey(context.getString(R.string.key_previous_version_code))
    val serverUrlKey = stringPreferencesKey(context.getString(R.string.key_park_server_url))
    val crashReportingKey = booleanPreferencesKey(context.getString(R.string.key_crash_reporting))
    val refreshIntervalKey = stringPreferencesKey(context.getString(R.string.key_refresh_interval))
    val privacyOnBoardingKey = booleanPreferencesKey(context.getString(R.string.key_privacy_on_boarding))
    val crashReportingConsentKey = booleanPreferencesKey(context.getString(R.string.key_crash_reporting_consent))
    val crashReportingConsentTimestampKey = longPreferencesKey(context.getString(R.string.key_crash_reporting_consent_timestamp))
    val themeKey = stringPreferencesKey(context.getString(R.string.key_theme))

    private val defaultCrashReporting =
        context.getString(R.string.default_crash_reporting).toBoolean()
    private val defaultPrivacyOnBoarding =
        context.getString(R.string.default_privacy_on_boarding).toBoolean()
    private val defaultTheme =
        context.getString(R.string.default_theme)

    var settingsChangeListener: SettingsChangeListener? = null

    private val preferencesState: StateFlow<Preferences?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val serverFlow: Flow<String> = dataStore.data.map { preferences ->
        Utils.fixUrl(preferences[serverUrlKey] ?: "")
    }

    val crashReportingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[crashReportingKey] ?: defaultCrashReporting
    }

    val automaticUpdateIntervalFlow: Flow<Long> = dataStore.data.map { preferences ->
        (preferences[refreshIntervalKey] ?: context.getString(R.string.default_refresh_interval)).toLong()
    }

    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[themeKey] ?: defaultTheme
    }

    val privacyOnBoardingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[privacyOnBoardingKey] ?: defaultPrivacyOnBoarding
    }

    init {
        scope.launch {
            dataStore.edit { preferences ->
                if (preferences[firstVersionCodeKey] == null) {
                    preferences[firstVersionCodeKey] = BuildConfig.VERSION_CODE.toString()
                }
                if (preferences[currentVersionCodeKey] == null) {
                    preferences[currentVersionCodeKey] = BuildConfig.VERSION_CODE.toString()
                }
                if (preferences[firstVersionNameKey] == null) {
                    preferences[firstVersionNameKey] = BuildConfig.VERSION_NAME
                }

                val storedVersionCode =
                    preferences[currentVersionCodeKey] ?: BuildConfig.VERSION_CODE.toString()
                val newCurrentVersionCode = BuildConfig.VERSION_CODE.toString()
                if (storedVersionCode != newCurrentVersionCode) {
                    preferences[previousVersionCodeKey] = storedVersionCode
                    preferences[currentVersionCodeKey] = newCurrentVersionCode

                    val prevInt = storedVersionCode.toIntOrNull() ?: 0
                    val currInt = newCurrentVersionCode.toIntOrNull() ?: 0
                    if (prevInt < 6 && currInt >= 6) {
                        preferences[crashReportingKey] = false
                        preferences[crashReportingConsentKey] = false
                        preferences[crashReportingConsentTimestampKey] =
                            TimeUtils.now().toEpochMilli()
                    }
                }
            }
        }
    }

    private fun getPreferences(): Preferences {
        return preferencesState.value ?: runBlocking {
            try {
                dataStore.data.first()
            } catch (e: Exception) {
                emptyPreferences()
            }
        }
    }

    fun hasServer(): Boolean = getServer().isNotEmpty()

    fun getServer(): String = Utils.fixUrl(getPreferences()[serverUrlKey] ?: "")

    suspend fun setServer(server: String) {
        dataStore.edit { prefs ->
            prefs[serverUrlKey] = server
        }
        settingsChangeListener?.onSettingsChanged(SettingsChangeListener.Setting.SERVER)
    }

    fun crashReportingEnabled(): Boolean =
        getPreferences()[crashReportingKey] ?: defaultCrashReporting

    fun getAutomaticUpdateInterval(): Long =
        (getPreferences()[refreshIntervalKey]
            ?: context.getString(R.string.default_refresh_interval)).toLong()

    suspend fun setAutomaticUpdateInterval(interval: Long) {
        dataStore.edit { prefs ->
            prefs[refreshIntervalKey] = interval.toString()
        }
    }

    fun hasSeenPrivacyOnBoarding(): Boolean =
        getPreferences()[privacyOnBoardingKey] ?: defaultPrivacyOnBoarding

    suspend fun setPrivacyOnBoardingSeen() {
        dataStore.edit { prefs ->
            prefs[privacyOnBoardingKey] = true
        }
    }

    suspend fun setCrashReportingEnabled(isEnabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[crashReportingKey] = isEnabled
            prefs[crashReportingConsentKey] = isEnabled
            prefs[crashReportingConsentTimestampKey] = TimeUtils.now().toEpochMilli()
        }
    }

    fun getTheme(): String =
        getPreferences()[themeKey] ?: defaultTheme

    suspend fun setTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[themeKey] = theme
        }
    }
}
