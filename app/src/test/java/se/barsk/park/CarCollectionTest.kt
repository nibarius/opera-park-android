package se.barsk.park

import android.util.SparseBooleanArray
import org.junit.Assert.assertEquals
import org.junit.Test
import se.barsk.park.datatypes.*

class CarCollectionTest {
    private val fullGarage = Garage(listOf(
            ParkedCar("zzz999", "owner", "1 hour ago"),
            ParkedCar("yyy888", "owner2", "1 hour ago"),
            ParkedCar("xxx777", "owner3", "1 hour ago"),
            ParkedCar("www666", "owner4", "1 hour ago"),
            ParkedCar("vvv555", "owner5", "1 hour ago"),
            ParkedCar("uuu444", "owner6", "1 hour ago")))
    private val carCollection = MockCarCollection()

    @Test
    fun testTestDetection() = assertEquals(true, isTesting())

    @Test
    fun testEmptyCarCollection() {
        carCollection.replaceContent(mutableListOf())
        assertEquals(true, carCollection.getCars().isEmpty())
    }

    @Test
    fun testUpdateStatusEmptyCollection() {
        carCollection.replaceContent(mutableListOf())
        assertEquals(false, carCollection.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusNoParkedCar() {
        carCollection.replaceContent(mutableListOf(OwnCar("abc123", "me")))
        assertEquals(false, carCollection.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusCarIsParked() {
        carCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        assertEquals(true, carCollection.updateParkStatus(fullGarage))
        assertEquals(true, carCollection.getCars()[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsStillParked() {
        carCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        carCollection.updateParkStatus(fullGarage)
        assertEquals(false, carCollection.updateParkStatus(fullGarage))
        assertEquals(true, carCollection.getCars()[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsNoLongerParked() {
        carCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        carCollection.updateParkStatus(fullGarage)
        assertEquals(true, carCollection.updateParkStatus(Garage()))
        assertEquals(false, carCollection.getCars()[0].parked)
    }

    @Test
    fun testPositionOfOneCar() {
        val car = OwnCar("uuu444", "me", id = "my_uuid")
        carCollection.replaceContent(mutableListOf(car))
        assertEquals(0, carCollection.positionOf(car))
    }

    @Test
    fun testPositionOfFirstCar() {
        val car1 = OwnCar("uuu444", "me", id = "my_uuid")
        val car2 = OwnCar("uuu444", "me")
        carCollection.replaceContent(mutableListOf(car1, car2))
        assertEquals(0, carCollection.positionOf(car1))
    }

    @Test
    fun testPositionOfLastCar() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("uuu444", "me", id = "my_uuid")
        carCollection.replaceContent(mutableListOf(car1, car2))
        assertEquals(1, carCollection.positionOf(car2))
    }

    @Test(expected = RuntimeException::class)
    fun testPositionOfDoesNotExist() {
        carCollection.replaceContent(mutableListOf())
        carCollection.positionOf(OwnCar("uuu444", "me"))
    }
}