package se.barsk.park.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

object Utils {
    /**
     * Adds a https:// to the beginning of an url if it doesn't have any protocol
     * Adds a / to the end of an url if it doesn't have any
     * Trims any leading / trailing whitespace
     * Make the whole url lowercase
     */
    fun fixUrl(url: String): String {
        if (url.isBlank()) {
            return ""
        }

        var fixedUrl = url
            .trim()
            .lowercase(Locale.US)
                .replace("http://", "https://")
        if (!fixedUrl.startsWith("https://")) {
            fixedUrl = "https://$fixedUrl"
        }
        if (!fixedUrl.endsWith("/")) {
            fixedUrl += "/"
        }

        return fixedUrl
    }

    /**
     * Takes a licence plate string and makes it consistent and nicer for display.
     * It makes all letters uppercase and tries to insert a space between letters
     * and numbers.
     */
    fun fixRegnoForDisplay(regNo: String): String =
            regNo.uppercase(Locale.getDefault()).replace("[A-Z]\\d".toRegex()) {
                "${it.value[0]} ${it.value[1]}"
            }

    fun setTheme(value: String) {
        when {
            value == "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            value == "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            pOrBelow() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun pOrBelow(): Boolean {
        return android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P
    }

    fun isDarkTheme(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                false
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                true
            }
            else -> false
        }
    }
}