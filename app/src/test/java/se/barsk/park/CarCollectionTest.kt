package se.barsk.park

import org.junit.Assert.assertEquals
import org.junit.Test

class CarCollectionTest {

    val fullGarage = Garage(listOf(
            ParkedCar("zzz999", "owner", "1 hour ago"),
            ParkedCar("yyy888", "owner2", "1 hour ago"),
            ParkedCar("xxx777", "owner3", "1 hour ago"),
            ParkedCar("www666", "owner4", "1 hour ago"),
            ParkedCar("vvv555", "owner5", "1 hour ago"),
            ParkedCar("uuu444", "owner6", "1 hour ago")))

    @Test
    fun testEmptyCarCollection() {
        val cars = CarCollection()
        assertEquals(true, cars.ownCars.isEmpty())
    }

    @Test
    fun testUpdateStatusEmptyCollection() {
        val cars = CarCollection()
        assertEquals(false, cars.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusNoParkedCar() {
        val cars = CarCollection(mutableListOf(OwnCar("abc123", "me", "timestamp")))
        assertEquals(false, cars.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusCarIsParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me", "timestamp")))
        assertEquals(true, cars.updateParkStatus(fullGarage))
        assertEquals(true, cars.ownCars[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsStillParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me", "timestamp")))
        cars.updateParkStatus(fullGarage)
        assertEquals(false, cars.updateParkStatus(fullGarage))
        assertEquals(true, cars.ownCars[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsNoLongerParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me", "timestamp")))
        cars.updateParkStatus(fullGarage)
        assertEquals(true, cars.updateParkStatus(Garage()))
        assertEquals(false, cars.ownCars[0].parked)
    }

}