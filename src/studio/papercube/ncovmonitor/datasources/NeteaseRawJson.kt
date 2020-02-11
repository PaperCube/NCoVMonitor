package studio.papercube.ncovmonitor.datasources

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Request
import okhttp3.Response
import studio.papercube.ncovmonitor.BadResponseException
import studio.papercube.ncovmonitor.DataSource
import studio.papercube.ncovmonitor.StatObject
import studio.papercube.ncovmonitor.StatisticItem


class NeteaseRawJson : DataSource(
        "NeteaseRawJson",
        "https://c.m.163.com/ug/api/wuhan/app/data/list-total"
) {
    override fun newRequestBuilder(): Request.Builder {
        return super.newRequestBuilder()
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    }
    override fun parseResponse(response: Response): StatObject {
        val str = response.body?.string() ?: throw BadResponseException.nullBody()
        val obj = ObjectMapper().readTree(str)
        val objChinaToday = obj["data"]["chinaTotal"]["total"]
        return newStatObject(
                "counts" to StatisticItem(
                        objChinaToday["confirm"].asInt(-1),
                        objChinaToday["suspect"].asInt(-1),
                        objChinaToday["heal"].asInt(-1),
                        objChinaToday["dead"].asInt(-1)
                ),
                "provinceData" to obj
        )
    }
}