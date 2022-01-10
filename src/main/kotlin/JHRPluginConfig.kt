package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object JHRPluginConfig : AutoSavePluginConfig("HorseRacingPluginConfig") {

    @ValueDescription("签到奖励")
    val signInReward by value(5000)

    @ValueDescription("启用赛马的群")
    var enabledGroups: MutableList<Long> by value()
}