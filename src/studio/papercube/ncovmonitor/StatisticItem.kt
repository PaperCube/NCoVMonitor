package studio.papercube.ncovmonitor

data class StatisticItem(
        val confirmedCount: Int,
        val suspectedCount: Int,
        val curedCount: Int,
        val deathCount: Int,
        val seriousCount:Int = -1
)