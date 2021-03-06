package se.barsk.park

import org.junit.Assert.assertEquals
import org.junit.Test
import se.barsk.park.utils.Utils


class UtilsTest {
    @Test
    fun testFixUrl() {
        assertEquals("", Utils.fixUrl(""))
        assertEquals("https://park.example.com/", Utils.fixUrl("https://park.example.com/"))
        assertEquals("https://park.example.com/", Utils.fixUrl("park.example.com"))
        assertEquals("https://park.example.com/", Utils.fixUrl("park.example.com/"))
        assertEquals("https://park.example.com/", Utils.fixUrl("https://park.example.com"))
    }

    @Test
    fun testFixUrlHttpToHttps() {
        assertEquals("https://park.example.com/", Utils.fixUrl("http://park.example.com/"))
        assertEquals("https://park.example.com/", Utils.fixUrl("http://park.example.com"))
    }

    @Test
    fun testFixUrlUppercase() {
        assertEquals("https://park.example.com/", Utils.fixUrl("Http://park.example.com/"))
        assertEquals("https://park.example.com/", Utils.fixUrl("Park.example.com"))
        assertEquals("https://park.example.com/", Utils.fixUrl("Https://park.example.com"))
        assertEquals("https://park.example.com/", Utils.fixUrl("Park.ExamPle.Com"))
    }

    @Test
    fun testFixUrlTrailingSpace() {
        assertEquals("https://park.example.com/", Utils.fixUrl("  https://park.example.com"))
        assertEquals("https://park.example.com/", Utils.fixUrl("park.example.com/  "))
        assertEquals("https://park.example.com/", Utils.fixUrl("  https://park.example.com/  "))
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