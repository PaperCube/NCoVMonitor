package studio.papercube.ncovmonitor.datasources

import okhttp3.Response
import studio.papercube.ncovmonitor.*

class DxySource : DataSource(
        "dxy",
        "https://ncov.dxy.cn/ncovh5/view/pneumonia"
) {
    private fun count(str: String): StatisticItem {
        //language=RegExp
        val regex = "\"countRemark\":\".*?\",(\"confirmedCount\":.+?),\"virus\":".toRegex()
        val match = regex.find(str)
                ?: throw BadResponseException.noMatch()
        val groupValues = match.groupValues
        val source: String? = groupValues.getOrNull(1)
        val confirmed = source?.extractIntFromRegex(1, "\"confirmedCount\":(\\d+)".toRegex())
                ?: throw BadResponseException.nullValue("confirmed")
        val suspected = source.extractIntFromRegex(1, "\"suspectedCount\":(\\d+)".toRegex()) ?: -1
        val cured = source.extractIntFromRegex(1, "\"curedCount\":(\\d+)".toRegex()) ?: -1
        val death = source.extractIntFromRegex(1, "\"deadCount\":(\\d+)".toRegex()) ?: -1
        return StatisticItem(confirmed, suspected, cured, death)
    }

    private fun extractTime(str: String): Any {
        val modifyTime = "\"modifyTime\":(\\d+)".toRegex().extractGroupIn(str, 1) ?: "-1"
        val literalTime = "截至 ([\\d\\s-/:]+?) 全国数据统计".toRegex().extractGroupIn(str, 0) ?: "--"
        return object {
            val modifyTime = modifyTime
            val literalTime = literalTime
        }
    }

    private fun extractAreaStat(str: String): String? {
        val regex = "window.getAreaStat = (.*?)}catch\\(e\\)\\{}</script>"
                .toRegex()
        return regex.extractGroupIn(str, 1)
    }

    override fun parseResponse(response: Response): StatObject {
        val string = response.body?.string()
        try {
            string ?: throw BadResponseException.nullBody()
            val counts = count(string)
            val time = extractTime(string)
            val areaStat = extractAreaStat(string)
            return newStatObject(
                    "counts" to counts,
                    "time" to time,
                    "provinceData" to areaStat
            )
        } catch (e: Exception) {
            log.e(msg = "Failed to parse response. Response body: $string")
            throw e
        }
    }
}