package se.barsk.park

import android.content.Context
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.network.NetworkManager
import se.barsk.park.storage.StorageManager

object Injection {
    fun provideNetworkManager() = NetworkManager()
    fun provideCarCollection() = CarCollection()
    fun provideStorageManager() = StorageManager()
    fun provideSharedPreferencesFileName(context: Context) = context.getString(R.string.shared_prefs_file_name)
}