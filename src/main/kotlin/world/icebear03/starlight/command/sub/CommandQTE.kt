package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.other.QTEProvider


val commandQTE = subCommand {
    dynamic("difficulty") {
        suggestionUncheck<Player> { _, _ -> QTEProvider.QTEDifficulty.values().map { it.name } }
        dynamic("type") {
            suggestionUncheck<Player> { _, _ -> QTEProvider.QTEType.values().map { it.name } }
            execute<Player> { sender, ctx, _ ->
                QTEProvider.sendQTE(sender, QTEProvider.QTEDifficulty.valueOf(ctx["difficulty"]),
                    QTEProvider.QTEType.valueOf(ctx["type"]), { suc ->
                        sendMessage(suc.toString())
                    })
            }
        }
    }
}