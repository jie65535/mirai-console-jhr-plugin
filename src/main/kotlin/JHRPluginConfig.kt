package top.jie65535.jhr

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object JHRPluginConfig : AutoSavePluginConfig("HorseRacingPluginConfig") {

    @ValueDescription("启用赛马的群")
    var enabledGroups: MutableList<Long> by value()

    @ValueDescription("查询余额命令")
    val queryScoreCommand: MutableList<String> by value(
        mutableListOf(
            "我有多少钱鸭老婆",
            "老婆我有多少钱",
            "我有多少钱",
            "我有多少钱老婆",
            "老子还有多少钱",
            "查询",
        )
    )

    @ValueDescription("好事件 ?为占位符")
    val goodEvents: MutableList<String> by value(
        mutableListOf(
            "?号马发现了前方的母马，加速加速！",
            "?号马使用了私藏的超级棒棒糖，加速加速！",
            "?号马已经没什么所谓了！",
            "?号马发现，赛道岂是如此不便之物！",
            "?号马使用了砸瓦鲁多！",
            "?号马说:兄弟！买挂吗！",
            "?号马旋转升天法力无边！",
            "?号马那我走？",
            "?号马勇敢牛牛不怕困难!",
            "?号马三点了！饮茶先啊！",
            "?号马使用了印尼宽带！",
            "?号马正面上我啊！",
            "?号马发现了前方有电脑配件！",
            "?号马欧拉欧拉欧拉欧拉欧拉！",
            "?号马就这就这？",
        )
    )

    @ValueDescription("坏事件 ?为占位符")
    val badEvents: MutableList<String> by value(
        mutableListOf(
            "?号马滑倒了！",
            "?号马自由了！",
            "?号马踩到了sf！",
            "?号马突然想上天摘星星！",
            "?号马掉入了时辰的陷阱！",
            "?号马突然想吃饭！",
            "?号马看到了后方的母马！",
            "?号马前去买瓜",
            "?号马~希望の花 繋いだ絆を~",
            "?号马看到了招生'减'章！",
            "?号马听君一席话如听君一席话",
            "?号马网抑云了",
            "?号马小丑竟是我自己！",
            "?号马希望大家玩得愉快",

            )
    )

    /**
     * 自动开始时间(s)
     */
    @ValueDescription("自动开始时间(s)")
    var autoStartTime by value(180)
}
//    @ValueDescription("赛马数量")
//    val horseCount by value(5)