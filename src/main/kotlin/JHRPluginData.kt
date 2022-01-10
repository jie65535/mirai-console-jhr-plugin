package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object JHRPluginData : AutoSavePluginData("HorseRacingPluginData") {

    @ValueDescription("用户存款")
    val Scores: HashMap<Long, Int> by value()

}