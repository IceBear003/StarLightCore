package world.icebear03.starlight.command.sub

import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand

var rewardLocation: Location? = null

val commandRewardLocation = subCommand {
    execute<Player> { sender, _, _ ->
        rewardLocation = sender.location
        sender.sendMessage("§b繁星工坊 §7>> 设置奖励坐标完成！")
    }
}