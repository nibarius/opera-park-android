package se.barsk.park

import android.content.Context
import se.barsk.park.analytics.MockCrashReporting
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.storage.MockStorageManager

@Suppress("UNUSED_PARAMETER")
object Injection {
    fun provideNetworkManager() = MockNetworkManager()
    fun provideCarCollection() = MockCarCollection()
    fun provideStorageManager(context: Context) = MockStorageManager(context)
    fun provideCrashReporting() = MockCrashReporting()
}
