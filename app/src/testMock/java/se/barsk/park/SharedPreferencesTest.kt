package se.barsk.park

import android.content.Context
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import se.barsk.park.storage.SharedPrefs
import java.lang.reflect.Modifier


class SharedPreferencesTest : RobolectricTest() {
    /**
     * Test that there are no duplicate keys in SharedPrefs.keys since that would mean
     * different things are written to the same key in SharedPreferences.
     */
    @Test
    fun uniqueKeysTest() {
        val context = context()
        val keys: MutableList<String> = mutableListOf()
        // based on https://stackoverflow.com/a/17607449/1730966
        R.string::class.java.declaredFields
                .filter { Modifier.isStatic(it.modifiers) && !Modifier.isPrivate(it.modifiers) && it.type == Int::class.javaPrimitiveType }
                .forEach {
                    try {
                        if (it.name.startsWith("key_")) {
                            val keyName = context.getString(it.getInt(null))
                            Assert.assertEquals("Multiple instances of key '$keyName'", false, keys.contains(keyName))
                            keys.add(keyName)
                        }
                    } catch (e: IllegalArgumentException) {
                        // ignore
                    } catch (e: IllegalAccessException) {
                        // ignore
                    }
                }
    }

    private val prefsFile = "test_prefs_file"

    @Before
    fun clearSharedPrefs() {
        context().getSharedPreferences(prefsFile, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /**
     * Test that first and current version code/name is written to shared preferences
     * on first install
     */
    @Test
    fun sharedPrefsFirstInstallTest() {
        val context = context()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_first_version_code)))
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_first_version_name)))
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_current_version_code)))
        SharedPrefs(context, sharedPreferences)
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_first_version_code), ""))
        Assert.assertEquals(BuildConfig.VERSION_NAME, sharedPreferences.getString(context.getString(R.string.key_first_version_name), ""))
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_current_version_code), ""))
    }


    /**
     * Test that first and current version code/name is updated properly on upgrade
     */
    @Test
    fun sharedPrefsUpgradeTest() {
        val context = context()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.key_first_version_code), "0")
        editor.putString(context.getString(R.string.key_first_version_name), "0.1")
        editor.putString(context.getString(R.string.key_current_version_code), "0")
        editor.apply()
        SharedPrefs(context, sharedPreferences)
        Assert.assertEquals("0", sharedPreferences.getString(context.getString(R.string.key_first_version_code), ""))
        Assert.assertEquals("0.1", sharedPreferences.getString(context.getString(R.string.key_first_version_name), ""))
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_current_version_code), ""))
    }

    @Test
    fun upgradePast5Test() {
        if (BuildConfig.VERSION_CODE < 6) {
            // This test can only be run when current version is 6 or higher.
            return
        }
        val context = context()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.key_first_version_code), "0")
        editor.putString(context.getString(R.string.key_first_version_name), "0.1")
        editor.putString(context.getString(R.string.key_current_version_code), "0")
        editor.putBoolean(context.getString(R.string.key_usage_statistics), true)
        editor.putBoolean(context.getString(R.string.key_crash_reporting), true)
        editor.apply()
        SharedPrefs(context, sharedPreferences)
        Assert.assertEquals(false, sharedPreferences.getBoolean(context.getString(R.string.key_usage_statistics), false))
        Assert.assertEquals(false, sharedPreferences.getBoolean(context.getString(R.string.key_crash_reporting), false))
    }

    /**
     * Test that the expected default values are reported on clean install before the user
     * have changed anything.
     */
    @Test
    fun defaultsTest() {
        val context = context()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val sharedPrefs = SharedPrefs(context, sharedPreferences)
        Assert.assertEquals(context.getString(R.string.default_usage_statistics)!!.toBoolean(), sharedPrefs.crashReportingEnabled())
        Assert.assertEquals(context.getString(R.string.default_usage_statistics)!!.toBoolean(), sharedPrefs.statsEnabled())
        Assert.assertEquals(context.getString(R.string.default_refresh_interval).toLong(), sharedPrefs.getAutomaticUpdateInterval())
    }
}