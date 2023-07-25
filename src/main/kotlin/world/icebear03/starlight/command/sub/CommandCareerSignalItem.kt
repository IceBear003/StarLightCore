package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.mechanism.discharge.DischargeHandler


val commandCareerSignalItem = subCommand {
    execute<Player> { sender, _, _ ->
        sender.giveItem(DischargeHandler.item.clone())
    }
}