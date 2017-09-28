package se.barsk.park

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.storage.Database

@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {

    private val databaseName = "test.db"
    lateinit var db: Database

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Database(context, databaseName)
    }

    @After
    fun deleteDb() {
        val context = InstrumentationRegistry.getTargetContext()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun emptyDatabaseTest() {
        assertEquals(true, db.fetchAllCars().isEmpty())
    }

    @Test
    fun insertCarTest() {
        db.insertOrReplace(OwnCar("car1", "user1"), 0)
        db.insertOrReplace(OwnCar("car2", "user2"), 1)
        db.insertOrReplace(OwnCar("car3", "user3"), 2)
        val allCars = db.fetchAllCars()
        assertEquals(3, allCars.size)
        assertEquals("car1", allCars[0].regNo)
        assertEquals("car2", allCars[1].regNo)
        assertEquals("car3", allCars[2].regNo)
    }

    @Test
    fun replaceCarTest() {
        val oldCar = OwnCar("old", "old")
        val newCar = OwnCar("new", "new", id = oldCar.id)
        db.insertOrReplace(oldCar, 0)
        db.insertOrReplace(newCar, 0)
        val allCars = db.fetchAllCars()
        assertEquals(1, allCars.size)
        assertEquals(newCar, allCars[0])
    }

    @Test
    fun removeCarTest() {
        val car1 = OwnCar("car1", "user1")
        val removedCar = OwnCar("car2", "user2")
        val car3 = OwnCar("car3", "user3")
        db.insertOrReplace(car1, 0)
        db.insertOrReplace(removedCar, 1)
        db.insertOrReplace(car3, 2)
        db.remove(removedCar)
        val allCars = db.fetchAllCars()
        assertEquals(2, allCars.size)
        assertEquals(true, allCars.contains(car1))
        assertEquals(false, allCars.contains(removedCar))
        assertEquals(car3, allCars[1])
    }
}