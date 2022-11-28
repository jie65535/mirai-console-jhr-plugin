package top.jie65535.jhr.game
import kotlinx.serialization.Serializable

/**
 * 玩家数据统计
 */
@Serializable
class PlayerStatistics {
    /**
     * 下注次数
     */
    var betCount = 0

    /**
     * 胜利次数
     */
    var winCount = 0

    /**
     * 贡献次数（增加词条次数）
     */
    var contribution = 0

    /**
     * 签到次数
     */
    var signCount = 0

    /**
     * 下注积分累计
     */
    var totalBetScore = 0

    /**
     * 获胜积分累计
     */
    var totalWinScore = 0

    /**
     * 失败积分累计 （负数）
     */
    var totalLossScore = 0

    /**
     * 总利润
     */
    val totalProfit get() = totalWinScore + totalLossScore

    /**
     * 胜率
     */
    val winPercentage get() = if (betCount > 0) (winCount/betCount.toDouble()*100).toInt() else 0

    override fun toString(): String {
        return "获胜次数：${winCount}/${betCount} (${winPercentage}%)\n" +
            "贡献次数：${contribution}\n" +
            "签到次数：${signCount}\n" +
            "下注积分：${totalBetScore}\n" +
            "收益：${totalWinScore}\n" +
            "亏损：${totalLossScore}"
    }
}
