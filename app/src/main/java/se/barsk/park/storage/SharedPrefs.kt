package se.barsk.park.storage

import android.content.Context
import android.content.SharedPreferences
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.Utils
import se.barsk.park.network.NetworkManager

/**
 * Class for handing all interactions with the SharedPreferences
 */
class SharedPrefs(private val context: Context, private val sharedPreferences: SharedPreferences) {
    private val FIRST_VERSION_CODE = context.getString(R.string.key_first_version_code)
    private val FIRST_VERSION_NAME = context.getString(R.string.key_first_version_name)
    private val CURRENT_VERSION_CODE = context.getString(R.string.key_current_version_code)
    private val PREVIOUS_VERSION_CODE = context.getString(R.string.key_previous_version_code)
    private val SERVER_URL = context.getString(R.string.key_park_server_url)
    private val USAGE_STATISTICS = context.getString(R.string.key_usage_statistics)
    private val DEFAULT_USAGE_STATISTICS = context.getString(R.string.default_usage_statistics).toBoolean()
    private val REFRESH_INTERVAL = context.getString(R.string.key_refresh_interval)


    private val internalServerChangeListener = InternalServerChangeListener()

    init {
        putSettingIfNotExist(FIRST_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(CURRENT_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(FIRST_VERSION_NAME, BuildConfig.VERSION_NAME)
        upgradeIfNeeded()
        sharedPreferences.registerOnSharedPreferenceChangeListener(internalServerChangeListener)
    }

    private fun readSetting(key: String): String = sharedPreferences.getString(key, context.getString(R.string.the_empty_string))
    private fun putSetting(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun putSettingIfNotExist(key: String, value: String) {
        if (!sharedPreferences.contains(key)) {
            putSetting(key, value)
        }
    }

    private fun upgradeIfNeeded() {
        val storedVersionCode = readSetting(CURRENT_VERSION_CODE)
        val currentVersionCode = BuildConfig.VERSION_CODE.toString()
        if (storedVersionCode != currentVersionCode) {
            val editor = sharedPreferences.edit()
            editor.putString(PREVIOUS_VERSION_CODE, storedVersionCode)
            editor.putString(CURRENT_VERSION_CODE, currentVersionCode)
            editor.apply()
        }
    }

    fun hasServer() = getServer().isNotEmpty()
    // Need to fix url when reading from storage, rather than writing to storage
    // since the settings view write to settings directly without a chance to modify the value before.
    fun getServer() = Utils.fixUrl(readSetting(SERVER_URL))

    fun setServer(server: String) = putSetting(SERVER_URL, server)
    fun statsEnabled(): Boolean = sharedPreferences.getBoolean(USAGE_STATISTICS, DEFAULT_USAGE_STATISTICS)
    fun getAutomaticUpdateInterval(): Long = sharedPreferences.getString(REFRESH_INTERVAL,
            context.getString(R.string.default_refresh_interval)).toLong()

    /**
     * Listener used to be notified about changes done to the server via the settings activity
     */
    inner class InternalServerChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
            if (key == SERVER_URL) {
                NetworkManager.setServer(getServer())
            }
        }
    }
}