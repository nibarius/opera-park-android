package se.barsk.park.datatypes

/**
 * Representation of a car with a registration number and an owner.
 */
abstract class Car constructor() {
    abstract val regNo: String
    abstract val owner: String
}