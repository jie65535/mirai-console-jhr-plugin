package top.jie65535.jhr

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import java.util.*


object JHorseRacing : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jhr-plugin",
        name = "J Horse Racing",
        version = "0.1.0"
    ) {
        author("jie65535")
        info("赛马娘")
    }
) {
    var date: Date = Calendar.getInstance().time

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        JHRPluginConfig.reload()
        JHRPluginData.reload()
        JHRCommand.register()

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            // 确认该群是否启用赛马
            if (JHRPluginConfig.enabledGroups.indexOf(group.id) == -1) {
                return@subscribeAlways
            }

            val msg = message.contentToString()
            // at机器人
            if (message.any { it is At && it.target == bot.id }) {
                when {
                    msg.startsWith("#赛马") -> TODO()
                    msg.startsWith("#开始赛马") -> TODO()
                }
            } else {
                when {
                    msg.startsWith("#签到") -> TODO()
                    msg.startsWith("#押马") -> TODO()
                    msg.startsWith("#余额") -> TODO()
                }
            }
        }
    }

    override fun onDisable() {
        JHRCommand.unregister()
    }


}
