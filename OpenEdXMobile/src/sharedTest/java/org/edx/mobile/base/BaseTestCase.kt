package org.edx.mobile.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.edx.mobile.logger.Logger
import org.edx.mobile.test.util.TimeUtilsForTests
import org.edx.mobile.util.Config
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

@Ignore
abstract class BaseTestCase : BaseTest() {

    @JvmField
    protected val logger = Logger(javaClass.name)

    protected lateinit var context: Context

    protected lateinit var config: Config

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        config = Config(generateConfigProperties())
        val injector = Injector(config)
        inject(injector)
        // Set time zone to a constant value to make time-based tests predictable
        TimeZone.setDefault(TimeUtilsForTests.DEFAULT_TIME_ZONE)
        print("Started Test Case: " + javaClass.name)
    }

    /**
     * subclass should inject the properties marked as @Inject
     */
    @Throws(Exception::class)
    protected open fun inject(injector: Injector?) {
    }

    @Throws(IOException::class)
    protected open fun generateConfigProperties(): JsonObject? {
        // Generate default config properties for subclasses to customize
        context.assets?.open("config/config.json").use { inputStream ->
            return JsonParser().parse(
                InputStreamReader(inputStream)
            ).asJsonObject
        }
    }

    @After
    @Throws(Exception::class)
    open fun tearDown() {
        print("Finished Test Case: " + javaClass.name)
    }

    protected fun print(msg: String?) {
        println(msg)
        logger.debug(msg)
    }
}
