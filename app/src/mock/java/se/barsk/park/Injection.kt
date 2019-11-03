package se.barsk.park

import android.content.Context
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.datatypes.MockUser
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.storage.MockStorageManager

object Injection {
    fun provideNetworkManager() = MockNetworkManager()
    fun provideCarCollection() = MockCarCollection()
    fun provideStorageManager(context: Context) = MockStorageManager(context)
    fun provideSharedPreferencesFileName(context: Context) = "mock_prefs"
    fun provideSignInHandler(context: Context, listener: SignInHandler.StatusChangedListener) =
            MockSignInHandler(context, listener)
    fun provideUser(context: Context) = MockUser(context)
}
