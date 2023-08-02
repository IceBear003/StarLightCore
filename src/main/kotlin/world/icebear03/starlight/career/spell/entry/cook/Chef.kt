package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapedRecipe
import world.icebear03.starlight.utils.cooldownStamps
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object Chef {
    fun initialize() {
        addLimit(HandlerType.USE, "主厨" to 0, Material.SMOKER)

        listOf(Material.HOPPER, Material.DISPENSER, Material.DROPPER).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("主厨", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(Material.SMOKER, 6))
                return@addLowestListener false to "你不能在烟熏炉边放置这个方块，需要解锁 §e职业分支 ${display("主厨")}"
            return@addLowestListener true to null
        }

        addLimit(HandlerType.CRAFT, "主厨" to 0, Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.GLISTERING_MELON_SLICE)

        shapedRecipe(
            NamespacedKey.minecraft("career_enchanted_apple"),
            ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            listOf("aaa", "aba", "aaa"),
            'a' to Material.GOLD_BLOCK,
            'b' to Material.APPLE
        ).addSpecialRecipe("配方传承", 1)

        "地狱厨房".discharge { name, _ ->
            var amount = 0
            val list = getNearbyEntities(8.0, 8.0, 8.0).filter {
                it is Player && it.career().hasClass("厨师")
            }.toMutableList()
            list += player
            list.forEach { player ->
                amount += 1
                val map = cooldownStamps[player.uniqueId] ?: return@forEach
                map.toMap().forEach spells@{ (spellName, stamp) ->
                    val spell = getSpell(spellName) ?: return@spells
                    if (!spell.isEureka)
                        map[spellName] = stamp - 1000 * 20
                }
                if (player.uniqueId != uniqueId)
                    player.sendMessage("§a生涯系统 §7>> §e${this.name} §7使用 ${display(name)} §7让你的技能冷却§a-20s")
            }
            finish(name)
            "${display(name)} §7运作中，为周围§a${amount}个§7玩家减少了技能冷却"
        }.finish { _, _ ->
            null
        }

        "奇味异珍".discharge { name, _ ->
            "${display(name)} §7使得下一次食用某些水果时触发额外增益"
        }
        "镀金美馔".discharge { name, _ ->
            "${display(name)} §7使得下一次食用某些镀金食物时触发额外增益"
        }

        Material.values().filter { it.isEdible }.addHighListener(HandlerType.SINTER) { event, player, type ->
            val level = player.spellLevel("精准火候")
            val rate = level * 0.1
            var amount = 0
            val sinterEvent = event as InventoryClickEvent
            val originAmount = sinterEvent.currentItem!!.amount

            for (i in 1..originAmount)
                if (Math.random() <= rate)
                    amount += 1

            if (amount != 0) {
                player.giveItem(ItemStack(type, amount))
                "${display("精准火候")} §7使得烧炼获得额外§a${amount}个§7产物"
            } else null
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

        if (player.isDischarging("奇味异珍")) {
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
        if (player.isDischarging("镀金美馔")) {
            val level = player.spellLevel("镀金美馔")
            if (level >= 1) {
                if (type == Material.GOLDEN_CARROT) {
                    player.getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<Player>().forEach { other ->
                        other.foodLevel = minOf(other.foodLevel + 4, 20)
                        other.sendMessage("§a生涯系统 §7>> §e${player.name} §7使用 ${display("镀金美馔")} §7让你的饱食度§a+4")
                    }
                    player.foodLevel = minOf(player.foodLevel + 4, 20)
                    player.finish("镀金美馔")
                }
            }
            if (level >= 2) {
                if (type == Material.GOLDEN_APPLE) {
                    player.getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<Player>().forEach { other ->
                        other.health = minOf(other.health + 2, 20.0)
                        other.sendMessage("§a生涯系统 §7>> §e${player.name} §7使用 ${display("镀金美馔")} §7让你的生命值§a+2")
                    }
                    player.health = minOf(player.health + 2, 20.0)
                    player.finish("镀金美馔")
                }
            }
            if (level >= 3) {
                if (type == Material.ENCHANTED_GOLDEN_APPLE) {
                    player.getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<Player>().forEach { other ->
                        other.foodLevel = minOf(other.foodLevel + 6, 20)
                        other.health = minOf(other.health + 8, 20.0)
                        other.effect(PotionEffectType.FIRE_RESISTANCE, 30, 1)
                        other.effect(PotionEffectType.DAMAGE_RESISTANCE, 30, 1)
                        other.sendMessage("§a生涯系统 §7>> §e${player.name} §7使用 ${display("镀金美馔")} §7让你获得大量增益")
                    }
                    player.foodLevel = minOf(player.foodLevel + 6, 20)
                    player.health = minOf(player.health + 8, 20.0)
                    player.finish("镀金美馔")
                }
            }
        }
    }
}