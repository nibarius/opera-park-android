package se.barsk.park.storage

import android.content.Context

/**
 * A mock implementation of the storage manager with a dummy server set automatically
 */
class MockStorageManager(context: Context) : StorageManager(context) {
    private var server = "https://park.example.com/"

    // Shared preferences interaction functions
    override fun hasServer() = server.isNotEmpty()

    override fun getServer() = server
    override fun setServer(server: String) {
        this.server = server
    }
}