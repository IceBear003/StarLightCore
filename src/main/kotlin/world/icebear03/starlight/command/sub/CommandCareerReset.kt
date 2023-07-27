package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.career


val commandCareerReset = subCommand {
    execute<Player> { sender, _, _ ->
        sender.career().remake()
        sender.closeInventory()
        sender.sendMessage("§a生涯系统 §7>> 数据已重置")
    }
}