package se.barsk.park

import android.app.Application
import android.content.Context
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
        application = RoboelectricTest.ApplicationStub::class,
        sdk = intArrayOf(21))
abstract class RoboelectricTest {
    fun context(): Context = RuntimeEnvironment.application

    fun cacheDir(): File = context().cacheDir

    internal class ApplicationStub : Application()
}