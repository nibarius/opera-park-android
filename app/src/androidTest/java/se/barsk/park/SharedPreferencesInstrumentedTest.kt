package se.barsk.park

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.barsk.park.storage.SharedPrefs
import java.lang.reflect.Modifier

@RunWith(AndroidJUnit4::class)
class SharedPreferencesInstrumentedTest {

    /**
     * Test that there are no duplicate keys in SharedPrefs.keys since that would mean
     * different things are written to the same key in SharedPreferences.
     */
    @Test
    fun uniqueKeysTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val keys: MutableList<String> = mutableListOf()
        // based on https://stackoverflow.com/a/17607449/1730966
        R.string::class.java.declaredFields
                .filter { Modifier.isStatic(it.modifiers) && !Modifier.isPrivate(it.modifiers) && it.type == Int::class.javaPrimitiveType }
                .forEach {
                    try {
                        if (it.name.startsWith("key_")) {
                            val keyName = appContext.getString(it.getInt(null))
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
        val context = InstrumentationRegistry.getTargetContext()
        context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /**
     * Test that first and current version code/name is written to shared preferences
     * on first install
     */
    @Test
    fun sharedPrefsFirstInstallTest() {
        val context = InstrumentationRegistry.getTargetContext()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_first_version_code)))
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_first_version_name)))
        Assert.assertFalse(sharedPreferences.contains(context.getString(R.string.key_current_version_code)))
        val sharedPrefs = SharedPrefs(context, sharedPreferences)
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_first_version_code), ""))
        Assert.assertEquals(BuildConfig.VERSION_NAME, sharedPreferences.getString(context.getString(R.string.key_first_version_name), ""))
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_current_version_code), ""))
    }


    /**
     * Test that first and current version code/name is updated properly on upgrade
     */
    @Test
    fun sharedPrefsUpgradeTest() {
        val context = InstrumentationRegistry.getTargetContext()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.key_first_version_code), "0")
        editor.putString(context.getString(R.string.key_first_version_name), "0.1")
        editor.putString(context.getString(R.string.key_current_version_code), "0")
        editor.apply()
        val sharedPrefs = SharedPrefs(context, sharedPreferences)
        Assert.assertEquals("0", sharedPreferences.getString(context.getString(R.string.key_first_version_code), ""))
        Assert.assertEquals("0.1", sharedPreferences.getString(context.getString(R.string.key_first_version_name), ""))
        Assert.assertEquals(BuildConfig.VERSION_CODE.toString(), sharedPreferences.getString(context.getString(R.string.key_current_version_code), ""))
    }

    /**
     * Test that the expected default values are reported on clean install before the user
     * have changed anything.
     */
    @Test
    fun defaultsTest() {
        val context = InstrumentationRegistry.getTargetContext()
        val sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val sharedPrefs = SharedPrefs(context, sharedPreferences)
        assertEquals(context.getString(R.string.default_usage_statistics).toBoolean(), sharedPrefs.crashReportingEnabled())
        assertEquals(context.getString(R.string.default_usage_statistics).toBoolean(), sharedPrefs.statsEnabled())
        assertEquals(context.getString(R.string.default_refresh_interval).toLong(), sharedPrefs.getAutomaticUpdateInterval())
    }
}