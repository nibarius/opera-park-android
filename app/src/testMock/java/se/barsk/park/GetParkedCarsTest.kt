package se.barsk.park

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.network.NetworkManager

/**
 * Tests for getting parked cars
 */
class GetParkedCarsTest {
    private val noParked = """
{
  "free": 6,
  "used": []
}"""

    private val oneParked = """
{
  "free": 5,
  "used": [{
    "regno": "AAA111",
    "start": "2017-06-18 15:44:09",
    "user": "user1"
  }]
}"""

    private val allParked = """
{
  "free": 0,
  "used": [{
    "regno": "AAA111",
    "start": "2017-06-18 15:44:00",
    "user": "user1"
  },{
    "regno": "BBB222",
    "start": "2017-06-18 15:44:10",
    "user": "user2"
  },{
    "regno": "CCC333",
    "start": "2017-06-18 15:44:20",
    "user": "user3"
  },{
    "regno": "DDD444",
    "start": "2017-06-18 15:44:30",
    "user": "user4"
  },{
    "regno": "EEE555",
    "start": "2017-06-18 15:44:40",
    "user": "user5"
  },{
    "regno": "FFF666",
    "start": "2017-06-18 15:44:50",
    "user": "user6"
  }]
}"""

    @Test
    fun noParkedCars() {
        val json = JSONObject(noParked)
        val networkManager = MockNetworkManager()
        val parkedCars = networkManager.getParkedCarsFromResponse(json)
        assertEquals(listOf<ParkedCar>(), parkedCars)
    }

    @Test
    fun oneParkedCar() {
        val json = JSONObject(oneParked)
        val networkManager = MockNetworkManager()
        val parkedCars = networkManager.getParkedCarsFromResponse(json)
        val expected = mutableListOf<ParkedCar>()
        expected.add(ParkedCar("AAA111", "user1", "2017-06-18 15:44:09"))
        assertEquals(expected, parkedCars)
    }

    @Test
    fun allParked() {
        val json = JSONObject(allParked)
        val networkManager = MockNetworkManager() //todo: testing network manager instead?
        val parkedCars = networkManager.getParkedCarsFromResponse(json)
        val expected = mutableListOf<ParkedCar>()
        expected.add(ParkedCar("AAA111", "user1", "2017-06-18 15:44:00"))
        expected.add(ParkedCar("BBB222", "user2", "2017-06-18 15:44:10"))
        expected.add(ParkedCar("CCC333", "user3", "2017-06-18 15:44:20"))
        expected.add(ParkedCar("DDD444", "user4", "2017-06-18 15:44:30"))
        expected.add(ParkedCar("EEE555", "user5", "2017-06-18 15:44:40"))
        expected.add(ParkedCar("FFF666", "user6", "2017-06-18 15:44:50"))
        assertEquals(expected, parkedCars)
    }
}