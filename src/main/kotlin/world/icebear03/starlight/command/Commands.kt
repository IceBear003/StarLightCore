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
    val career = commandCareer

    @CommandBody(permission = "starlight.op")
    val careerPoint = commandCareerPoint

    @CommandBody(permission = "starlight.op")
    val careerSignalItem = commandCareerSignalItem

    @CommandBody(permission = "starlight.op")
    val careerReset = commandCareerReset

    @CommandBody(permission = "starlight.op")
    val stamina = commandStamina

    @CommandBody(permission = "starlight.op")
    val station = commandStation

    @CommandBody(permission = "starlight.op")
    val stationStampReset = commandStationStampReset

    @CommandBody
    val escape = commandEscape
}