package top.jie65535.jhr.game
import kotlinx.serialization.Serializable

@Serializable
class PlayerStatistics {
    var betCount = 0
    var winCount = 0
    var contribution = 0
    var signCount = 0
    var totalBetScore = 0

    override fun toString(): String {
        return "下注次数：${betCount}\n" +
            "获胜次数：${winCount}\n" +
            "贡献次数：${contribution}\n" +
            "签到次数：${signCount}\n" +
            "下注积分：${totalBetScore}"
    }
}
