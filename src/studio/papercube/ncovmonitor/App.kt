package studio.papercube.ncovmonitor

import okhttp3.OkHttpClient
import studio.papercube.library.simplelogger.AsyncSimpleLogger
import studio.papercube.library.simplelogger.TimeFormatter.currentTimeDividedWithHyphens
import studio.papercube.ncovmonitor.datasources.DxySource
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import kotlin.concurrent.thread

val httpClient = OkHttpClient()
val log = AsyncSimpleLogger.inDirectory(File("./log/"))

fun main(args: Array<String>) {
    val thread = thread {
        while (true) {
            val interval = 1800
            try {
                perform()
            } catch (e: Exception) {
                log.e(e)
            }
            Thread.sleep(1000L * interval)
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
        val sources = listOf<DataSource>(DxySource())
        for (source in sources) {
            val result = source.fetchDataSource()
            println("Source: ${source.sourceName}")
            println(result.updateTime)
            println(result.overall)
            println(result.provinceData)
        }
        log.v("Successfully saved result to $fileName")
    }
}