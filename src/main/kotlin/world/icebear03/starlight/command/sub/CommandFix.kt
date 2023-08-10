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
        Bukkit.getPlayer("xiaozhu_zty")?.let { player ->
            val career = player.career()

            career.classes.remove(getClass("学者")!!)
            career.classes.remove(getClass("农夫")!!)
            career.classes[getClass("厨师")!!] = mutableListOf()
            career.classes[getClass("建筑师")!!] = mutableListOf()

            player["career_time", PersistentDataType.INTEGER] = 86400
        }
    }
}