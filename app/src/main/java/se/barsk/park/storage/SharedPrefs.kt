package se.barsk.park.storage

import android.content.SharedPreferences
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.Utils
import se.barsk.park.network.NetworkManager

/**
 * Class for handing all interactions with the SharedPreferences
 */
class SharedPrefs(private val sharedPreferences: SharedPreferences) {
    object keys { //Todo: move these keys to a resource file?
        const val FIRST_VERSION_CODE = "fvc"
        const val FIRST_VERSION_NAME = "fvn"
        const val CURRENT_VERSION_CODE = "cvc"
        const val PREVIOUS_VERSION_CODE = "pvc"
        const val SERVER_URL = "park_server_url"
    }

    private val internalServerChangeListener = InternalServerChangeListener()

    init {
        putSettingIfNotExist(keys.FIRST_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(keys.CURRENT_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(keys.FIRST_VERSION_NAME, BuildConfig.VERSION_NAME)
        upgradeIfNeeded()
        sharedPreferences.registerOnSharedPreferenceChangeListener(internalServerChangeListener)
    }

    private fun readBoolean(key: String): Boolean = sharedPreferences.getBoolean(key, false)
    private fun readSetting(key: String): String = sharedPreferences.getString(key, "")
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
        val storedVersionCode = readSetting(keys.CURRENT_VERSION_CODE)
        val currentVersionCode = BuildConfig.VERSION_CODE.toString()
        if (storedVersionCode != currentVersionCode) {
            val editor = sharedPreferences.edit()
            editor.putString(keys.PREVIOUS_VERSION_CODE, storedVersionCode)
            editor.putString(keys.CURRENT_VERSION_CODE, currentVersionCode)
            editor.apply()
        }
    }

    fun hasServer() = getServer().isNotEmpty()
    // Need to fix url when reading from storage, rather than writing to storage
    // since the settings view write to settings directly without a chance to modify the value before.
    fun getServer() = Utils.fixUrl(readSetting(keys.SERVER_URL))

    fun setServer(server: String) = putSetting(keys.SERVER_URL, server)
    fun statsEnabled(): Boolean = readBoolean("usage_statistics")
    fun getAutomaticUpdateInterval(): Long = sharedPreferences.getString("refresh_interval", "10").toLong()

    /**
     * Listener used to be notified about changes done to the server via the settings activity
     */
    inner class InternalServerChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
            if (key == SharedPrefs.keys.SERVER_URL) {
                NetworkManager.setServer(getServer())
            }
        }
    }
}