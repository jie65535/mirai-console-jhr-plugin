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

    override fun toString(): String {
        return "下注次数：${betCount}\n" +
            "获胜次数：${winCount}\n" +
            "贡献次数：${contribution}\n" +
            "签到次数：${signCount}\n" +
            "下注积分：${totalBetScore}"
    }
}
