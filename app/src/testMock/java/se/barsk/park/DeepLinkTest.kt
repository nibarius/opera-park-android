package se.barsk.park

import android.net.Uri
import com.google.firebase.FirebaseApp
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Test
import se.barsk.park.analytics.DynamicLinkFailedEvent
import se.barsk.park.datatypes.OwnCar


class DeepLinkTest : RobolectricTest() {
    @Test
    fun createDeepLinkOnlyServerTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F"
        val deepLink = DeepLink(Uri.parse(url))
        Assert.assertEquals(true, deepLink.isValid)
        Assert.assertEquals("http://park.example.com/", deepLink.server)
        Assert.assertEquals(0, deepLink.cars.size)
    }

    @Test
    fun createDeepLinkOneCarTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=user"
        val deepLink = DeepLink(Uri.parse(url))
        Assert.assertEquals(true, deepLink.isValid)
        Assert.assertEquals("http://park.example.com/", deepLink.server)
        Assert.assertEquals(1, deepLink.cars.size)
        Assert.assertEquals("ABC 123", deepLink.cars[0].regNo)
        Assert.assertEquals("user", deepLink.cars[0].owner)
    }

    @Test
    fun createDeepLinkTwoCarsTest() {
        val url = "https://opera-park.appspot.com?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=owner&r=DEF%20456&o=owner2"
        val deepLink = DeepLink(Uri.parse(url))

        testDeepLinkWithTwoCars(deepLink)
        Assert.assertEquals("http://park.example.com/", deepLink.server)
    }

    @Test
    fun getDynamicLinkForTest() {
        FirebaseApp.initializeApp(context())
        val cars = listOf(OwnCar("ABC 123", "owner"), OwnCar("DEF 456", "owner2"))
        val dynamicLinkUri = Uri.parse(DeepLink.getDynamicLinkFor(cars, "http://park.example.com"))
        Assert.assertEquals("share", dynamicLinkUri.getQueryParameter("utm_campaign"))
        Assert.assertEquals("in-app", dynamicLinkUri.getQueryParameter("utm_source"))
        Assert.assertEquals("qgy49.app.goo.gl", dynamicLinkUri.authority)
        val deepLinkUri = Uri.parse(dynamicLinkUri.getQueryParameter("link"))
        Assert.assertEquals("opera-park.appspot.com", deepLinkUri.authority)
        val deepLink = DeepLink(deepLinkUri)
        testDeepLinkWithTwoCars(deepLink)
    }

    private fun testDeepLinkWithTwoCars(deepLink: DeepLink) {
        Assert.assertEquals(true, deepLink.isValid)
        Assert.assertEquals(2, deepLink.cars.size)
        Assert.assertEquals("ABC 123", deepLink.cars[0].regNo)
        Assert.assertEquals("owner", deepLink.cars[0].owner)
        Assert.assertEquals("DEF 456", deepLink.cars[1].regNo)
        Assert.assertEquals("owner2", deepLink.cars[1].owner)
    }

    @Test
    fun dynamicLinkFailedEventTest() {
        val shortException = "short exception"
        val longException = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
        val event = DynamicLinkFailedEvent(shortException)
        event.parameters[DynamicLinkFailedEvent.Param.EXCEPTION] shouldEqual shortException
        val event2 = DynamicLinkFailedEvent(longException)
        event2.parameters[DynamicLinkFailedEvent.Param.EXCEPTION] shouldEqual longException.substring(0, 100)
    }
}