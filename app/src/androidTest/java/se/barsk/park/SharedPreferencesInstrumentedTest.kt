package se.barsk.park

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
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
}