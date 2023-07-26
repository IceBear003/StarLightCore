package world.icebear03.starlight.command

import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import world.icebear03.starlight.command.sub.*

@CommandHeader(name = "starlightcore", aliases = ["sl", "slc", "cd"])
object Commands {

    @CommandBody
    val main = mainCommand {
        execute<Player> { sender, _, _ ->
            sender.performCommand("bs 主菜单")
        }
    }

    @CommandBody
    val help = subCommand {
        createHelper(checkPermissions = true)
    }

    @CommandBody
    val careerMenu = commandCareerMenu

    @CommandBody
    val careerPoint = commandCareerPoint

    @CommandBody
    val careerSignalItem = commandCareerSignalItem

    @CommandBody
    val careerReset = commandCareerReset

    @CommandBody
    val beacon = commandBeacon

    @CommandBody
    val stamina = commandStamina
}