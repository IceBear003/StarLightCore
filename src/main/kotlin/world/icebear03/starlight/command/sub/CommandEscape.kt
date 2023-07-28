package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand


val commandEscape = subCommand {
    execute<Player> { sender, _, _ ->
        sender.teleport(sender.location.add(0.0, 0.5, 0.0))
        sender.sendMessage("§b繁星工坊 §7>> 尝试将你带离卡点...")
    }
}