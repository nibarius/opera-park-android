package se.barsk.park

import org.junit.Assert
import org.junit.Test
import se.barsk.park.storage.SharedPrefs
import kotlin.reflect.full.memberProperties


class SharedPrefsTest {
    /**
     * Test that there are no duplicate keys in SharedPrefs.keys since that would mean
     * different things are written to the same key in SharedPreferences.
     */
    @Test
    fun uniqueKeysTest() {
        val keys: MutableList<String> = mutableListOf()
        for (key in SharedPrefs.keys.javaClass.kotlin.memberProperties) {
            val keyName = key.getter.call() as String
            Assert.assertEquals("Multiple instances of key '$keyName'", false, keys.contains(keyName))
            keys.add(keyName)
        }
    }
}