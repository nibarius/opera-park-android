package se.barsk.park.analytics

class MockCrashReporting : CrashReporting() {
    override fun enableIfAllowed() {}
}