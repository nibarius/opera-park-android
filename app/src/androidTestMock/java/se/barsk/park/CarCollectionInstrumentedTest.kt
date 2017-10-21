package se.barsk.park

import android.support.test.runner.AndroidJUnit4
import android.util.SparseBooleanArray
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.datatypes.OwnCar


@RunWith(AndroidJUnit4::class)
class CarCollectionInstrumentedTest {

    private val carCollection = MockCarCollection()

    @Test
    fun testRemoveFirstCar() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("vvv555", "me")
        val car3 = OwnCar("www666", "me")
        carCollection.replaceContent(mutableListOf(car1, car2, car3))
        val toDelete = SparseBooleanArray()
        toDelete.put(0, true)
        carCollection.removeCars(toDelete)
        Assert.assertEquals(2, carCollection.getCars().size)
        Assert.assertEquals(car2, carCollection.getCarAtPosition(0))
        Assert.assertEquals(car3, carCollection.getCarAtPosition(1))
    }

    @Test
    fun testRemoveLastCar() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("vvv555", "me")
        val car3 = OwnCar("www666", "me")
        carCollection.replaceContent(mutableListOf(car1, car2, car3))
        val toDelete = SparseBooleanArray()
        toDelete.put(2, true)
        carCollection.removeCars(toDelete)
        Assert.assertEquals(2, carCollection.getCars().size)
        Assert.assertEquals(car1, carCollection.getCarAtPosition(0))
        Assert.assertEquals(car2, carCollection.getCarAtPosition(1))
    }

    @Test
    fun testRemoveTwoCars() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("vvv555", "me")
        val car3 = OwnCar("www666", "me")
        carCollection.replaceContent(mutableListOf(car1, car2, car3))
        val toDelete = SparseBooleanArray()
        toDelete.put(0, true)
        toDelete.put(2, true)
        carCollection.removeCars(toDelete)
        Assert.assertEquals(1, carCollection.getCars().size)
        Assert.assertEquals(car2, carCollection.getCarAtPosition(0))
    }

    @Test
    fun testRemoveAllCars() {
        val car1 = OwnCar("uuu444", "me")
        val car2 = OwnCar("vvv555", "me")
        val car3 = OwnCar("www666", "me")
        carCollection.replaceContent(mutableListOf(car1, car2, car3))
        val toDelete = SparseBooleanArray()
        toDelete.put(0, true)
        toDelete.put(1, true)
        toDelete.put(2, true)
        carCollection.removeCars(toDelete)
        Assert.assertEquals(0, carCollection.getCars().size)
    }
}