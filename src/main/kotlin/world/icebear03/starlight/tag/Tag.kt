package world.icebear03.starlight.tag

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.function.textured
import taboolib.module.chat.colored
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.set

data class Tag(
    val name: String,
    val color: String,
    val description: List<String>,
    val skull: String
) {
    fun display(): String {
        return color.colored() + name
    }

    fun icon(): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        item.textured(skull)
        return item.modifyMeta<ItemMeta> {
            this["tag", PersistentDataType.STRING] = name
            setDisplayName(display())
            lore = description.map { "§8| §7$it" }
        }
    }
}