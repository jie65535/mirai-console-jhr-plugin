package top.jie65535.jhr

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.info
import top.jie65535.jhr.game.Bet
import top.jie65535.jhr.game.Horse
import top.jie65535.jhr.game.PlayerStatistics
import java.util.*
import kotlin.math.min
import kotlin.random.Random


object JHorseRacing : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jhr-plugin",
        name = "J Horse Racing",
        version = "0.1.0"
    ) {
        author("jie65535")
        info("赛马")
    }
) {
    // region 签到
    private val signUpSheet: MutableList<Long>
        get() {
            val now = Calendar.getInstance().get(Calendar.DATE)
            if (JHRPluginData.signDate != now) {
                JHRPluginData.signDate = now
                JHRPluginData.signUpSheet.clear()
            }
            return JHRPluginData.signUpSheet
        }

    private fun checkSign(id: Long): Boolean {
        return signUpSheet.indexOf(id) != -1
    }
    // endregion

    //随机签到奖励范围
    private val signReward
        get() = (100..1000).random()

    // region 赛马

    private data class Rank(val horses: List<Horse>, val job: Job)

    /**
     * 奖池
     */
    private val pools = mutableMapOf<Long, MutableList<Bet>>()
    private const val horseCount = 5 //多少个马
    private const val lapLength = 20 //赛道长度
    private val horseTypes = listOf(
        "\uD83E\uDD84",//独角兽也算马吧
        "\uD83D\uDC34",//真马·头
        "\uD83D\uDC14",//超级鸡马（
        "\uD83D\uDEBD",//马桶也算马！
        "\uD83D\uDC1C",//蚂蚁（读音带ma也算！
        "\uD83D\uDC0E",//另一只真·马
        "\uD83D\uDFC7",//赛马
        "\uD83C\uDFC7\uD83C\uDFFB",//赛马
        "\uD83C\uDFC7\uD83C\uDFFC",//赛马
        "\uD83C\uDFC7\uD83C\uDFFD",//赛马
        "\uD83C\uDFC7\uD83C\uDFFE",//赛马
        "\uD83C\uDFC7\uD83C\uDFFF",//赛马
        "\uD83C\uDDF2\uD83C\uDDFE", //马来西亚也算马吧，应该显示my
        "\ufffd", //乱码2
        "\u265e",//国际象棋马
        "\u2658",//国际象棋马
        "\ud83e\udd9b",//河马
        "\ud83e\udd54",//马铃薯也算马！
        "\ud83e\ude63",//象棋的马
        "\ud83e\ude6a",//象棋的马（不知道能不能显示
        "\ud83d\udc12",//弼马温(猴)也算马！
    )
    private const val horseLogo = "\uD83C\uDFC7\uD83C\uDFFB"
    private val ranks = mutableMapOf<Long, Rank>()
    private fun drawHorse(horses: List<Horse>): String {
        val sb = StringBuilder()
        for ((i, horse) in horses.withIndex()) {
            sb.append(i + 1)
            for (j in horse.position until lapLength)
                sb.append("Ξ")
            sb.append(horseTypes[horse.type])
            for (j in 0 until min(lapLength, horse.position))
                sb.append("Ξ")
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun getPlayerStat(id: Long): PlayerStatistics {
        var stat = JHRPluginData.playerStat[id]
        if (stat == null) {
            stat = PlayerStatistics()
            JHRPluginData.playerStat[id] = stat
        }
        return stat
    }

    private fun addContribution(id: Long) {
        getPlayerStat(id).contribution += 1
    }

    private suspend fun startRank(subject: Group) {
        val t = pools[subject.id] ?: return
        if (t.size == 0) {
            subject.sendMessage("无人下分，无法开始哦")
            return
        }
        if (ranks[subject.id] != null) return
        logger.info("开始赛马")
        subject.sendMessage("$horseLogo 开始辣，走过路过不要错过")
        val rank = Rank(List(horseCount) { Horse(Random.nextInt(horseTypes.size)) }, Job())
        ranks[subject.id] = rank
        subject.sendMessage(drawHorse(rank.horses))
        launch(rank.job) {
            val winners = mutableListOf<Int>()
            while (winners.size == 0) {
                delay(Random.nextLong(5000, 7000))

                // 所有马前进
                for (horse in rank.horses) {
                    ++horse.position
                }

                // 比赛事件触发
                val steps = (1..3).random() //事件触发前进或后退随机大小
                // 事件赛马下标
                val eventHorseIndex = Random.nextInt(rank.horses.size)
                // 事件赛马
                val eventHorse = rank.horses[eventHorseIndex]
                // 触发事件
                val eventMsg = if (Random.nextInt(77) > 32) {
                    eventHorse.position += steps
                    JHRPluginConfig.goodEvents[Random.nextInt(JHRPluginConfig.goodEvents.size)]
                } else {
                    eventHorse.position -= steps
                    JHRPluginConfig.badEvents[Random.nextInt(JHRPluginConfig.badEvents.size)]
                }


                // 计算获胜者
                for ((i, horse) in rank.horses.withIndex()) {
                    if (horse.position >= lapLength) {
                        winners.add(i + 1)
                    }
                }

                // 发送事件消息
                val number = (eventHorseIndex + 1).toString()
                subject.sendMessage(eventMsg.replace("?", number))
                delay(Random.nextLong(100, 200))
                // 渲染赛马
                subject.sendMessage(drawHorse(rank.horses))
            }
            delay(Random.nextLong(100, 200))
            val mb = MessageChainBuilder()
            for (winner in winners) {
                // 增加该赛马胜场
                JHRPluginData.horseWinCount[winner-1] += 1
                mb.add(JHRPluginConfig.winnerMessage[Random.nextInt(JHRPluginConfig.winnerMessage.size)].replace("?", winner.toString()))
                mb.add("\n")
            }
            // 增加总赛马场次
            JHRPluginData.totalRankCount += 1
            // 移除比赛
            ranks.remove(subject.id)
            // 奖池结算
            val pool = pools.remove(subject.id)
            if (pool != null && pool.size > 0) {
                for (bet in pool) {
                    val score = JHRPluginData.Scores[bet.id]!!
                    val stat = getPlayerStat(bet.id)
                    // 下注计数
                    stat.betCount += 1
                    // 下注积分累积
                    stat.totalBetScore += bet.score
                    val income = if (winners.indexOf(bet.number) != -1) {
                        // 胜场计数累积
                        stat.winCount += 1
                        // 收益积分
                        (bet.score * if (bet.score == score)
                            JHRPluginConfig.allinOdds // 若allin，则使用allin赔率
                        else
                            // 否则根据下注金额与余额的比值计算赔率
                            min(
                                JHRPluginConfig.minOdds,
                                bet.score.toDouble() / score * JHRPluginConfig.maxOdds
                            )
                            ).toInt()
                    } else {
                        // 亏损积分
                        -bet.score
                    }
                    // 计算积分
                    JHRPluginData.Scores[bet.id] = score + income
                    mb.add("\n")
                    mb.add(At(bet.id))
                    if (income > 0) {
                        mb.add(" +$income")
                        // 累计收益
                        stat.totalWinScore += income
                    } else {
                        mb.add(" $income")
                        // 累计亏损
                        stat.totalLossScore += income
                    }
                }
            }
            subject.sendMessage(mb.asMessageChain())
            // 手动保存数据
            JHRPluginData.save()
            logger.info("赛马结束")
        }
    }

    // endregion

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        JHRPluginConfig.reload()
        JHRPluginData.reload()
        JHRCommand.register()

        // 初始化赛马胜场计数器
        while (JHRPluginData.horseWinCount.size < horseCount) {
            JHRPluginData.horseWinCount.add(0)
        }

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            val msg = message.contentToString()

            // 确认该群是否启用赛马
            if (JHRPluginConfig.enabledGroups.indexOf(group.id) == -1) {
                if (msg == "开启赛马" && sender.permission.isOperator()) {
                    JHRPluginConfig.enabledGroups.add(group.id)
                    subject.sendMessage("已开启$horseLogo")
                    logger.info("群 ${subject.id} 已启用赛马")
                }
                return@subscribeAlways
            }

            when {
                msg.startsWith("赛马") -> {
                    if (pools[subject.id] != null) {
                        subject.sendMessage("已经有比赛在进行了")
                    } else {
                        logger.info("群 ${subject.id} 已开盘")
                        val pool = mutableListOf<Bet>()
                        pools[subject.id] = pool
                        subject.sendMessage("${horseLogo}来咯，有钱交钱妹钱交人。\n${JHRPluginConfig.autoStartTime}秒后将自动开始")
                        launch {
                            delay(JHRPluginConfig.autoStartTime * 1000L)
                            if (pools[subject.id] == pool) {
                                startRank(subject)
                            }
                        }
                    }
                }
                msg.startsWith("开始") -> launch {
                    startRank(subject)
                }
                msg.startsWith("结束") -> {
                    if (sender.permission.isOperator()) {
                        val rank = ranks[subject.id]
                        if (rank != null) {
                            rank.job.cancel()
                            ranks.remove(subject.id)
                            pools.remove(subject.id)
                            subject.sendMessage("已结束比赛")
                        } else {
                            subject.sendMessage("没有正在进行中的赛马")
                        }
                    }
                }
                msg == "关闭赛马" -> {
                    if (sender.permission.isOperator()) {
                        JHRPluginConfig.enabledGroups.remove(subject.id)
                        subject.sendMessage("已关闭$horseLogo")
                        logger.info("群 ${subject.id} 已关闭赛马")
                    }
                }
                msg == "签到" -> {
                    if (checkSign(sender.id)) {
                        subject.sendMessage("一天只能签到一次噢")
                    } else {
                        signUpSheet.add(sender.id)
                        getPlayerStat(sender.id).signCount += 1
                        val score = JHRPluginData.Scores[sender.id]
                        val reward = signReward
                        if (score != null) {
                            JHRPluginData.Scores[sender.id] = score + reward
                            subject.sendMessage("积分+${reward}，现有${score + reward}积分")
                        } else {
                            JHRPluginData.Scores[sender.id] = reward
                            subject.sendMessage("积分+${reward}")
                        }
                    }
                }
                msg.startsWith("马") -> {
                    // 如果比赛进行中则不允许下注
                    if (ranks[subject.id] != null) return@subscribeAlways
                    val pool = pools[subject.id] ?: return@subscribeAlways
                    if (pool.any { it.id == sender.id }) {
                        subject.sendMessage("你已经下过分辣")
                        return@subscribeAlways
                    }

                    val m = msg.removePrefix("马")
                    val p = m.split(' ')
                    if (p.size != 2) {
                        return@subscribeAlways
                    }
                    val no = p[0].toIntOrNull()
                    if (no == null || no < 1 || no > horseCount) {
                        subject.sendMessage("没有这个编号的选手")
                        return@subscribeAlways
                    }
                    val score = JHRPluginData.Scores[sender.id]
                    if (score == null || score <= 0) {
                        subject.sendMessage("你还没有积分哦，请通过签到获取积分再来")
                        return@subscribeAlways
                    }
                    // 根据第二个参数确认动作
                    val cmd = p[1]
                    val coin: Int?
                    // '梭哈'表示全压
                    if (cmd == "梭哈" || cmd == "allin") {
                        coin = score
                    } else if (cmd == "随机" || cmd == "random" || cmd == "rand") {
                        // '随机'则是系统随机选择下注积分
                        coin = Random.nextInt(score) + 1
                    } else {
                        // 否则只允许输入数字作为下注积分
                        coin = cmd.toIntOrNull()
                        if (coin == null || coin <= 0) {
                            subject.sendMessage("胡乱下分不可取")
                            return@subscribeAlways
                        }
                    }

                    if (score - coin < 0) {
                        subject.sendMessage("没那么多可以下注的分惹")
                        return@subscribeAlways
                    }

                    pool.add(Bet(sender.id, no, coin))
                    subject.sendMessage(JHRPluginConfig.betMessage[Random.nextInt(JHRPluginConfig.betMessage.size)].replace("?", no.toString()))
                }
                msg.startsWith("增加好事") -> {
                    val event = msg.removePrefix("增加好事").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (event.indexOf('?') == -1) {
                        subject.sendMessage("请使用'?'作为占位符")
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.goodEvents.indexOf(event) == -1) {
                        JHRPluginConfig.goodEvents.add(event)
                        addContribution(sender.id)
                        logger.info("已增加好事件'$event'")
                    }
                    subject.sendMessage("OK")
                }
                msg.startsWith("增加坏事") -> {
                    val event = msg.removePrefix("增加坏事").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (event.indexOf('?') == -1) {
                        subject.sendMessage("请使用'?'作为占位符")
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.badEvents.indexOf(event) == -1) {
                        JHRPluginConfig.badEvents.add(event)
                        addContribution(sender.id)
                        logger.info("已增加坏事件'$event'")
                    }
                    subject.sendMessage("OK")
                }
                msg.startsWith("增加胜利词") -> {
                    val event = msg.removePrefix("增加胜利词").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (event.indexOf('?') == -1) {
                        subject.sendMessage("请使用'?'作为占位符")
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.winnerMessage.indexOf(event) == -1) {
                        JHRPluginConfig.winnerMessage.add(event)
                        addContribution(sender.id)
                        logger.info("已增加胜利词'$event'")
                    }
                    subject.sendMessage("OK")
                }
                msg.startsWith("增加下注词") -> {
                    val event = msg.removePrefix("增加下注词").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (event.indexOf('?') == -1) {
                        subject.sendMessage("请使用'?'作为占位符")
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.betMessage.indexOf(event) == -1) {
                        JHRPluginConfig.betMessage.add(event)
                        addContribution(sender.id)
                        logger.info("已增加下注词'$event'")
                    }
                    subject.sendMessage("OK")
                }
                msg.startsWith("删除好事") -> {
                    val event = msg.removePrefix("删除好事").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.goodEvents.remove(event)) {
                        logger.info("已删除好事件'$event'")
                        subject.sendMessage("OK")
                    } else {
                        subject.sendMessage("没有这一项")
                    }
                }
                msg.startsWith("删除坏事") -> {
                    val event = msg.removePrefix("删除坏事").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.badEvents.remove(event)) {
                        logger.info("已删除坏事件'$event'")
                        subject.sendMessage("OK")
                    } else {
                        subject.sendMessage("没有这一项")
                    }
                }
                msg.startsWith("删除胜利词") -> {
                    val event = msg.removePrefix("删除胜利词").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.winnerMessage.remove(event)) {
                        logger.info("已删除胜利词'$event'")
                        subject.sendMessage("OK")
                    } else {
                        subject.sendMessage("没有这一项")
                    }
                }
                msg.startsWith("删除下注词") -> {
                    val event = msg.removePrefix("删除下注词").trim()
                    if (event.isBlank()) {
                        return@subscribeAlways
                    }
                    if (JHRPluginConfig.betMessage.remove(event)) {
                        logger.info("已删除下注词'$event'")
                        subject.sendMessage("OK")
                    } else {
                        subject.sendMessage("没有这一项")
                    }
                }
                msg == "好事列表" -> {
                    subject.sendMessage(JHRPluginConfig.goodEvents.joinToString("\n"))
                }
                msg == "坏事列表" -> {
                    subject.sendMessage(JHRPluginConfig.badEvents.joinToString("\n"))
                }
                msg == "胜利词列表" -> {
                    subject.sendMessage(JHRPluginConfig.winnerMessage.joinToString("\n"))
                }
                msg == "下注词列表" -> {
                    subject.sendMessage(JHRPluginConfig.betMessage.joinToString("\n"))
                }
                msg == "排名" || msg == "积分榜" -> {
                    val msgB = MessageChainBuilder(11)
                    msgB.append("积分榜\n")
                    JHRPluginData.Scores.entries.filter { subject.contains(it.key) }
                        .sortedByDescending { it.value }
                        .take(10)
                        .onEach {
                            msgB.append("| ${it.value} | ${subject[it.key]!!.nameCardOrNick}\n")
                        }
                    subject.sendMessage(msgB.asMessageChain())
                }
                msg == "白给榜" -> {
                    val msgB = MessageChainBuilder(11)
                    msgB.append("白给榜\n")
                    JHRPluginData.playerStat.entries.filter { subject.contains(it.key) }
                        .sortedBy { it.value.totalLossScore }
                        .take(10)
                        .onEach {
                            msgB.append("| ${it.value.totalLossScore} | ${subject[it.key]!!.nameCardOrNick}\n")
                        }
                    subject.sendMessage(msgB.asMessageChain())
                }
                msg == "收益榜" -> {
                    val msgB = MessageChainBuilder(11)
                    msgB.append("收益榜\n")
                    JHRPluginData.playerStat.entries.filter { subject.contains(it.key) }
                        .sortedByDescending { it.value.totalWinScore }
                        .take(10)
                        .onEach {
                            msgB.append("| ${it.value.totalWinScore} | ${subject[it.key]!!.nameCardOrNick}\n")
                        }
                    subject.sendMessage(msgB.asMessageChain())
                }
                msg == "利润榜" -> {
                    val msgB = MessageChainBuilder(11)
                    msgB.append("利润榜\n")
                    JHRPluginData.playerStat.entries.filter {
                        subject.contains(it.key)
                            && it.value.totalProfit != 0
                    }.sortedByDescending { it.value.totalProfit }
                        .take(10)
                        .onEach {
                            msgB.append("| ${it.value.totalProfit} | ${subject[it.key]!!.nameCardOrNick}\n")
                        }
                    subject.sendMessage(msgB.asMessageChain())
                }
                msg == "统计" -> {
                    val ret = MessageChainBuilder()
                    ret.append(message.quote())
                        .append(getPlayerStat(sender.id).toString())
                        .append("\nヾ(◍°∇°◍)ﾉﾞ继续加油吧！")
                    subject.sendMessage(ret.asMessageChain())
                }
                msg == "胜率" -> {
                    logger.info("查询胜率")
                    val ret = StringBuilder()
                    for (i in 0 until horseCount) {
                        ret.append("${horseLogo}${i+1} ${JHRPluginData.horseWinCount[i]}/${JHRPluginData.totalRankCount}\n")
                    }
                    subject.sendMessage(ret.toString())
                }
            }
        }

        eventChannel.subscribeAlways<MessageEvent> {
            val msg = message.contentToString().trim()
            if (JHRPluginConfig.queryScoreCommand.indexOf(msg) != -1) {
                // 查询余额
                val score = JHRPluginData.Scores[it.sender.id]
                val ret = MessageChainBuilder()
                ret.add(message.quote())
                if (score != null) {
                    ret.add("有${score}积分")
                    if (!checkSign(sender.id)) {
                        ret.add("，还没有签到哦")
                    }
                } else {
                    ret.add("手里捧着窝窝头，菜里没有一滴油")
                }
                subject.sendMessage(ret.asMessageChain())
            }
        }
    }

    override fun onDisable() {
        JHRCommand.unregister()
    }
}
