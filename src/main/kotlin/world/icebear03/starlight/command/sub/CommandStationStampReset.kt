package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.station.getStation


val commandStationStampReset = subCommand {
    execute<Player> { sender, _, _ ->
        sender.getStation().stamp = System.currentTimeMillis() - 100000000
        sender.sendMessage("§a生涯系统 §7>> §b驻扎信标§7回收/重放置冷却已清除")
    }
}