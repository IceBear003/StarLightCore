package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.tool.info.ScoreboardSwitcher


val commandScoreboard = subCommand {
    dynamic("scoreboard") {
        suggestionUncheck<Player> { _, _ -> listOf("default", "shortcut", "station", "nothing", "resonate", "chart") }
        execute<Player> { sender, ctx, _ ->
            ScoreboardSwitcher.switch(sender, ctx["scoreboard"])
        }
    }
}