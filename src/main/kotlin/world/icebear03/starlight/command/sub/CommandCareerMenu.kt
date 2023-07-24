package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.career.gui.CareerMenuUI


val commandCareerMenu = subCommand {
    execute<Player> { sender, _, _ ->
        CareerMenuUI.open(sender, null)
    }
}