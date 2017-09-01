package se.barsk.park

import org.junit.Assert.assertEquals
import org.junit.Test


class UtilsTest {
    @Test
    fun testFixUrl() {
        assertEquals("", Utils.fixUrl(""))
        assertEquals("http://park.example.com/", Utils.fixUrl("http://park.example.com/"))
        assertEquals("http://park.example.com/", Utils.fixUrl("http://park.example.com"))
        assertEquals("http://park.example.com/", Utils.fixUrl("park.example.com"))
        assertEquals("https://park.example.com/", Utils.fixUrl("https://park.example.com"))
    }
}