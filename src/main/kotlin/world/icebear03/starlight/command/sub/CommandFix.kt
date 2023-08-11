package world.icebear03.starlight.command.sub

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.career
import world.icebear03.starlight.career.getClass
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set


val commandFix = subCommand {
    dynamic("mode") {
        suggestionUncheck<Player> { _, _ -> listOf("Sherlock_Holms", "dabaodabao") }
        execute<Player> { sender, ctx, _ ->
            when (ctx["mode"]) {
                "Sherlock_Holms" -> {
                    Bukkit.getPlayer("Sherlock_Holms")?.let { player ->
                        player["career_time", PersistentDataType.INTEGER] =
                            5 * 3600 + (player["career_time", PersistentDataType.INTEGER] ?: 0)
                    }
                }

                "dabaodabao" -> {
                    Bukkit.getPlayer("dabaodabao")?.let { player ->
                        val career = player.career()
                        career.remake(false)
                        career.points = 4
                        career.classes.clear()
                        career.classes[getClass("学者")!!] = mutableListOf()
                        career.classes[getClass("工人")!!] = mutableListOf()
                        career.classes[getClass("建筑师")!!] = mutableListOf()
                    }
                }
            }
        }
    }
}