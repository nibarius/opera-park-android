package se.barsk.park

import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import se.barsk.park.datatypes.OwnCar

@RunWith(AndroidJUnit4::class)
class DeepLinkInstrumentedTest {
    @Test
    fun createDeepLinkOnlyServerTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F"
        val deepLink = DeepLink(Uri.parse(url))
        assertEquals(true, deepLink.isValid)
        assertEquals("http://park.example.com/", deepLink.server)
        assertEquals(0, deepLink.cars.size)
    }

    @Test
    fun createDeepLinkOneCarTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=user"
        val deepLink = DeepLink(Uri.parse(url))
        assertEquals(true, deepLink.isValid)
        assertEquals("http://park.example.com/", deepLink.server)
        assertEquals(1, deepLink.cars.size)
        assertEquals("ABC 123", deepLink.cars[0].regNo)
        assertEquals("user", deepLink.cars[0].owner)
    }

    @Test
    fun createDeepLinkTwoCarsTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=owner&r=DEF%20456&o=owner2"
        val deepLink = DeepLink(Uri.parse(url))

        testDeepLinkWithTwoCars(deepLink)
        assertEquals("http://park.example.com/", deepLink.server)
    }

    @Test
    fun getDynamicLinkForTest() {
        ParkApp.init(InstrumentationRegistry.getTargetContext())
        val cars = listOf(OwnCar("ABC 123", "owner"), OwnCar("DEF 456", "owner2"))
        val dynamicLinkUri = Uri.parse(DeepLink.getDynamicLinkFor(cars))
        assertEquals("share", dynamicLinkUri.getQueryParameter("utm_campaign"))
        assertEquals("in-app", dynamicLinkUri.getQueryParameter("utm_source"))
        assertEquals("qgy49.app.goo.gl", dynamicLinkUri.authority)
        val deepLinkUri = Uri.parse(dynamicLinkUri.getQueryParameter("link"))
        assertEquals("opera-park.appspot.com", deepLinkUri.authority)
        val deepLink = DeepLink(deepLinkUri)
        testDeepLinkWithTwoCars(deepLink)
    }

    private fun testDeepLinkWithTwoCars(deepLink: DeepLink) {
        assertEquals(true, deepLink.isValid)
        assertEquals(2, deepLink.cars.size)
        assertEquals("ABC 123", deepLink.cars[0].regNo)
        assertEquals("owner", deepLink.cars[0].owner)
        assertEquals("DEF 456", deepLink.cars[1].regNo)
        assertEquals("owner2", deepLink.cars[1].owner)
    }
}