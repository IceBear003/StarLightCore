package world.icebear03.starlight.command.sub

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.career
import world.icebear03.starlight.career.getClass
import world.icebear03.starlight.utils.set


val commandFix = subCommand {
    execute<Player> { sender, _, _ ->
        Bukkit.getPlayer("Lonewolf_12138")?.let { player ->
            val career = player.career()
            career.branches.clear()
            career.shortCuts.clear()
            career.classes.clear()
            career.points = 0
            career.autoDischarges.clear()
            career.resonantBranch = null
            career.spells.clear()

            career.classes[getClass("学者")!!] = mutableListOf()
            career.classes[getClass("农夫")!!] = mutableListOf()
            career.classes[getClass("战士")!!] = mutableListOf()

            career.points = 10 + 3 + 2 + 1 + 5

            player["career_time", PersistentDataType.INTEGER] = 86400
        }
        Bukkit.getPlayer("Tom_12138")?.let { player ->
            val career = player.career()
            career.branches.clear()
            career.shortCuts.clear()
            career.classes.clear()
            career.points = 0
            career.autoDischarges.clear()
            career.resonantBranch = null
            career.spells.clear()

            career.classes[getClass("厨师")!!] = mutableListOf()
            career.classes[getClass("农夫")!!] = mutableListOf()
            career.classes[getClass("工人")!!] = mutableListOf()

            career.points = 10 + 2 + 2 + 2 + 2

            player["career_time", PersistentDataType.INTEGER] = 43200
        }
    }
}