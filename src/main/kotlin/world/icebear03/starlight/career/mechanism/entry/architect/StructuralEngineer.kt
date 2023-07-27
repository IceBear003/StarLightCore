package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.data.getSpellLevel
import world.icebear03.starlight.career.data.hasEureka
import world.icebear03.starlight.career.mechanism.discharge.defineDischarge
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.passive.limit.LimitType
import world.icebear03.starlight.career.mechanism.passive.recipe.CraftHandler
import world.icebear03.starlight.career.mechanism.passive.recipe.addSpecialRecipe
import world.icebear03.starlight.career.mechanism.passive.recipe.registerShapelessRecipe
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside

object StructuralEngineerActive {
    fun initialize() {
        "识色敏锐".defineDischarge { id, _ ->
            "§a技能 ${id.display()} §7释放成功，下一次合成染色方块时会获得额外同种方块"
        }

        "凌空创想".defineDischarge { id, skillLevel ->
            val player = this
            val level = if (skillLevel == 3) 2 else 1
            val seconds = 60 * (2 + skillLevel)

            this.effect(PotionEffectType.SPEED, seconds, level)
            if (skillLevel == 3) {
                this.effect(PotionEffectType.DAMAGE_RESISTANCE, seconds)
            }

            var tot = 0
            submit(period = 20L) {
                tot += 1

                if (!player.isDischarging("凌空创想")) {
                    cancel()
                    return@submit
                }

                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.effect(PotionEffectType.SPEED, seconds - tot, level)
                }
                if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, seconds - tot, 0)
                }

                val tmp = when (skillLevel) {
                    2 -> listOf(2, 2, 2)
                    3 -> listOf(3, 3, 3)
                    else -> listOf(2, 1, 1)
                }

                if (player.hasBlockAside(Material.SCAFFOLDING, 1)) {
                    player.effect(PotionEffectType.SPEED, 3, tmp[0])
                    player.effect(PotionEffectType.JUMP, 3, tmp[1])
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, 3, tmp[2])
                }
            }

            "§a技能 ${id.display()} §7释放成功，即刻获得§f速度I§7，可利用脚手架获得更多增益"
        }

        CraftHandler.registerHigh(StructuralEngineerSet.DYED_BLOCK.types) { player, type ->
            val level = player.getSpellLevel("识色敏锐")
            if (player.isDischarging("识色敏锐")) {
                player.sendMessage("awa")
                val amount = if (level == 3) 2 else 1
                player.giveItem(ItemStack(type, amount))
                "§a生涯系统 §7>> 合成染色方块时获得了额外产物"
            } else null
        }
    }
}

object StructuralEngineerPassive {

    fun initialize() {
        DyeColor.values().forEach {
            val concreteType = Material.valueOf(it.toString() + "_CONCRETE")
            val powderType = Material.valueOf(it.toString() + "_CONCRETE_POWDER")
            val key = NamespacedKey.minecraft("concrete_special_" + it.toString().lowercase())

            registerShapelessRecipe(key, ItemStack(concreteType), 1 to powderType).addSpecialRecipe("凝固剂")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun breakBlock(event: BlockBreakEvent) {
        val player = event.player
        val type = event.block.type

        if (StructuralEngineerSet.DYED_BLOCK.types.contains(type)) {
            if (player.hasEureka("奇珍颜料")) {
                if (Math.random() <= 0.2) {
                    DyeColor.values().filter {
                        type.toString().contains(it.toString())
                    }.forEach {
                        player.giveItem(ItemStack(Material.valueOf(it.toString() + "_DYE")))
                    }
                    player.sendMessage("§a生涯系统 §7>> 破坏染色方块时获得了额外染料")
                }
            }
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
            Material.LOOM
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