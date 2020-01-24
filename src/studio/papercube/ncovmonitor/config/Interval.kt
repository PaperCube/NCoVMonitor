package studio.papercube.ncovmonitor.config

import studio.papercube.ncovmonitor.sharedExecutor
import java.util.concurrent.Callable
import java.util.concurrent.Future

object Interval : ConfigItem("interval") {
    fun query(): Int? {
        return getOrNull()?.toIntOrNull()
    }

    fun queryAsync(): Future<Int> {
        return sharedExecutor.submit(Callable { query() ?: 1800 })
    }
}