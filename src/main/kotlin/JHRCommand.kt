package top.jie65535.jhr

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig

object JHRCommand : CompositeCommand(
    JHorseRacing, "jhr",
    description = "HorseRacing Commands"
) {
    @SubCommand
    @Description("开启赛马")
    suspend fun CommandSender.enable(group: Long) {
        if (JHRPluginConfig.enabledGroups.indexOf(group) == -1)
            JHRPluginConfig.enabledGroups.add(group)
        sendMessage("OK")
    }

    @SubCommand
    @Description("开启赛马")
    suspend fun CommandSender.disable(group: Long) {
        JHRPluginConfig.enabledGroups.remove(group)
        sendMessage("OK")
    }
    @SubCommand
    @Description("重载配置")
    suspend fun CommandSender.reload() {
        JHorseRacing.reloadPluginConfig(JHRPluginConfig)
        sendMessage("OK")
    }
}