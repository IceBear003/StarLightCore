package world.icebear03.starlight.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.expansion.createHelper
import world.icebear03.starlight.command.sub.commandCareerMenu
import world.icebear03.starlight.command.sub.commandCareerPoint

@CommandHeader(name = "starlightcore", aliases = ["sl", "slc"])
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper(checkPermissions = true)
    }

    @CommandBody
    val careerMenu = commandCareerMenu

    @CommandBody(permission = "starlightcore.career.point")
    val point = commandCareerPoint
}