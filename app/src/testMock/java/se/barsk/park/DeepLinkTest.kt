package se.barsk.park

import android.net.Uri
import org.junit.Assert
import org.junit.Test


class DeepLinkTest : RoboelectricTest() {
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

    private fun testDeepLinkWithTwoCars(deepLink: DeepLink) {
        Assert.assertEquals(true, deepLink.isValid)
        Assert.assertEquals(2, deepLink.cars.size)
        Assert.assertEquals("ABC 123", deepLink.cars[0].regNo)
        Assert.assertEquals("owner", deepLink.cars[0].owner)
        Assert.assertEquals("DEF 456", deepLink.cars[1].regNo)
        Assert.assertEquals("owner2", deepLink.cars[1].owner)
    }
}