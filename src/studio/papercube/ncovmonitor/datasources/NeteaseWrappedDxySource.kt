package studio.papercube.ncovmonitor.datasources

import okhttp3.Request
import okhttp3.Response
import studio.papercube.ncovmonitor.BadResponseException
import studio.papercube.ncovmonitor.DataSource
import studio.papercube.ncovmonitor.StatObject
import java.nio.charset.Charset

class NeteaseWrappedDxySource : DataSource(
        "NeteaseWrappedDxySource",
        "https://news.163.com/special/00018IRU/data_from_dxy.js"
) {
    override fun parseResponse(response: Response): StatObject {
        val reader = response.body?.byteStream()
                ?.bufferedReader(Charset.forName("GBK"))
        val string = reader?.use { it.readText() } ?: throw BadResponseException.nullBody()
        return newStatObject(
                "provinceData" to string
        )
    }
}