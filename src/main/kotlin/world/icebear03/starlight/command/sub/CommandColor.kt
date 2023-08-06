package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.tool.info.chat.ColorChooseUI


val commandColor = subCommand {
    execute<Player> { sender, _, _ ->
        ColorChooseUI.open(sender)
    }
}