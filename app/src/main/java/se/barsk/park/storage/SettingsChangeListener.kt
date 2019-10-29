package se.barsk.park.storage

interface SettingsChangeListener {
    enum class Setting { SERVER }

    fun onSettingsChanged(which: Setting)
}