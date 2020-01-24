package studio.papercube.ncovmonitor

import okhttp3.Request

abstract class DataSource(
        protected val urlString: String
) {
    protected open val userAgent get() = HttpRequestParameters.userAgent
    protected open val acceptContent: String get() = HttpRequestParameters.acceptContent

    fun Request.Builder.setBasicProperties() = apply{
        url(urlString)
        addHeader("user-agent", userAgent)
        addHeader("accept", acceptContent)
    }

    abstract fun fetchDataSource(): StatisticItem
}