package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object JHRPluginConfig : AutoSavePluginConfig("HorseRacingPluginConfig") {

    @ValueDescription("签到奖励")
    val signReward by value(5000)

    @ValueDescription("启用赛马的群")
    var enabledGroups: MutableList<Long> by value()

    @ValueDescription("查询余额命令")
    val queryScoreCommand: List<String> by value(listOf(
        "我有多少钱鸭老婆",
        "老婆我有多少钱",
        "我有多少钱",
        "我有多少钱老婆",
        "老子还有多少钱",
    ))

    @ValueDescription("好事件 ?为占位符")
    val goodEvents: List<String> by value(listOf(
        "?号马发现了前方的母马，加速加速！",
        "?号马使用了私藏的超级棒棒糖，加速加速！",
        "?号马已经没什么所谓了！",
        "?号马发现，赛道岂是如此不便之物！",
    ))

    @ValueDescription("坏事件 ?为占位符")
    val badEvents: List<String> by value(listOf(
        "?号马滑倒了！",
        "?号马自由了！",
        "?号马踩到了sf！",
        "?号马突然想上天摘星星！",
        "?号马掉入了时辰的陷阱！",
    ))

//    @ValueDescription("赛马数量")
//    val horseCount by value(5)
}