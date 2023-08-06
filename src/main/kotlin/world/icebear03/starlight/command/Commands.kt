package world.icebear03.starlight.command

import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import world.icebear03.starlight.command.sub.*
import world.icebear03.starlight.utils.performAsOp

@CommandHeader(name = "starlightcore", aliases = ["sl", "slc", "cd"], permissionDefault = PermissionDefault.TRUE)
object Commands {

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val main = mainCommand {
        execute<Player> { sender, _, _ ->
            sender.performAsOp("bs 主菜单")
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val help = subCommand {
        createHelper(checkPermissions = true)
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val career = commandCareer

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val point = commandPoint

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

    @CommandBody(permission = "starlight.op")
    val cooldownReset = commandCooldownReset

    @CommandBody(permission = "starlight.op")
    val qte = commandQTE

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val escape = commandEscape

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val scoreboard = commandScoreboard

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val recipe = commandRecipe

    @CommandBody(permission = "starlight.op")
    val tag = commandTag
}