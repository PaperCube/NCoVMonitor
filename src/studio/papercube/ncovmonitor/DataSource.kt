package studio.papercube.ncovmonitor

import okhttp3.Request
import okhttp3.Response

abstract class DataSource(
        val sourceName: String,
        protected val urlString: String
) {
    protected open val userAgent get() = HttpRequestParameters.userAgent
    protected open val acceptContent: String get() = HttpRequestParameters.acceptContent

    protected open fun newStatObject(vararg pairs: Pair<*, *>): StatObject {
        return StatObject.of(sourceName, *pairs)
    }

    protected open fun newRequestBuilder(): Request.Builder {
        return Request.Builder()
                .setBasicProperties()
                .get()
    }

    protected open fun newRequest(): Request {
        return newRequestBuilder().build()
    }

    open fun Request.Builder.setBasicProperties() = apply {
        url(urlString)
        addHeader("user-agent", userAgent)
        addHeader("accept", acceptContent)
    }

    protected abstract fun parseResponse(response: Response): StatObject

    open fun fetchDataSource(): StatObject {
        val request = newRequest()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw BadResponseException("Request failed")
        }
        return parseResponse(response)
    }

    override fun toString(): String {
        return "DataSource(sourceName='$sourceName', urlString='$urlString')"
    }
}