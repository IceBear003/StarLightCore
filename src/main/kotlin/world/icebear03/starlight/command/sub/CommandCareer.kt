package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.career.gui.CareerUI


val commandCareer = subCommand {
    execute<Player> { sender, _, _ ->
        CareerUI.open(sender, null)
    }
}