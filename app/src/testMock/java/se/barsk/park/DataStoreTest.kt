package se.barsk.park

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import se.barsk.park.storage.DataStoreManager
import java.io.File
import java.lang.reflect.Modifier

class DataStoreTest : RobolectricTest() {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private fun createDataStoreManager(fileName: String): DataStoreManager {
        val testFile = File(context().filesDir, fileName)
        if (testFile.exists()) {
            testFile.delete()
        }
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        return DataStoreManager(
            context = context(),
            dataStore = testDataStore,
            scope = testScope
        )
    }

    /**
     * Test that there are no duplicate keys in R.string.key_* since that would mean
     * different settings write to the same key.
     */
    @Test
    fun uniqueKeysTest() {
        val context = context()
        val keys: MutableList<String> = mutableListOf()
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

    /**
     * Test that default values are returned correctly on a clean install.
     */
    @Test
    fun defaultsTest() = runTest(testDispatcher) {
        val context = context()
        val manager = createDataStoreManager("defaults_test.preferences_pb")

        Assert.assertEquals(
            context.getString(R.string.default_crash_reporting).toBoolean(),
            manager.crashReportingEnabled()
        )
        Assert.assertEquals(
            context.getString(R.string.default_refresh_interval).toLong(),
            manager.getAutomaticUpdateInterval()
        )
        Assert.assertEquals(
            context.getString(R.string.default_theme),
            manager.getTheme()
        )
        Assert.assertEquals(
            context.getString(R.string.default_privacy_on_boarding).toBoolean(),
            manager.hasSeenPrivacyOnBoarding()
        )
    }

    /**
     * Test setting and reading server URL and theme preferences.
     */
    @Test
    fun setAndGetPreferencesTest() = runTest(testDispatcher) {
        val manager = createDataStoreManager("set_get_test.preferences_pb")

        manager.setServer("https://test.server.com/")
        Assert.assertEquals("https://test.server.com/", manager.getServer())
        Assert.assertEquals("https://test.server.com/", manager.serverFlow.first())

        manager.setTheme("dark")
        Assert.assertEquals("dark", manager.getTheme())
        Assert.assertEquals("dark", manager.themeFlow.first())

        manager.setAutomaticUpdateInterval(30)
        Assert.assertEquals(30L, manager.getAutomaticUpdateInterval())
        Assert.assertEquals(30L, manager.automaticUpdateIntervalFlow.first())

        manager.setCrashReportingEnabled(true)
        Assert.assertTrue(manager.crashReportingEnabled())
        Assert.assertTrue(manager.crashReportingFlow.first())

        manager.setPrivacyOnBoardingSeen()
        Assert.assertTrue(manager.hasSeenPrivacyOnBoarding())
        Assert.assertTrue(manager.privacyOnBoardingFlow.first())
    }
}
