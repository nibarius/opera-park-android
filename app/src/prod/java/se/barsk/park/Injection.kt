package se.barsk.park

import android.content.Context
import se.barsk.park.analytics.CrashReporting
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.network.NetworkManager
import se.barsk.park.storage.StorageManager

object Injection {
    fun provideNetworkManager() = NetworkManager()
    fun provideCarCollection() = CarCollection()
    fun provideStorageManager(context: Context) = StorageManager(context)
    fun provideCrashReporting() = CrashReporting()
}
