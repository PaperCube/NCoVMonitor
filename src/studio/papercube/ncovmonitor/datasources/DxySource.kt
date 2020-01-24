package studio.papercube.ncovmonitor.datasources

import okhttp3.Response
import studio.papercube.ncovmonitor.*

class DxySource : DataSource(
        "dxy",
        "https://3g.dxy.cn/newh5/view/pneumonia"
) {
    private fun count(str: String): StatisticItem {
        //language=RegExp
        val regex = "\"countRemark\":\".+?\"".toRegex()
        val match = regex.find(str)
                ?: throw BadResponseException.noMatch()
        val groupValues = match.groupValues
        val source: String? = groupValues.firstOrNull()
        val confirmed = source?.extractIntFromRegex(1, "确诊(.+?)例".toRegex())
                ?: throw BadResponseException.nullValue("confirmed")
        val suspected = source.extractIntFromRegex(1, "疑似(.+?)例".toRegex()) ?: -1
        val cured = source.extractIntFromRegex(1, "治愈(.+?)例".toRegex()) ?: -1
        val death = source.extractIntFromRegex(1, "死亡(.+?)例".toRegex()) ?: -1
        return StatisticItem(confirmed, suspected, cured, death)
    }

    private fun extractTime(str: String): String {
        val modifyTime = "\"modifyTime\":(\\d+)".toRegex().extractGroupIn(str, 1) ?: "-1"
        val literalTime = "截至 ([\\d\\s-:]+?)（北京时间）数据统计".toRegex().extractGroupIn(str, 0) ?: "--"
        return "$modifyTime\n$literalTime"
    }

    private fun extractAreaStat(str: String): String? {
        val regex = "window.getAreaStat = (.*?)}catch\\(e\\)\\{}</script>"
                .toRegex()
        return regex.extractGroupIn(str, 1)
    }

    override fun parseResponse(response: Response): Statistics {
        val string = response.body?.string()
        try {
            string ?: throw BadResponseException.nullBody()
            val counts = count(string)
            val time = extractTime(string)
            val areaStat = extractAreaStat(string)
            return Statistics(time, counts, areaStat)
        } catch (e: Exception) {
            log.e(msg = "Failed to parse response. Response body: $string")
            throw e
        }
    }
}