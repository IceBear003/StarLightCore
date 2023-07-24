package world.icebear03.starlight.career.mechanism.set

import org.bukkit.Material
import world.icebear03.starlight.career.mechanism.limit.LimitType

object StructuralEngineer {
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