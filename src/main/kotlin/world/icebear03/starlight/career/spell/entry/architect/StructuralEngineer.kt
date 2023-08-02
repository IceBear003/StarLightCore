package world.icebear03.starlight.career.spell.entry.architect

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
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapelessRecipe
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging

object StructuralEngineer {


    val decorationBlocks = listOf(
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
    )
    val dyedBlocks = Material.values().filter {
        val string = it.toString()
        (string.contains("_WOOL") && it != Material.WHITE_WOOL) ||
                string.contains("_CONCRETE") ||
                string.contains("_CONCRETE_POWDER") ||
                (string.contains("_TERRACOTTA") && it != Material.TERRACOTTA)
    }
    val glassBlocks = Material.values().filter {
        val string = it.toString()
        string.contains("_GLASS") ||
                string.contains("_GLASS_PANE") ||
                it == Material.TINTED_GLASS ||
                it == Material.GLASS ||
                it == Material.GLASS_PANE
    }

    fun initialize() {
        addLimit(HandlerType.CRAFT, "结构工程师" to 0, *decorationBlocks.toTypedArray())
        addLimit(HandlerType.PLACE, "结构工程师" to 0, *decorationBlocks.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "高效回收" to 1, *decorationBlocks.toTypedArray())
        addLimit(HandlerType.CRAFT, "结构工程师" to 0, *dyedBlocks.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "高效回收" to 2, *dyedBlocks.toTypedArray())
        addLimit(HandlerType.CRAFT, "结构工程师" to 0, *glassBlocks.toTypedArray())
        addLimit(HandlerType.CRAFT, "结构工程师" to 0, *glassBlocks.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "高效回收" to 3, *glassBlocks.toTypedArray())
        addLimit(
            HandlerType.CRAFT, "结构工程师" to 0, *listOf(
                Material.LANTERN,
                Material.SOUL_LANTERN,
                Material.BARRIER,
                Material.ENDER_CHEST,
                Material.ITEM_FRAME,
                Material.GLOW_ITEM_FRAME,
                Material.BOOKSHELF
            ).toTypedArray()
        )
        addLimit(HandlerType.PLACE, "结构工程师" to 0, Material.SCAFFOLDING)
        addLimit(HandlerType.USE, "结构工程师" to 0, Material.LOOM)

        DyeColor.values().forEach {
            val concreteType = Material.valueOf(it.toString() + "_CONCRETE")
            val powderType = Material.valueOf(it.toString() + "_CONCRETE_POWDER")
            val key = NamespacedKey.minecraft("career_concrete_" + it.toString().lowercase())
            shapelessRecipe(key, ItemStack(concreteType), 1 to powderType).addSpecialRecipe("凝固剂")
        }

        "识色敏锐".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下一次合成染色方块时会获得额外同种方块"
        }

        "凌空创想".discharge { name, skillLevel ->
            val level = if (skillLevel == 3) 2 else 1
            val seconds = 60 * (2 + skillLevel)

            this.effect(PotionEffectType.SPEED, seconds, level)
            if (skillLevel == 3) {
                this.effect(PotionEffectType.DAMAGE_RESISTANCE, seconds)
            }

            var tot = 0
            submit(period = 20L) {
                tot += 1

                if (!isDischarging("凌空创想")) {
                    cancel()
                    return@submit
                }

                if (!hasPotionEffect(PotionEffectType.SPEED)) {
                    effect(PotionEffectType.SPEED, seconds - tot, level)
                }
                if (!hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    effect(PotionEffectType.DAMAGE_RESISTANCE, seconds - tot, 0)
                }

                val tmp = when (skillLevel) {
                    2 -> listOf(2, 2, 2)
                    3 -> listOf(3, 3, 3)
                    else -> listOf(2, 1, 1)
                }

                if (hasBlockAside(Material.SCAFFOLDING, 1)) {
                    effect(PotionEffectType.SPEED, 3, tmp[0])
                    effect(PotionEffectType.JUMP, 3, tmp[1])
                    effect(PotionEffectType.DAMAGE_RESISTANCE, 3, tmp[2])
                }
            }

            "§a技能 ${display(name)} §7释放成功，即刻获得§f速度I§7，可利用脚手架获得更多增益"
        }

        dyedBlocks.addHighListener(HandlerType.CRAFT) { _, player, type ->
            val level = player.spellLevel("识色敏锐")
            if (player.isDischarging("识色敏锐")) {
                player.finish("识色敏锐")
                val amount = if (level == 3) 2 else 1
                player.giveItem(ItemStack(type, amount))
                "§a生涯系统 §7>> 合成染色方块时获得了额外产物"
            } else null
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun breakBlock(event: BlockBreakEvent) {
        val player = event.player
        val type = event.block.type

        if (dyedBlocks.contains(type) && player.meetRequirement("奇珍颜料")) {
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