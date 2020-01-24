package studio.papercube.ncovmonitor

data class Statistics(
        val updateTime: String,
        val overall: StatisticItem,
        val provinceData: String?
)