package studio.papercube.ncovmonitor.datasources

import okhttp3.Response
import studio.papercube.ncovmonitor.BadResponseException
import studio.papercube.ncovmonitor.DataSource
import studio.papercube.ncovmonitor.StatisticItem
import studio.papercube.ncovmonitor.Statistics
import java.nio.charset.Charset

class NeteaseWrappedDxySource : DataSource(
        "NeteaseWrappedDxySource",
        "https://news.163.com/special/00018IRU/data_from_dxy.js"
) {
    override fun parseResponse(response: Response): Statistics {
        val reader = response.body?.byteStream()
                ?.bufferedReader(Charset.forName("GBK"))
        val string = reader?.use { it.readText() } ?: throw BadResponseException.nullBody()
        return Statistics(
                "--",
                StatisticItem(-1, -1, -1, -1),
                string
        )
    }
}