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

fun perform(){
    val resultDir = File("./results")
    resultDir.mkdirs()
    val dateTodayString = LocalDate.now().toString()
    val fileName = "Result_${dateTodayString}_${currentTimeDividedWithHyphens}.txt"
    val resultWriter = File(resultDir, fileName)
            .bufferedWriter(Charsets.UTF_8).let { PrintWriter(it) }
    val dataSource = DxySource()
    val result = dataSource.fetchDataSource()
    resultWriter.println(result)
    resultWriter.close()
    log.v("Successfully saved result to $fileName")
}