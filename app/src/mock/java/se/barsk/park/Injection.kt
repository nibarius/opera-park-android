package se.barsk.park

import android.content.Context
import se.barsk.park.carcollection.MockCarCollection
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.storage.MockStorageManager

object Injection {
    fun provideNetworkManager() = MockNetworkManager()
    fun provideCarCollection() = MockCarCollection()
    fun provideStorageManager() = MockStorageManager()
    fun provideSharedPreferencesFileName(context: Context) = "mock_prefs"
}