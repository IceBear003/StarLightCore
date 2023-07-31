package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.cooldownStamps
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.shapedRecipe

object Chef {
    fun initialize() {
        addLimit(HandlerType.USE, "主厨" to 0, Material.SMOKER)

        listOf(Material.HOPPER, Material.DISPENSER).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("主厨", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(Material.SMOKER, 6))
                return@addLowestListener false to "你不能在烟熏炉边放置这个方块，需要解锁 §e职业分支 ${display("主厨")}"
            return@addLowestListener true to null
        }

        addLimit(HandlerType.CRAFT, "主厨" to 0, Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.GLISTERING_MELON_SLICE)

        shapedRecipe(
            NamespacedKey.minecraft("enchant_apple"),
            ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            listOf("aaa", "aba", "aaa"),
            'a' to Material.GOLD_BLOCK,
            'b' to Material.APPLE
        ).addSpecialRecipe("配方传承", 1)

        "地狱厨房".discharge { name, _ ->
            var amount = 0
            getNearbyEntities(8.0, 8.0, 8.0).filter {
                it is Player && it.career().hasClass("厨师")
            }.also { listOf(this) }.forEach { player ->
                amount += 1
                val map = cooldownStamps[player.uniqueId] ?: return@forEach
                map.toMap().forEach spells@{ (spellName, stamp) ->
                    val spell = getSpell(spellName) ?: return@spells
                    if (!spell.isEureka)
                        map[spellName] = stamp - 1000 * 20
                }
            }
            finish(name)
            "地狱厨房运作中，为周围${amount}个玩家减少了技能冷却"
        }.finish { _, _ ->
            null
        }

        "奇味异珍".discharge { name, _ ->
            "${display(name)} 使得下一次食用某些水果时触发额外增益"
        }
        "镀金美馔".discharge { name, _ ->
            "${display(name)} 使得下一次食用某些镀金食物时触发额外增益"
        }
    }


    @SubscribeEvent
    fun eat(event: FoodLevelChangeEvent) {
        val item = event.item ?: return
        val type = item.type
        val player = event.entity as Player
        if (player.meetRequirement("主厨", 0)) {
            event.foodLevel += 1
        }
        if (type == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE) {
            event.foodLevel = 20
        }

        if (player.meetRequirement("奇味异珍")) {
            val level = player.spellLevel("奇味异珍")
            if (level >= 1) {
                if (type == Material.CHORUS_FRUIT) {
                    event.foodLevel += 4
                    val teleported = player.getNearbyEntities(8.0, 8.0, 8.0).filterIsInstance<Player>().random()
                    submit(delay = 1L) {
                        teleported.teleport(player)
                    }
                    player.finish("奇味异珍")
                }
            }
            if (level >= 2) {
                if (type == Material.GLOW_BERRIES) {
                    event.foodLevel += 2
                    player.effect(PotionEffectType.GLOWING, 10, 1)
                    player.effect(PotionEffectType.SPEED, 10, 2)
                    player.finish("奇味异珍")
                }
            }
            if (level >= 3) {
                if (type == Material.POISONOUS_POTATO) {
                    event.foodLevel += 2
                    player.giveItem(ItemStack(Material.POTATO, 3))
                    submit {
                        player.removePotionEffect(PotionEffectType.POISON)
                    }
                    player.finish("奇味异珍")
                }
            }
        }
        if (player.meetRequirement("镀金美馔")) {
            val level = player.spellLevel("镀金美馔")
            if (level >= 1) {
                if (type == Material.GOLDEN_CARROT) {
                    player.finish("镀金美馔")
                }
            }
            if (level >= 2) {
                if (type == Material.GOLDEN_APPLE) {
                    player.finish("镀金美馔")
                }
            }
            if (level >= 3) {
                if (type == Material.ENCHANTED_GOLDEN_APPLE) {
                    player.finish("镀金美馔")
                }
            }
        }
    }
}