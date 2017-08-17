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
        val cars = CarCollection(mutableListOf(OwnCar("abc123", "me")))
        assertEquals(false, cars.updateParkStatus(fullGarage))
    }

    @Test
    fun testUpdateStatusCarIsParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me")))
        assertEquals(true, cars.updateParkStatus(fullGarage))
        assertEquals(true, cars.ownCars[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsStillParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me")))
        cars.updateParkStatus(fullGarage)
        assertEquals(false, cars.updateParkStatus(fullGarage))
        assertEquals(true, cars.ownCars[0].parked)
    }

    @Test
    fun testUpdateStatusCarIsNoLongerParked() {
        val cars = CarCollection(mutableListOf(OwnCar("uuu444", "me")))
        cars.updateParkStatus(fullGarage)
        assertEquals(true, cars.updateParkStatus(Garage()))
        assertEquals(false, cars.ownCars[0].parked)
    }

    @Test
    fun testPositionOfOneCar() {
        val car = OwnCar("uuu444", "me", id = "my_uuid")
        val cars = CarCollection(mutableListOf(car))
        assertEquals(0, cars.positionOf(car))
    }

    @Test
    fun testPositionOfFirstCar() {
        val car1 = OwnCar("uuu444", "me", id = "my_uuid")
        val car2 = OwnCar("uuu444", "me")
        val cars = CarCollection(mutableListOf(car1, car2))
        assertEquals(0, cars.positionOf(car1))
    }

    @Test
    fun testPositionOfLastCar() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("uuu444", "me", id = "my_uuid")
        val cars = CarCollection(mutableListOf(car1, car2))
        assertEquals(1, cars.positionOf(car2))
    }

    @Test(expected = RuntimeException::class)
    fun testPositionOfDoesNotExsit() {
        val cars = CarCollection()
        cars.positionOf(OwnCar("uuu444", "me"))
    }


}