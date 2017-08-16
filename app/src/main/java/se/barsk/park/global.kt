package se.barsk.park

// Utility function on the global scope.


/**
 * Consume function for the menu that consumes the item selected event by
 * running the given function and returning true
 */
inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}