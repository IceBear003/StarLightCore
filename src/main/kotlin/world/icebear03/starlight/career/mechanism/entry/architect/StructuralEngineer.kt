package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.mechanism.discharge.DischargeHandler
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.limit.LimitType

object StructuralEngineerActive {
    init {
        DischargeHandler.dischargeMap["识色敏锐"] = { id, _ ->
            "技能 ${id.display()} &r释放成功，下一次合成染色方块时会获得额外同种方块"
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: CraftItemEvent) {
        val item = event.recipe.result
        val type = item.type

        if (!StructuralEngineerSet.DYED_BLOCK.types.contains(type))
            return

        val player = event.whoClicked as Player
        val level = player.getSkillLevel("识色敏锐")

        if (player.isDischarging("识色敏锐", level)) {
            val amount = when (level) {
                1, 2 -> 1
                else -> 2
            }
            player.giveItem(ItemStack(type, amount))
        }
    }
}

enum class StructuralEngineerSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    DECORATION_BLOCK(
        listOf(
            Material.CLOCK,
            Material.FLETCHING_TABLE,
            Material.JUKEBOX,
            Material.LECTERN,
            Material.LODESTONE,
            Material.RESPAWN_ANCHOR,
            Material.END_ROD,
            Material.OCHRE_FROGLIGHT,
            Material.VERDANT_FROGLIGHT,
            Material.PEARLESCENT_FROGLIGHT,
            Material.LOOM,
            Material.BEACON
        ), listOf(
            LimitType.CRAFT to ("结构工程师" to 0),
            LimitType.PLACE to ("结构工程师" to 0),
            LimitType.DROP_IF_BREAK to ("高效回收" to 1)
        )
    ),
    DYED_BLOCK(
        Material.values().filter {
            val string = it.toString()
            (string.contains("_WOOL") && it != Material.WHITE_WOOL) ||
                    string.contains("_CONCRETE") ||
                    string.contains("_CONCRETE_POWDER") ||
                    (string.contains("_TERRACOTTA") && it != Material.TERRACOTTA)
        }, listOf(
            LimitType.CRAFT to ("结构工程师" to 0),
            LimitType.DROP_IF_BREAK to ("高效回收" to 2)
        )
    ),
    SINTER_GLASSES(
        Material.values().filter {
            val string = it.toString()
            string.contains("_GLASS") ||
                    string.contains("_GLASS_PANE") ||
                    it == Material.TINTED_GLASS ||
                    it == Material.GLASS ||
                    it == Material.GLASS_PANE
        }, listOf(
            LimitType.CRAFT to ("结构工程师" to 0),
            LimitType.SINTER to ("结构工程师" to 0),
            LimitType.DROP_IF_BREAK to ("高效回收" to 3)
        )
    ),
    OTHER(
        listOf(
            Material.LANTERN,
            Material.SOUL_LANTERN,
            Material.BARRIER,
            Material.ENDER_CHEST,
            Material.ITEM_FRAME,
            Material.GLOW_ITEM_FRAME,
            Material.BOOKSHELF
        ).also {
            Material.values().filter {
                val string = it.toString()
                string.endsWith("BANNER") ||
                        string.endsWith("CARPET")
            }
        }, listOf(LimitType.CRAFT to ("结构工程师" to 0))
    ),
    SCAFFOLDING(
        listOf(Material.SCAFFOLDING),
        listOf(LimitType.PLACE to ("结构工程师" to 0))
    ),
    LOOM(
        listOf(Material.LOOM),
        listOf(LimitType.USE to ("结构工程师" to 0))
    )
}