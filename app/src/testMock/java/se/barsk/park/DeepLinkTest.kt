package se.barsk.park

import android.net.Uri
import com.google.firebase.FirebaseApp
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import se.barsk.park.analytics.AnalyticsEvent
import se.barsk.park.analytics.DynamicLinkFailedEvent
import se.barsk.park.datatypes.OwnCar


class DeepLinkTest : RobolectricTest() {
    @Test
    fun createDeepLinkOnlyServerTest() {
        val url =
            "https://opera-park.appspot.com/app/dl?park_server=http%3A%2F%2Fpark.example.com%2F"
        val deepLink = DeepLink(Uri.parse(url))
        deepLink.isValid.shouldBeTrue()
        deepLink.server shouldBeEqualTo "http://park.example.com/"
        deepLink.cars.size shouldBeEqualTo 0
    }

    @Test
    fun createDeepLinkOneCarTest() {
        val url =
            "https://opera-park.appspot.com/app/dl?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=user"
        val deepLink = DeepLink(Uri.parse(url))
        deepLink.isValid.shouldBeTrue()
        deepLink.server shouldBeEqualTo "http://park.example.com/"
        deepLink.cars.size shouldBeEqualTo 1
        deepLink.cars[0].regNo shouldBeEqualTo "ABC 123"
        deepLink.cars[0].owner shouldBeEqualTo "user"
    }

    @Test
    fun createDeepLinkTwoCarsTest() {
        val url =
            "https://opera-park.appspot.com/app/dl?park_server=http%3A%2F%2Fpark.example.com%2F&r=ABC%20123&o=owner&r=DEF%20456&o=owner2"
        val deepLink = DeepLink(Uri.parse(url))

        testDeepLinkWithTwoCars(deepLink)
        deepLink.server shouldBeEqualTo "http://park.example.com/"
    }

    @Test
    fun getDynamicLinkForTest() {
        FirebaseApp.initializeApp(context())
        val cars = listOf(OwnCar("ABC 123", "owner"), OwnCar("DEF 456", "owner2"))
        val dynamicLinkUri = Uri.parse(DeepLink.getDynamicLinkFor(cars, "http://park.example.com"))
        dynamicLinkUri.getQueryParameter("utm_campaign") shouldBeEqualTo "share"
        dynamicLinkUri.getQueryParameter("utm_source") shouldBeEqualTo "in-app"
        dynamicLinkUri.authority shouldBeEqualTo "qgy49.app.goo.gl"
        val deepLinkUri = Uri.parse(dynamicLinkUri.getQueryParameter("link"))
        deepLinkUri.authority shouldBeEqualTo "opera-park.appspot.com"
        val deepLink = DeepLink(deepLinkUri)
        testDeepLinkWithTwoCars(deepLink)
    }

    private fun testDeepLinkWithTwoCars(deepLink: DeepLink) {
        deepLink.isValid.shouldBeTrue()
        deepLink.cars.size shouldBeEqualTo 2
        deepLink.cars[0].regNo shouldBeEqualTo "ABC 123"
        deepLink.cars[0].owner shouldBeEqualTo "owner"
        deepLink.cars[1].regNo shouldBeEqualTo "DEF 456"
        deepLink.cars[1].owner shouldBeEqualTo "owner2"
    }

    @Test
    fun dynamicLinkFailedEventTest() {
        val shortException = "short exception"
        val longException =
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
        val event = DynamicLinkFailedEvent(shortException)
        event.parameters[AnalyticsEvent.Param.EXCEPTION] shouldBeEqualTo shortException
        val event2 = DynamicLinkFailedEvent(longException)
        event2.parameters[AnalyticsEvent.Param.EXCEPTION] shouldBeEqualTo longException.substring(
            0,
            100
        )
    }
}