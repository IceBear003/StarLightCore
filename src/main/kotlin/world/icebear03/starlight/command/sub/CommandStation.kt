package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem
import world.icebear03.starlight.station.station


val commandStation = subCommand {
    execute<Player> { sender, _, _ ->
        sender.giveItem(sender.station().generateItem())
        sender.sendMessage("§b繁星工坊 §7>> §b驻扎信标§7已经发送至背包")
    }
}