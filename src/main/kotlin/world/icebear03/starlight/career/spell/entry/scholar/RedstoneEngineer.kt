package world.icebear03.starlight.career.spell.entry.scholar

import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapelessRecipe
import world.icebear03.starlight.utils.*

object RedstoneEngineer {

    val machines = listOf(
        Material.DAYLIGHT_DETECTOR,
        Material.OBSERVER,
        Material.SCULK_SENSOR,
        Material.TRAPPED_CHEST,
        Material.COMPARATOR,
        Material.REPEATER,
        Material.DISPENSER,
        Material.DROPPER,
        Material.PISTON,
        Material.STICKY_PISTON
    )

    fun initialize() {
        addLimit(HandlerType.CRAFT, "红石工程师" to 0, *machines.toTypedArray())
        addLimit(HandlerType.PLACE, "红石工程师" to 0, *machines.toTypedArray())
        addLimit(HandlerType.CRAFT, "红石工程师" to 0, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL)
        addLimit(HandlerType.PLACE, "红石工程师" to 0, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL)
        addLimit(HandlerType.DROP_IF_BREAK, "红石工程师" to 0, Material.REDSTONE_WIRE, *machines.toTypedArray())

        Material.REDSTONE.addHighListener(HandlerType.PLACE) { _, player, type ->
            val level = player.spellLevel("电路设计")
            if (level > 0 && Math.random() <= 0.02 + 0.02 * level) {
                player.giveItem(ItemStack(type))
                return@addHighListener "${display("电路设计")} §7使得本次放置不消耗红石粉"
            }
            null
        }
        machines.addHighListener(HandlerType.CRAFT) { _, player, type ->
            if (player.isDischarging("自动化生产")) {
                val level = player.spellLevel("自动化生产")
                if (Math.random() <= 0.075 + 0.075 * level) {
                    player.giveItem(ItemStack(type))
                    return@addHighListener "${display("自动化生产")} §7使得本次合成额外获得了产物"
                }
                player.finish("自动化生产")
            }
            null
        }

        "自动化生产".discharge { name, _ ->
            "${display(name)} §7释放成功，下次合成高级红石机械是有概率获得额外产物"
        }

        "超载".discharge spell@{ name, _ ->
            val wires = location.getBlockAside(8, Material.REDSTONE_WIRE)
            submit(delay = 10L, period = 20L) {
                if (isDischarging(name)) {
                    wires.forEach {
                        val loc = it.add(0.5, 0.5, 0.5)
                        world.spawnParticle(Particle.REDSTONE, loc, 3, Particle.DustOptions(Color.RED, 1.0f))
                        world.players.forEach players@{ other ->
                            if (other.uniqueId == uniqueId)
                                return@players
                            if (other.location.verticalDistance(loc) <= 1.5 && other.location.horizontalDistance(loc) <= 0.5) {
                                other.damage(1.0, this@spell)
                            }
                        }
                    }
                } else cancel()
            }
            "${display(name)} §7释放成功，周围的红石粉进入超载状态"
        }

        shapelessRecipe(
            NamespacedKey.minecraft("redstone_engineer_torch"),
            ItemStack(Material.REDSTONE_TORCH),
            1 to Material.TORCH
        )

        "定点脉冲".discharge { name, _ ->
            finish(name)
            if (takeItem(16) { it.isSimilar(ItemStack(Material.REDSTONE)) }) {
                val locations = location.getBlockAside(4, *machines.toTypedArray())
                locations.forEach {
                    it.block.breakNaturally()
                }
                "${display(name)} §7释放成功，破坏了周围§a${locations.size}个§7红石机械"
            } else {
                "${display(name)} §7释放失败，因为没有足够的红石粉"
            }
        }

        "雷霆之杖".discharge { name, _ ->
            finish(name)
            val item = inventory.itemInMainHand
            if (item.type == Material.LIGHTNING_ROD) {
                val trace = rayTraceBlocks(50.0, FluidCollisionMode.NEVER)
                val block = trace?.hitBlock ?: return@discharge "${display(name)} §7释放失败，因为没有指向§a50格§7内的地方"
                if (item.amount == 1) inventory.setItemInMainHand(ItemStack(Material.AIR))
                else item.amount -= 1
                var rate = 0.1
                if (!world.isClearWeather)
                    rate = 0.2
                if (world.isThundering)
                    rate = 0.33
                if (Math.random() <= rate) {
                    world.strikeLightning(block.location)
                    "${display(name)} §7释放成功，在指针出引发了一道雷电"
                } else
                    "${display(name)} §7释放失败，貌似这次没有引发雷电"
            } else {
                "${display(name)} §7释放失败，因为手里没有避雷针"
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun attack(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager !is Player)
            return
        val entity = event.entity
        if (entity !is LivingEntity)
            return
        if (damager.meetRequirement("红色炬火")) {
            if (damager.inventory.itemInMainHand.type == Material.REDSTONE_TORCH && Math.random() <= 0.25) {
                entity.fireTicks = 200
            }
        }
    }
}