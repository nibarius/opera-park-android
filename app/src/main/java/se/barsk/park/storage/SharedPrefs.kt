package se.barsk.park.storage

import android.content.SharedPreferences
import se.barsk.park.BuildConfig
import se.barsk.park.network.NetworkManager

/**
 * Class for handing all interactions with the SharedPreferences
 */
class SharedPrefs(val sharedPreferences: SharedPreferences) {
    object keys {
        const val FIRST_VERSION_CODE = "fvc"
        const val FIRST_VERSION_NAME = "fvn"
        const val CURRENT_VERSION_CODE = "cvc"
        const val PREVIOUS_VERSION_CODE = "pvc"
        const val SERVER_URL = "park_server_url"
    }

    init {
        putSettingIfNotExist(keys.FIRST_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(keys.CURRENT_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        putSettingIfNotExist(keys.FIRST_VERSION_NAME, BuildConfig.VERSION_NAME)
        upgradeIfNeeded()
    }

    fun readSetting(key: String): String = sharedPreferences.getString(key, "")
    fun putSetting(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun putSettingIfNotExist(key: String, value: String) {
        if (!sharedPreferences.contains(key)) {
            putSetting(key, value)
        }
    }

    fun upgradeIfNeeded() {
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
    fun getServer() = readSetting(keys.SERVER_URL)
    fun setServer(server: String) {
        putSetting(keys.SERVER_URL, server)
        NetworkManager.serverUrl = server
    }
}