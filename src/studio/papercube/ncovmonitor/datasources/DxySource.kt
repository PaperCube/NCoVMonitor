package studio.papercube.ncovmonitor.datasources

import okhttp3.Request
import okhttp3.Response
import studio.papercube.ncovmonitor.*

class DxySource : DataSource(
        "https://3g.dxy.cn/newh5/view/pneumonia"
) {
    private fun newRequest(): Request {
        val request = Request.Builder()
                .setBasicProperties()
                .get()
        return request.build()
    }

    private fun String.trimToIntOrNull(): Int? {
        return trim().toIntOrNull()
    }

    private fun String.extractIntFromRegex(pos: Int, regex: Regex): Int? {
        val str = regex.find(this, 0)?.groupValues?.getOrNull(pos)
        val ret = str?.trimToIntOrNull()
        if (ret == null) {
            log.w("$this: Match against $regex failed")
        }
        return ret
    }

    private fun parseResponse(response: Response): StatisticItem {
        val string = response.body?.string()
        try {
            string ?: throw BadResponseException("Successfully retrieved response but got null string")
            //language=RegExp
            val regex = "\"countRemark\":\".+?\"".toRegex()
            val match = regex.find(string)
                    ?: throw BadResponseException("Cannot find result: no match against given pattern found")
            val groupValues = match.groupValues
            val source: String? = groupValues.firstOrNull()
            val confirmed = source?.extractIntFromRegex(1, "确诊(.+?)例".toRegex())
                    ?: throw BadResponseException("\$Confirmed is null")
            val suspected = source.extractIntFromRegex(1, "疑似(.+?)例".toRegex()) ?: -1
            val cured = source.extractIntFromRegex(1, "治愈(.+?)例".toRegex()) ?: -1
            val death = source.extractIntFromRegex(1, "死亡(.+?)例".toRegex()) ?: -1
            return StatisticItem(confirmed, suspected, cured, death)
        } catch (e: Exception) {
            log.e(msg = "Failed to parse response. Response body: $string")
            throw e
        }

    }

    override fun fetchDataSource(): StatisticItem {
        val request = newRequest()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw BadResponseException("Request failed")
        }
        return parseResponse(response)
    }
}