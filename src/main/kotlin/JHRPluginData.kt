package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.jie65535.jhr.game.PlayerStatistics
import java.util.*

object JHRPluginData : AutoSavePluginData("HorseRacingPluginData") {

    @ValueDescription("用户存款")
    val Scores: MutableMap<Long, Int> by value()

    @ValueDescription("用户统计")
    val playerStat: MutableMap<Long, PlayerStatistics> by value()

    @ValueDescription("总比赛场次")
    var totalRankCount by value(0)

    @ValueDescription("赛马获胜计数")
    val horseWinCount: MutableList<Int> by value()

    @ValueDescription("签到表日期")
    var signDate by value(Calendar.getInstance().get(Calendar.DATE))

    @ValueDescription("当日签到表")
    var signUpSheet by value(mutableListOf<Long>())
}