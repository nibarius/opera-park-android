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

    @Test
    fun fixRegnoForDisplay() {
        assertEquals("", Utils.fixRegnoForDisplay(""))
        assertEquals("ABC 123", Utils.fixRegnoForDisplay("ABC 123"))
        assertEquals("ABC 123", Utils.fixRegnoForDisplay("abc 123"))
        assertEquals("ABC 123", Utils.fixRegnoForDisplay("abc123"))
        assertEquals("AB-CD 123", Utils.fixRegnoForDisplay("ab-cd 123"))
        assertEquals("AB-CD 123", Utils.fixRegnoForDisplay("ab-cd123"))
        assertEquals("AB-CD 123", Utils.fixRegnoForDisplay("ab-cD123"))
    }
}