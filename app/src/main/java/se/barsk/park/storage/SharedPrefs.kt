package se.barsk.park.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.Utils
import se.barsk.park.utils.TimeUtils

/**
 * Class for handing all interactions with the SharedPreferences
 */
class SharedPrefs(private val context: Context, private val sharedPreferences: SharedPreferences) {
    private val firstVersionCode = context.getString(R.string.key_first_version_code)
    private val firstVersionName = context.getString(R.string.key_first_version_name)
    private val currentVersionCode = context.getString(R.string.key_current_version_code)
    private val previousVersionCode = context.getString(R.string.key_previous_version_code)
    private val serverUrl = context.getString(R.string.key_park_server_url)
    private val usageStatistics = context.getString(R.string.key_usage_statistics)
    private val crashReporting = context.getString(R.string.key_crash_reporting)
    private val defaultUsageStatistics = context.getString(R.string.default_usage_statistics)!!.toBoolean()
    private val refreshInterval = context.getString(R.string.key_refresh_interval)
    private val onWaitList = context.getString(R.string.key_on_wait_list)
    private val defaultOnWaitList = context.getString(R.string.default_on_wait_list)!!.toBoolean()
    private val privacyOnBoarding = context.getString(R.string.key_privacy_on_boarding)
    private val defaultPrivacyOnBoarding = context.getString(R.string.default_privacy_on_boarding)!!.toBoolean()


    private val internalServerChangeListener = InternalServerChangeListener()

    init {
        putSettingIfNotExist(firstVersionCode, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(currentVersionCode, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(firstVersionName, BuildConfig.VERSION_NAME)
        upgradeIfNeeded()
        sharedPreferences.registerOnSharedPreferenceChangeListener(internalServerChangeListener)
    }

    private fun readSetting(key: String): String = sharedPreferences.getString(key, context.getString(R.string.the_empty_string))
    private fun putSetting(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun putSetting(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun putSettingIfNotExist(key: String, value: String) {
        if (!sharedPreferences.contains(key)) {
            putSetting(key, value)
        }
    }

    private fun upgradeIfNeeded() {
        val storedVersionCode = readSetting(currentVersionCode)
        val currentVersionCode = BuildConfig.VERSION_CODE.toString()
        if (storedVersionCode != currentVersionCode) {
            val editor = sharedPreferences.edit()
            editor.putString(previousVersionCode, storedVersionCode)
            editor.putString(this.currentVersionCode, currentVersionCode)
            editor.apply()

            upgradePast5(storedVersionCode.toInt(), currentVersionCode.toInt())
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
            giveAllStatsConsent(false)
        }
    }

    fun hasServer() = getServer().isNotEmpty()
    // Need to fix url when reading from storage, rather than writing to storage
    // since the settings view write to settings directly without a chance to modify the value before.
    fun getServer() = Utils.fixUrl(readSetting(serverUrl))

    fun setServer(server: String) = putSetting(serverUrl, server)
    fun statsEnabled(): Boolean = sharedPreferences.getBoolean(usageStatistics, defaultUsageStatistics)
    fun crashReportingEnabled(): Boolean = sharedPreferences.getBoolean(crashReporting, defaultUsageStatistics)
    fun getAutomaticUpdateInterval(): Long = sharedPreferences.getString(refreshInterval,
            context.getString(R.string.default_refresh_interval)).toLong()

    fun onWaitList(): Boolean = sharedPreferences.getBoolean(onWaitList, defaultOnWaitList)
    fun setOnWaitList(value: Boolean) = putSetting(onWaitList, value)
    fun hasSeenPrivacyOnBoarding(): Boolean = sharedPreferences.getBoolean(privacyOnBoarding, defaultPrivacyOnBoarding)
    fun setPrivacyOnBoardingSeen() = putSetting(privacyOnBoarding, true)

    fun giveAllStatsConsent(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(usageStatistics, value)
        editor.putBoolean(crashReporting, value)
        recordCrashReportingConsentChange(value, editor)
        recordUsageStatisticsConsentChange(value, editor)
        editor.apply()
    }

    /**
     * Record that the user have given / withdrawn their consent for sending crash reports.
     * @param value The new consent value, if null the value is read from SharedPreferences.
     * @param editor The editor to use to update the data with (without calling apply),
     * if null a new editor will be created and apply will be called after the update.
     */
    fun recordCrashReportingConsentChange(value: Boolean? = null, editor: SharedPreferences.Editor? = null) {
        recordConsentChange(value ?: crashReportingEnabled(),
                context.getString(R.string.key_crash_reporting_consent),
                context.getString(R.string.key_crash_reporting_consent_timestamp),
                editor)
    }

    /**
     * Record that the user have given / withdrawn their consent for sending usage statistics.
     * @param value The new consent value, if null the value is read from SharedPreferences.
     * @param editor The editor to use to update the data with (without calling apply),
     * if null a new editor will be created and apply will be called after the update.
     */
    fun recordUsageStatisticsConsentChange(value: Boolean? = null, editor: SharedPreferences.Editor? = null) {
        recordConsentChange(value ?: statsEnabled(),
                context.getString(R.string.key_usage_stats_consent),
                context.getString(R.string.key_usage_stats_consent_timestamp),
                editor)
    }

    @SuppressLint("CommitPrefEdits")
    private fun recordConsentChange(value: Boolean, consentKey: String, timestampKey: String,
                                    editor: SharedPreferences.Editor?) {
        val e = editor ?: sharedPreferences.edit()
        e.putBoolean(consentKey, value)
        e.putLong(timestampKey, TimeUtils.now().millis)
        if (editor == null) {
            e.apply()
        }
    }


    /**
     * Listener used to be notified about changes done to the server via the settings activity
     */
    inner class InternalServerChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
            if (key == serverUrl) {
                ParkApp.networkManager.serverChanged()
            }
        }
    }
}