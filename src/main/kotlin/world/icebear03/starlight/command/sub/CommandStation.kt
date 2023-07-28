package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem
import world.icebear03.starlight.station.getStation


val commandStation = subCommand {
    execute<Player> { sender, _, _ ->
        sender.giveItem(sender.getStation().generateItem())
        sender.sendMessage("§a生涯系统 §7>> §b驻扎信标§7已经发送至背包")
    }
}