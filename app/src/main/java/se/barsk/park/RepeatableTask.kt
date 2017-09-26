package se.barsk.park

import android.os.Handler

/**
 * A task that is run every {@code delay} seconds. When the start() method is called
 * the given task will be run after the specified delay. Then the task will be run
 * repeatedly with the given delay until stop is called.
 * @param task The task to run
 * @param delay The delay in seconds between runs. If 0 is specified the task will not
 *              be run at all.
 */
class RepeatableTask(private val task: () -> Unit, private val delay: Long) : Handler() {
    private val automaticUpdateRunnable: Runnable = Runnable{ run() }
    fun start() = reschedule()
    fun stop() = removeCallbacks(automaticUpdateRunnable)
    private fun reschedule() {
        if (delay > 0) {
            postDelayed(automaticUpdateRunnable, delay * MILLIS_IN_SECONDS)
        }
    }
    private fun run() {
        task.invoke()
        reschedule()
    }
}