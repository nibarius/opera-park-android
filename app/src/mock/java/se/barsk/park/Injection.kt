package se.barsk.park

import android.content.Context
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.storage.MockStorageManager

// The Injection object in the mock flavor might not need to use all parameters required
// by the production flavor
@Suppress("UNUSED_PARAMETER")
object Injection {
    fun provideNetworkManager() = MockNetworkManager()
    fun provideCarCollection() = MockCarCollection()
    fun provideStorageManager(context: Context) = MockStorageManager(context)
    fun provideSharedPreferencesFileName(context: Context) = "mock_prefs"
}
