package top.jie65535.jhr

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.info
import java.util.*
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
    private var date = Calendar.getInstance().get(Calendar.DATE)
    private val mSignUpSheet = mutableListOf<Long>()
    private val signUpSheet: MutableList<Long>
        get() {
            val now = Calendar.getInstance().get(Calendar.DATE)
            if (date != now) {
                date = now
                mSignUpSheet.clear()
            }
            return mSignUpSheet
        }

    private fun checkSign(id: Long): Boolean {
        return signUpSheet.indexOf(id) != -1
    }
    // endregion

    //随机签到奖励范围
    val signReward
        get() = (100..1000).random()

    // region 赛马

    private data class Bet(val id: Long, val number: Int, val score: Int)
    private data class Horse(val type: Int, var position: Int = 0)
    private data class Rank(val horses: List<Horse>, val job: Job)

    private val pools = mutableMapOf<Long, MutableList<Bet>>()
    private const val horseCount = 5 //多少个马
    private const val lapLength = 20 //赛道长度
    private val horseTypes = listOf(
        "\uD83E\uDD84",
        "\uD83D\uDC34",
        "\uD83D\uDC14",
        "\uD83D\uDEBD",
        "\uD83D\uDC1C"
    )
    private val ranks = mutableMapOf<Long, Rank>()
    private fun newRank(job: Job) = Rank(List(horseCount) { Horse(Random.nextInt(horseTypes.size)) }, job)
    private fun drawHorse(horses: List<Horse>): String {
        val sb = StringBuilder()
        for ((i, horse) in horses.withIndex()) {
            sb.append(i + 1)
            for (j in horse.position until lapLength)
                sb.append("Ξ")
            sb.append(horseTypes[horse.type])
            for (j in 1 until horse.position)
                sb.append("Ξ")
            sb.appendLine()
        }
        return sb.toString()
    }

    // endregion

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        JHRPluginConfig.reload()
        JHRPluginData.reload()
        JHRCommand.register()

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            val msg = message.contentToString()

            // 确认该群是否启用赛马
            if (JHRPluginConfig.enabledGroups.indexOf(group.id) == -1) {
                if (msg == "开启赛马" && sender.permission.isOperator()) {
                    JHRPluginConfig.enabledGroups.add(group.id)
                    subject.sendMessage("已开启赛马")
                }
                return@subscribeAlways
            }

            when {
                msg.startsWith("赛马") -> {
                    if (pools[subject.id] != null) {
                        subject.sendMessage("已经有比赛在进行了")
                    } else {
                        pools[subject.id] = mutableListOf()
                        subject.sendMessage("赛马比赛开盘，有钱交钱妹钱交人")
                    }
                }
                msg.startsWith("开始赛马") -> {
                    if (ranks[subject.id] != null) return@subscribeAlways
                    subject.sendMessage("赛马开始辣，走过路过不要错过")
                    val rank = newRank(Job())
                    ranks[subject.id] = rank
                    subject.sendMessage(drawHorse(rank.horses))
                    launch(rank.job) {
                        var winner = -1
                        while (winner == -1) {
                            delay(Random.nextLong(1000) + 2000)
                            // 比赛事件触发
                            val steps = (1..3).random() //事件触发前进或后退随机大小
                            val eventHorseIndex = Random.nextInt(rank.horses.size)
                            val eventHorse = rank.horses[eventHorseIndex]
                            val eventMsg = if (Random.nextInt(77) > 32) {
                                eventHorse.position += steps
                                JHRPluginConfig.goodEvents[Random.nextInt(JHRPluginConfig.goodEvents.size)]
                            } else {
                                eventHorse.position -= steps
                                JHRPluginConfig.badEvents[Random.nextInt(JHRPluginConfig.badEvents.size)]
                            }
                            subject.sendMessage(eventMsg.replace("?", (eventHorseIndex + 1).toString()))

                            // 所有马前进
                            for ((i, horse) in rank.horses.withIndex()) {
                                if (++horse.position >= lapLength) {
                                    winner = i + 1
                                }
                            }
                            subject.sendMessage(drawHorse(rank.horses))

                            delay(Random.nextLong(1000) + 3000)
                        }
                        val mb = MessageChainBuilder()
                        mb.add("${winner}最终赢得了胜利，让我们为它鼓掌")
                        ranks.remove(subject.id)
                        val pool = pools.remove(subject.id)
                        if (pool != null && pool.size > 0) {
                            for (bet in pool) {
                                val score = JHRPluginData.Scores[bet.id]!!
                                val income = if (bet.number == winner) {
                                    (bet.score * 1.5).toInt()
                                } else {
                                    -bet.score
                                }
                                JHRPluginData.Scores[bet.id] = score + income
                                mb.add("\n")
                                mb.add(At(bet.id))
                                mb.add(PlainText("收益${income}"))
                            }
                        }
                        subject.sendMessage(mb.asMessageChain())
                    }
                }
                msg == "关闭赛马" -> {
                    if (sender.permission.isOperator()) {
                        JHRPluginConfig.enabledGroups.remove(subject.id)
                        subject.sendMessage("已关闭赛马")
                    }
                }
                msg == "签到" -> {
                    if (checkSign(sender.id)) {
                        subject.sendMessage("一天只能签到一次噢")
                    } else {
                        signUpSheet.add(sender.id)
                        val score = JHRPluginData.Scores[sender.id]
                        val reward = signReward
                        if (score != null) {
                            JHRPluginData.Scores[sender.id] = score + reward
                            subject.sendMessage("硬币+${reward}，现有${score + reward}硬币")
                        } else {
                            JHRPluginData.Scores[sender.id] = reward
                            subject.sendMessage("硬币+${reward}")
                        }
                    }
                }
                msg.startsWith("押马") -> {
                    val pool = pools[subject.id] ?: return@subscribeAlways
                    if (pool.any { it.id == sender.id }) {
                        subject.sendMessage("你已经下过注辣")
                        return@subscribeAlways
                    }

                    val m = msg.removePrefix("押马")
                    val p = m.split(' ')
                    if (p.size != 2) {
                        return@subscribeAlways
                    }
                    val no = p[0].toIntOrNull()
                    val coin = p[1].toIntOrNull()
                    if (no == null || no < 1 || no > horseCount) {
                        subject.sendMessage("没有这个编号的选手")
                        return@subscribeAlways
                    }
                    if (coin == null || coin < 0) {
                        subject.sendMessage("胡乱下注不可取")
                        return@subscribeAlways
                    }
                    val score = JHRPluginData.Scores[sender.id]
                    if (score == null || score - coin < 0) {
                        subject.sendMessage("没那么多可以下注的币惹")
                        return@subscribeAlways
                    }

                    pool.add(Bet(sender.id, no, coin))
                    subject.sendMessage("下注完成 加油啊${no}号马")
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
                    ret.add("有${score}块钱")
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
