package se.barsk.park

import android.view.View
import android.widget.ViewSwitcher

// Utility function on the global scope.

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
 * Takes a ViewSwitcher that contains a list and a placeholder view, the list it holds
 * and a boolean saying if the list is empty. Given this it ensures that the placeholder
 * is shown if the list is empty and that the list is shown otherwise.
 */
fun showPlaceholderIfNeeded(viewSwitcher: ViewSwitcher, listView: View?, shouldShowPlaceholder: Boolean) {
    val listShown = viewSwitcher.currentView == listView
    val placeholderShown = !listShown
    if ((shouldShowPlaceholder && listShown) || (!shouldShowPlaceholder && placeholderShown)) {
        viewSwitcher.showNext()
    }
}