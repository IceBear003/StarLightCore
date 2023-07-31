package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.shapelessRecipe

object Brewer {
    fun initialize() {
        addLimit(HandlerType.USE, "药剂师" to 0, Material.BREWING_STAND)

        Material.SUSPICIOUS_STEW.addLowestListener(HandlerType.CRAFT) { _, player, _ ->
            if (!player.meetRequirement("药剂师", 0) && Math.random() <= 0.5) {
                false to "合成谜之炖菜失败，解锁 §e职业分支 §7${display("药剂师")} §7可以提高成功率"
            } else true to null
        }

        listOf(Material.HOPPER, Material.DISPENSER, Material.DROPPER).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("药剂师", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(Material.BREWING_STAND, 6))
                return@addLowestListener false to "你不能在酿造台边放置这个方块，需要解锁 §e职业分支 ${display("药剂师")}"
            return@addLowestListener true to null
        }

        shapelessRecipe(
            NamespacedKey.minecraft("nether_native"),
            ItemStack(Material.POTION).modifyMeta<PotionMeta> {
                basePotionData = PotionData(PotionType.AWKWARD, false, false)
            },
            1 to Material.GLASS_BOTTLE,
            3 to Material.NETHER_WART
        ).addSpecialRecipe("下界原住民")

        "过量服用".discharge { _, _ ->
            activePotionEffects.toList().forEach { effect ->
                val type = effect.type
                val effectLevel = effect.amplifier + 2
                val duration = effect.duration / 20
                removePotionEffect(type)
                effect(type, duration, effectLevel)
            }
            "你过量服用了神秘的药水，使得目前所有药水效果等级§a+1"
        }.finish { _, _ ->
            activePotionEffects.toList().forEach { effect ->
                val type = effect.type
                val effectLevel = effect.amplifier
                val duration = effect.duration / 20
                if (effectLevel >= 1) {
                    removePotionEffect(type)
                    effect(type, duration, effectLevel)
                }
            }
            effect(PotionEffectType.POISON, 8, 2)
            "由于过量服用，你受到了很强的副作用"
        }

        shapelessRecipe(
            NamespacedKey.minecraft("brewer_in_war"),
            ItemStack(Material.SPLASH_POTION).modifyMeta<PotionMeta> {
                basePotionData = PotionData(PotionType.AWKWARD, false, false)
            },
            1 to Material.GLASS_BOTTLE,
            2 to Material.NETHER_WART,
            1 to Material.GUNPOWDER
        ).addSpecialRecipe("战地药剂师")

        "蒸发控件".discharge { _, _ ->
            "蒸发控件开始运行，接下来一分钟内周围酿造台酿造完成时有概率掉落烈焰粉"
        }.finish { _, _ ->
            "蒸发控件运行结束"
        }

        "肾上腺素注射".discharge { _, _ ->
            "肾上腺素已注射，接下来喷溅瞬间治疗药水时对所有受影响玩家赋予额外BUFF"
        }.finish { _, _ ->
            "肾上腺素药效已经结束"
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun brew(event: BrewEvent) {
        val loc = event.block.location
        val world = event.block.world

        world.players.filter { it.isDischarging("蒸发控件") }.forEach { player ->
            val spellLevel = player.spellLevel("蒸发控件")
            val range = if (spellLevel == 3) 3 else 2
            val rate = 0.1 * spellLevel
            if (player.location.distance(loc) <= range && Math.random() <= rate) {
                world.dropItemNaturally(loc, ItemStack(Material.BLAZE_POWDER))
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun splash(event: PotionSplashEvent) {
        val potion = event.potion
        val player = potion.shooter
        if (player !is Player)
            return
        if (!player.isDischarging("肾上腺素注射"))
            return

        val level = player.spellLevel("肾上腺素注射")
        val affected = event.affectedEntities
        val duration1 = when (level) {
            1 -> 8
            2 -> 12
            3 -> 20
            else -> 8
        }
        val level1 = if (level >= 2) 2 else 1
        affected.filterIsInstance<Player>().forEach {
            it.effect(PotionEffectType.FIRE_RESISTANCE, duration1, 1)
            it.effect(PotionEffectType.REGENERATION, 15 + 5 * level, level1)
            it.effect(PotionEffectType.ABSORPTION, 2 + level, level)
        }
        player.finish("肾上腺素注射")
    }
}