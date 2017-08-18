package se.barsk.park

import org.junit.Assert.assertEquals
import org.junit.Test
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

class CarCollectionTest {
    val fullGarage = Garage(listOf(
            ParkedCar("zzz999", "owner", "1 hour ago"),
            ParkedCar("yyy888", "owner2", "1 hour ago"),
            ParkedCar("xxx777", "owner3", "1 hour ago"),
            ParkedCar("www666", "owner4", "1 hour ago"),
            ParkedCar("vvv555", "owner5", "1 hour ago"),
            ParkedCar("uuu444", "owner6", "1 hour ago")))

    @Test
    fun testTestDetection() {
        assertEquals(true, isTesting())
    }

    @Test
    fun testEmptyCarCollection() {
        CarCollection.replaceContent(mutableListOf())
        assertEquals(true, CarCollection.getCars().isEmpty())
    }

    @Test
    fun testUpdateStatusEmptyCollection() {
        CarCollection.replaceContent(mutableListOf())
        assertEquals(false, CarCollection.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusNoParkedCar() {
        CarCollection.replaceContent(mutableListOf(OwnCar("abc123", "me")))
        assertEquals(false, CarCollection.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusCarIsParked() {
        CarCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        assertEquals(true, CarCollection.updateParkStatus(fullGarage))
        assertEquals(true, CarCollection.getCars()[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsStillParked() {
        CarCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        CarCollection.updateParkStatus(fullGarage)
        assertEquals(false, CarCollection.updateParkStatus(fullGarage))
        assertEquals(true, CarCollection.getCars()[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsNoLongerParked() {
        CarCollection.replaceContent(mutableListOf(OwnCar("uuu444", "me")))
        CarCollection.updateParkStatus(fullGarage)
        assertEquals(true, CarCollection.updateParkStatus(Garage()))
        assertEquals(false, CarCollection.getCars()[0].parked)
    }

    @Test
    fun testPositionOfOneCar() {
        val car = OwnCar("uuu444", "me", id = "my_uuid")
        CarCollection.replaceContent(mutableListOf(car))
        assertEquals(0, CarCollection.positionOf(car))
    }

    @Test
    fun testPositionOfFirstCar() {
        val car1 = OwnCar("uuu444", "me", id = "my_uuid")
        val car2 = OwnCar("uuu444", "me")
        CarCollection.replaceContent(mutableListOf(car1, car2))
        assertEquals(0, CarCollection.positionOf(car1))
    }

    @Test
    fun testPositionOfLastCar() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("uuu444", "me", id = "my_uuid")
        CarCollection.replaceContent(mutableListOf(car1, car2))
        assertEquals(1, CarCollection.positionOf(car2))
    }

    @Test(expected = RuntimeException::class)
    fun testPositionOfDoesNotExsit() {
        CarCollection.replaceContent(mutableListOf())
        CarCollection.positionOf(OwnCar("uuu444", "me"))
    }
}