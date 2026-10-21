package se.barsk.park.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.utils.TimeUtils
import se.barsk.park.utils.Utils

/**
 * Class for handing all interactions with the SharedPreferences
 */
class SharedPrefs(private val context: Context, private val sharedPreferences: SharedPreferences) {
    private val firstVersionCode = context.getString(R.string.key_first_version_code)
    private val firstVersionName = context.getString(R.string.key_first_version_name)
    private val currentVersionCode = context.getString(R.string.key_current_version_code)
    private val previousVersionCode = context.getString(R.string.key_previous_version_code)
    private val serverUrl = context.getString(R.string.key_park_server_url)
    private val crashReporting = context.getString(R.string.key_crash_reporting)
    private val defaultCrashReporting =
        context.getString(R.string.default_crash_reporting).toBoolean()
    private val refreshInterval = context.getString(R.string.key_refresh_interval)
    private val privacyOnBoarding = context.getString(R.string.key_privacy_on_boarding)
    private val defaultPrivacyOnBoarding =
        context.getString(R.string.default_privacy_on_boarding).toBoolean()

    var settingsChangeListener: SettingsChangeListener? = null
    private val internalServerChangeListener = InternalServerChangeListener()

    init {
        putSettingIfNotExist(firstVersionCode, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(currentVersionCode, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(firstVersionName, BuildConfig.VERSION_NAME)
        upgradeIfNeeded()
        sharedPreferences.registerOnSharedPreferenceChangeListener(internalServerChangeListener)
    }

    private fun readSetting(
        key: String,
        defaultValue: String = context.getString(R.string.the_empty_string)
    ): String {
        // When given a non-null default value getString will not return null
        return sharedPreferences.getString(key, defaultValue)!!
    }

    private fun putSetting(key: String, value: String) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    private fun putSetting(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    private fun putSettingIfNotExist(key: String, value: String) {
        if (!sharedPreferences.contains(key)) {
            putSetting(key, value)
        }
    }

    private fun upgradeIfNeeded() {
        val storedVersionCode = readSetting(currentVersionCode)
        val newCurrentVersionCode = BuildConfig.VERSION_CODE.toString()
        if (storedVersionCode != newCurrentVersionCode) {
            sharedPreferences.edit {
                putString(previousVersionCode, storedVersionCode)
                putString(currentVersionCode, newCurrentVersionCode)
            }

            upgradePast5(storedVersionCode.toInt(), newCurrentVersionCode.toInt())
        }
    }

    /**
     * In version 2.1 (version code 6) a new privacy on boarding dialog was introduced
     * where the user have to choose if they want Opark to collect usage statistics or not.
     * Forget the previous stats settings for users who are upgrading to 2.1 or later so
     * that nothing is reported for upgrading users before the user have really opted in.
     */
    private fun upgradePast5(previous: Int, current: Int) {
        if (previous < 6 && current >= 6) {
            setCrashReporting(false)
        }
    }

    fun hasServer() = getServer().isNotEmpty()

    // Need to fix url when reading from storage, rather than writing to storage
    // since the settings view write to settings directly without a chance to modify the value before.
    fun getServer() = Utils.fixUrl(readSetting(serverUrl))

    fun setServer(server: String) = putSetting(serverUrl, server)
    fun crashReportingEnabled(): Boolean =
        sharedPreferences.getBoolean(crashReporting, defaultCrashReporting)

    fun getAutomaticUpdateInterval(): Long = readSetting(
        refreshInterval,
        context.getString(R.string.default_refresh_interval)
    ).toLong()

    fun setAutomaticUpdateInterval(interval: Long) {
        putSetting(refreshInterval, interval.toString())
    }

    fun hasSeenPrivacyOnBoarding(): Boolean =
        sharedPreferences.getBoolean(privacyOnBoarding, defaultPrivacyOnBoarding)

    fun setPrivacyOnBoardingSeen() = putSetting(privacyOnBoarding, true)

    fun setCrashReporting(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(crashReporting, isEnabled)
            recordCrashReportingConsentChange(isEnabled, this)
        }
    }

    /**
     * Record that the user have given / withdrawn their consent for sending crash reports.
     * @param value The new consent value, if null the value is read from SharedPreferences.
     * @param editor The editor to use to update the data with (without calling apply),
     * if null a new editor will be created and apply will be called after the update.
     */
    fun recordCrashReportingConsentChange(
        value: Boolean? = null,
        editor: SharedPreferences.Editor? = null
    ) {
        recordConsentChange(
            value ?: crashReportingEnabled(),
            context.getString(R.string.key_crash_reporting_consent),
            context.getString(R.string.key_crash_reporting_consent_timestamp),
            editor
        )
    }

    @SuppressLint("CommitPrefEdits")
    private fun recordConsentChange(
        value: Boolean, consentKey: String, timestampKey: String,
        editor: SharedPreferences.Editor?
    ) {
        val e = editor ?: sharedPreferences.edit()
        e.putBoolean(consentKey, value)
        e.putLong(timestampKey, TimeUtils.now().toEpochMilli())
        if (editor == null) {
            e.apply()
        }
    }

    fun getTheme() =
        readSetting(
            context.getString(R.string.key_theme),
            context.getString(R.string.default_theme)
        )

    fun setTheme(theme: String) = putSetting(context.getString(R.string.key_theme), theme)

    /**
     * Listener used to be notified about changes done to the server via the settings activity
     */
    inner class InternalServerChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (key == serverUrl) {
                settingsChangeListener?.onSettingsChanged(SettingsChangeListener.Setting.SERVER)
            }
        }
    }
}