package world.icebear03.starlight.tag

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.remove
import world.icebear03.starlight.utils.set

object PlayerTag {

    fun addTag(player: Player, id: String) {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        player["tags", PersistentDataType.STRING] = "$string$id,"
    }

    fun removeTag(player: Player, id: String) {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        player["tags", PersistentDataType.STRING] = string.replace("$id,", "")
    }

    fun clearTag(player: Player) {
        player.remove("current_tag")
    }

    fun tagList(player: Player): List<Tag> {
        val string = player["tags", PersistentDataType.STRING] ?: ","
        return string.split(",").filter { it.isNotBlank() }.map { TagLibrary.getTag(it)!! }
    }

    fun setTag(player: Player, id: String) {
        player["current_tag", PersistentDataType.STRING] = id
    }

    fun currentTag(player: Player): Tag? {
        return TagLibrary.getTag(player["current_tag", PersistentDataType.STRING] ?: "")
    }
}