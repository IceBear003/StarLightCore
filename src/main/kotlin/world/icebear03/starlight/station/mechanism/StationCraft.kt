package world.icebear03.starlight.station.mechanism

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.serverct.parrot.parrotx.function.variable
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.spell.passive.recipe.registerShapedRecipe
import world.icebear03.starlight.station.station
import world.icebear03.starlight.utils.toRoman

object StationCraft {

    val crafts = mutableListOf<ItemStack>()

    fun initialize() {
        val item = ItemStack(Material.CAMPFIRE).modifyMeta<ItemMeta> {
            setDisplayName("§6驻扎篝火")
            lore = listOf(
                "§8| §7等级: &e{level}",
                "§7",
                "§8| §7注意: &c一旦放置新的篝火，旧的篝火将会失效",
                "§8| §7具体介绍和指导请看官方Wiki"
            )
        }
        for (i in 1..3) {
            val result = item.clone().variable("level", listOf(i.toRoman()))
            crafts += result
            val ingredients = when (i) {
                1 -> listOf('a' to Material.BARREL, 'b' to Material.COBBLESTONE, 'i' to Material.IRON_BLOCK)
                2 -> listOf('a' to Material.CHORUS_FLOWER, 'b' to Material.OBSIDIAN, 'i' to Material.NETHERITE_BLOCK)
                3 -> listOf('a' to Material.EXPERIENCE_BOTTLE, 'b' to Material.DRAGON_HEAD, 'i' to Material.NETHER_STAR)
                else -> continue
            }
            registerShapedRecipe(
                NamespacedKey.minecraft("station_$i"),
                result,
                listOf("aba", "bib", "aba"),
                *ingredients.toTypedArray()
            )
        }
    }

    @SubscribeEvent(EventPriority.MONITOR, ignoreCancelled = true)
    fun craft(event: CraftItemEvent) {
        val item = event.recipe.result
        val player = event.whoClicked as Player
        if (crafts.contains(item)) {
            val level = crafts.indexOf(item) + 1
            player.station().level = level
            event.currentItem = player.station().generateItem()
            player.sendMessage("§b繁星工坊 §7>> 驻扎等级已经变更为 §e${level.toRoman()}")
        }
    }
}