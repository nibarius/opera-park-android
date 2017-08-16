package se.barsk.park

import org.junit.Assert.assertEquals
import org.junit.Test
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

/**
 * Tests for the garage
 */
class GarageTest {
    val emptyGarage = Garage()
    val almostEmptyGarage = Garage(listOf(ParkedCar("ijk567", "owner", "today")))
    val almostFullGarage = Garage(listOf(
            ParkedCar("zzz999", "owner", "1 hour ago"),
            ParkedCar("yyy888", "owner2", "1 hour ago"),
            ParkedCar("xxx777", "owner3", "1 hour ago"),
            ParkedCar("www666", "owner4", "1 hour ago"),
            ParkedCar("uuu444", "owner6", "1 hour ago")))
    val fullGarage = Garage(listOf(
            ParkedCar("zzz999", "owner", "1 hour ago"),
            ParkedCar("yyy888", "owner2", "1 hour ago"),
            ParkedCar("xxx777", "owner3", "1 hour ago"),
            ParkedCar("www666", "owner4", "1 hour ago"),
            ParkedCar("vvv555", "owner5", "1 hour ago"),
            ParkedCar("uuu444", "owner6", "1 hour ago")))


    @Test
    fun carNotParkedEmpty() {
        val car = OwnCar("AAA 111", "owner")
        val parked: Boolean = emptyGarage.isParked(car)
        assertEquals(false, parked)
    }

    @Test
    fun carNotParkedFull() {
        val car = OwnCar("AAA 111", "owner")
        val parked: Boolean = fullGarage.isParked(car)
        assertEquals(false, parked)
    }

    @Test
    fun carParkedFull() {
        val car = OwnCar("zzz999", "someone else")
        val parked: Boolean = fullGarage.isParked(car)
        assertEquals(true, parked)
    }

    @Test
    fun garageIsFull() {
        assertEquals(true, fullGarage.isFull())
    }

    @Test
    fun garageIsNotFull() {
        assertEquals(false, almostFullGarage.isFull())
        assertEquals(false, emptyGarage.isFull())
    }

    @Test
    fun allSpotsFree() {
        assertEquals(Garage.CAPACITY, emptyGarage.spotsFree)
    }

    @Test
    fun someSpotsFree() {
        assertEquals(1, almostFullGarage.spotsFree)
    }

    @Test
    fun noSpotsFree() {
        assertEquals(0, fullGarage.spotsFree)
    }
}