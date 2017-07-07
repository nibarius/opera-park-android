package se.barsk.park

/**
 * Tests for parking cars
 */
class ParkCarTest {
    val parkCarResponse = """
{
  "free": 5,
  "result": "ok",
  "used": [{
    "regno": "ZZZ999",
    "start": "2017-06-27 02:38:23",
    "user": "parker"
  }]
}"""

    /*@Test
    fun parkCar() {
        val success : Boolean = NetworkManager.parkCar(OwnCar("ZZZ999", "parker"))
        assertEquals(true, success)
    }*/

}