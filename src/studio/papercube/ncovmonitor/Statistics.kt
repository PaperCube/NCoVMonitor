package studio.papercube.ncovmonitor

@Deprecated("Use StatObject instead")
data class Statistics(
        val updateTime: String,
        val overall: StatisticItem,
        val provinceData: String?
)