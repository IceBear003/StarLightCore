package world.icebear03.starlight.tag

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import taboolib.module.chat.colored
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.set
import world.icebear03.starlight.utils.skull

data class Tag(
    val id: String,
    val display: String,
    val description: List<String>,
    val skull: String,
    val owner: String? = null,
    val activity: String? = null
) {
    fun icon(): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        item.skull(skull)
        return item.modifyMeta<ItemMeta> {
            this["tag", PersistentDataType.STRING] = id
            setDisplayName(display.colored())
            lore = description.map { "§8| §7$it" } +
                    (if (owner != null) listOf("§7", "§8| §7专属: §e$owner")
                    else listOf()) +
                    (if (activity != null) listOf("§7", "§8| §7活动: §b$activity")
                    else listOf())
        }
    }
}