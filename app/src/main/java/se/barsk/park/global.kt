package se.barsk.park

import android.view.View
import android.widget.ViewSwitcher

// Utility function on the global scope.

const val INTENT_EXTRA_ADD_CAR = "addCar"
const val MILLIS_IN_SECONDS = 1000

/**
 * Consume function for the menu that consumes the item selected event by
 * running the given function and returning true
 */
inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

/**
 * Method used to see if the app is currently running as a unit test.
 */
fun isTesting(): Boolean = try {
    Class.forName("se.barsk.park.TestIndicator")
    true
} catch (e: ClassNotFoundException) {
    false
}

fun isMocking() = isTesting() || BuildConfig.isScreenshotBuild

/**
 * Takes a ViewSwitcher that contains a list and a placeholder view, the list it holds
 * and a boolean saying if the list is empty. Given this it ensures that the placeholder
 * is shown if the list is empty and that the list is shown otherwise.
 */
fun showPlaceholderIfNeeded(viewSwitcher: ViewSwitcher, listView: View?, empty: Boolean) {
    val listShown = viewSwitcher.currentView == listView
    val placeholderShown = !listShown
    if ((empty && listShown) || (!empty && placeholderShown)) {
        viewSwitcher.showNext()
    }
}