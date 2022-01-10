package top.jie65535.jhr

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

object JHRCommand : CompositeCommand(
    JHorseRacing, "jhr",
    description = "HorseRacing Commands"
) {
    @SubCommand
    @Deprecated("开启赛马")
    suspend fun CommandSender.enable(group: Long) {
        if (JHRPluginConfig.enabledGroups.indexOf(group) == -1)
            JHRPluginConfig.enabledGroups.add(group)
        sendMessage("OK")
    }

    @SubCommand
    @Deprecated("开启赛马")
    suspend fun CommandSender.disable(group: Long) {
        JHRPluginConfig.enabledGroups.remove(group)
        sendMessage("OK")
    }
}