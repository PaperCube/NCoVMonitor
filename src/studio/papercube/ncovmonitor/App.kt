package studio.papercube.ncovmonitor

import okhttp3.OkHttpClient
import studio.papercube.library.simplelogger.AsyncSimpleLogger
import studio.papercube.library.simplelogger.TimeFormatter.currentTimeDividedWithHyphens
import studio.papercube.ncovmonitor.config.Interval
import studio.papercube.ncovmonitor.datasources.DxySource
import studio.papercube.ncovmonitor.datasources.NeteaseSource
import studio.papercube.ncovmonitor.datasources.NeteaseWrappedDxySource
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.util.concurrent.Callable
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.concurrent.thread

val httpClient = OkHttpClient()
val log = AsyncSimpleLogger.inDirectory(File("./log/"))

fun main(args: Array<String>) {
    val thread = thread {
        while (true) {
            val interval = Interval.queryAsync()
            try {
                perform()
            } catch (e: Exception) {
                log.e(e)
            }
            val intervalMillis = 1000L * interval.get()
            log.v("Interval = $intervalMillis")
            Thread.sleep(intervalMillis)
        }
    }
    thread.join()
    log.stop()
}

inline fun newResultFile(action: PrintWriter.(fileName: String) -> Unit) {
    var resultWriter: PrintWriter? = null
    try {
        val resultDir = File("./results")
        resultDir.mkdirs()
        val dateTodayString = LocalDate.now().toString()
        val fileName = "Result_${dateTodayString}_${currentTimeDividedWithHyphens}.txt"
        resultWriter = File(resultDir, fileName)
                .bufferedWriter(Charsets.UTF_8).let { PrintWriter(it) }
        resultWriter.action(fileName)
    } finally {
        try {
            resultWriter?.close()
        } catch (e: Exception) {
            throw e
        }
    }
}

fun perform() {
    newResultFile { fileName ->
        val results = Stream.of(
                DxySource(),
                NeteaseSource(),
                NeteaseWrappedDxySource()
        )
                .parallel()
                .map { Pair(it, it.fetchDataSource()) }
                .collect(Collectors.toList())
        for ((source, result) in results) {
            println("============================")
            println("Source: ${source.sourceName}")
            println(result.updateTime)
            println(result.overall)
            println(result.provinceData)
            println("\n\n")
        }
        log.v("Successfully saved result to $fileName")
    }
}