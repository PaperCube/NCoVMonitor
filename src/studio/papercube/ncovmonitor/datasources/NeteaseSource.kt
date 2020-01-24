package studio.papercube.ncovmonitor.datasources

import studio.papercube.ncovmonitor.DataSource
import studio.papercube.ncovmonitor.Statistics

class NeteaseSource : DataSource(
        "Netease",
        "https://news.163.com/special/epidemic/"
){
    override fun fetchDataSource(): Statistics {
        TODO("not implemented")
    }
}