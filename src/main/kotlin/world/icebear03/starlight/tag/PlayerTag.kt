package world.icebear03.starlight.tag

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.remove
import world.icebear03.starlight.utils.set

object PlayerTag {

    fun addTag(player: Player, name: String) {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        player["tags", PersistentDataType.STRING] = "$string$name,"
    }

    fun removeTag(player: Player, name: String) {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        player["tags", PersistentDataType.STRING] = string.replace("$name,", "")
    }

    fun clearTag(player: Player) {
        player.remove("current_tag")
    }

    fun tagList(player: Player): List<Tag> {
        val string = player["tags", PersistentDataType.STRING] ?: ","
        return string.split(",").filter { it.isNotBlank() }.map { TagLibrary.getTag(it)!! }
    }

    fun setTag(player: Player, name: String) {
        player["current_tag", PersistentDataType.STRING] = name
    }

    fun currentTag(player: Player): Tag? {
        return TagLibrary.getTag(player["current_tag", PersistentDataType.STRING] ?: "")
    }
}