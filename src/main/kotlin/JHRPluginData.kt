package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.jie65535.jhr.game.PlayerStatistics

object JHRPluginData : AutoSavePluginData("HorseRacingPluginData") {

    @ValueDescription("用户存款")
    val Scores: MutableMap<Long, Int> by value()

    @ValueDescription("用户统计")
    val playerStat: MutableMap<Long, PlayerStatistics> by value()
}