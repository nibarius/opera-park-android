package se.barsk.park

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = RobolectricTest.ApplicationStub::class,
        sdk = [21])
abstract class RobolectricTest {
    fun context(): Context = ApplicationProvider.getApplicationContext()

    fun cacheDir(): File = context().cacheDir

    internal class ApplicationStub : Application()
}