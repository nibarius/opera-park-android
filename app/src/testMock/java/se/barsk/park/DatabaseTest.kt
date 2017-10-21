package se.barsk.park

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.storage.Database


class DatabaseTest : RoboelectricTest() {

    private val databaseName = "test.db"
    private lateinit var db: Database

    @Before
    fun createDb() {
        db = Database(context(), databaseName)
    }

    @After
    fun deleteDb() {
        context().deleteDatabase(databaseName)
    }

    @Test
    fun emptyDatabaseTest() = Assert.assertEquals(true, db.fetchAllCars().isEmpty())

    @Test
    fun insertCarTest() {
        db.insertOrReplace(OwnCar("car1", "user1"), 0)
        db.insertOrReplace(OwnCar("car2", "user2"), 1)
        db.insertOrReplace(OwnCar("car3", "user3"), 2)
        val allCars = db.fetchAllCars()
        Assert.assertEquals(3, allCars.size)
        Assert.assertEquals("car1", allCars[0].regNo)
        Assert.assertEquals("car2", allCars[1].regNo)
        Assert.assertEquals("car3", allCars[2].regNo)
    }

    @Test
    fun replaceCarTest() {
        val oldCar = OwnCar("old", "old")
        val newCar = OwnCar("new", "new", id = oldCar.id)
        db.insertOrReplace(oldCar, 0)
        db.insertOrReplace(newCar, 0)
        val allCars = db.fetchAllCars()
        Assert.assertEquals(1, allCars.size)
        Assert.assertEquals(newCar, allCars[0])
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
        Assert.assertEquals(2, allCars.size)
        Assert.assertEquals(true, allCars.contains(car1))
        Assert.assertEquals(false, allCars.contains(removedCar))
        Assert.assertEquals(car3, allCars[1])
    }

    @Test
    fun removeCarUpdatePositionTest() {
        val car1 = OwnCar("car1", "user1")
        val car2 = OwnCar("car2", "user2")
        val car3 = OwnCar("car3", "user3")
        val car4 = OwnCar("car4", "user4")
        val car5 = OwnCar("car5", "user5")
        db.insertOrReplace(car1, 0)
        db.insertOrReplace(car2, 1)
        db.insertOrReplace(car3, 2)
        db.insertOrReplace(car4, 3)
        db.remove(car2)
        db.remove(car3)
        db.insertOrReplace(car5, 2)
        val allCars = db.fetchAllCars()
        Assert.assertEquals(3, allCars.size)
        Assert.assertEquals(car1, allCars[0])
        Assert.assertEquals(car4, allCars[1])
        Assert.assertEquals(car5, allCars[2])
    }
}