package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.hasEureka
import world.icebear03.starlight.career.mechanism.discharge.defineDischarge
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.limit.LimitType
import world.icebear03.starlight.utils.effect

object StructuralEngineerActive {
    init {
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
                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.effect(PotionEffectType.SPEED, seconds - tot, level)
                }
                if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, seconds - tot, 0)
                }

                val tmp = when (skillLevel) {
                    2 -> listOf(2, 1, 1)
                    3 -> listOf(2, 2, 2)
                    else -> listOf(3, 3, 3)
                }

                if (player.location.block.type == Material.SCAFFOLDING) {
                    player.effect(PotionEffectType.SPEED, 3, tmp[0])
                    player.effect(PotionEffectType.JUMP, 3, tmp[1])
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, 3, tmp[2])
                }
            }

            "§a技能 ${id.display()} §7释放成功，即刻获得§f速度I§7，可利用脚手架获得更多增益"
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
            player.sendMessage("§a生涯系统 §7>> 合成染色方块时获得了额外产物")
        }
    }
}

object StructuralEngineerPassive {

    val specialRecipes = mutableListOf<NamespacedKey>()

    init {
        DyeColor.values().forEach {
            val concreteType = Material.valueOf(it.toString() + "_CONCRETE")
            val powderType = Material.valueOf(it.toString() + "_CONCRETE_POWDER")

            val key = NamespacedKey.minecraft("concrete_special_" + it.toString().lowercase())
            val recipe = ShapelessRecipe(key, ItemStack(concreteType))
            recipe.addIngredient(powderType)

            Bukkit.removeRecipe(key)
            Bukkit.addRecipe(recipe)
            specialRecipes += key
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun craftItem(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val recipe = event.recipe
        val type = recipe.result.type

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun craftSpecialConcrete(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val recipe = event.recipe
        if (recipe !is ShapelessRecipe)
            return

        if (specialRecipes.contains(recipe.key)) {
            if (!player.hasEureka("凝固剂")) {
                event.isCancelled = true
                player.closeInventory()
                player.sendMessage("§a生涯系统 §7>> 必须激活§d顿悟 " + "凝固剂".display() + " §7才可以使用此特殊合成")
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