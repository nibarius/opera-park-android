package se.barsk.park.analytics

/**
 * Event for parking related actions.
 */
class ParkActionEvent(action: Action) : AnalyticsEvent() {
    sealed class Action(val action: String) {
        class Park : Action("park")
        class Unpark : Action("unpark")
        class Wait : Action("wait")
        class StopWaiting : Action("stop_waiting")
    }
    override val name: String = "park_action"

    init {
        parameters.putString(AnalyticsEvent.Param.ACTION, action.action)
    }
}