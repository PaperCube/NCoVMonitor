package studio.papercube.ncovmonitor.datasources

import okhttp3.Response
import studio.papercube.ncovmonitor.*
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Future

class NeteaseSource : DataSource(
        "Netease",
        "https://news.163.com/special/epidemic/"
) {
    private var futureProvinceData: Future<Response>? = null
    private fun count(str: String): StatisticItem {
        //<p>中国（含港澳台）：确诊 929 例，死亡 26 例，治愈 38 例</p>
        val regex = "<p>中国.*?</p>".toRegex()
        val info = regex.find(str)?.groupValues?.firstOrNull()
                ?: throw BadResponseException.noMatch()
        val confirmed = info.extractIntFromRegex(1, "确诊(.+?)例".toRegex())
                ?: throw BadResponseException.nullValue("confirmed")
        val cured = info.extractIntFromRegex(1, "治愈(.+?)例".toRegex()) ?: -1
        val death = info.extractIntFromRegex(1, "死亡(.+?)例".toRegex()) ?: -1
        return StatisticItem(confirmed, -1, cured, death)
    }

    private fun extractTime(str: String): String {
        //<span>截止2020/01/24 20:00</span>
        val regex = "数据统计<span>截止([\\d/:\\s]+)</span>".toRegex()
        return regex.find(str)
                ?.groupValues
                ?.getOrNull(1)
                ?: "--"
    }

    override fun fetchDataSource(): Statistics {
        val epidemicDataJsUrl = "https://news.163.com/special/00019HSN/epidemic_data.js"
        val request = newRequestBuilder()
                .url(epidemicDataJsUrl)
                .build()
        futureProvinceData = sharedExecutor.submit(Callable {
            try {
                httpClient.newCall(request).execute()
            } catch (e: Exception) {
                log.e(msg = "Failed to fetch info from $epidemicDataJsUrl", e = e)
                throw e
            }
        })
        return super.fetchDataSource()
    }

    override fun parseResponse(response: Response): Statistics {
        val string = response.body?.string()
        try {
            string ?: throw BadResponseException.nullBody()
            val counts = count(string)
            val time = extractTime(string)
            val provinceData = futureProvinceData?.get()?.body?.bytes()?.toString(Charset.forName("GBK"))
            return Statistics(
                    time,
                    counts,
                    provinceData
            )
        } catch (e: Exception) {
            log.e(msg = "Failed to parse response. Response body: $string")
            throw e
        }
    }
}