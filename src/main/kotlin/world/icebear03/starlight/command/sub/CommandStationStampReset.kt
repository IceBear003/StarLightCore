package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.station.station


val commandStationStampReset = subCommand {
    execute<Player> { sender, _, _ ->
        sender.station().stamp = System.currentTimeMillis() - 100000000
        sender.sendMessage("§b繁星工坊 §7>> §b驻扎信标§7重放置冷却已清除")
    }
}