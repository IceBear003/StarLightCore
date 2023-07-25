package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.loadCareerData


val commandCareerReset = subCommand {
    execute<Player> { sender, _, _ ->
        loadCareerData(sender).remake()
        sender.closeInventory()
        sender.sendMessage("§a生涯系统 §7>> 数据已重置")
    }
}