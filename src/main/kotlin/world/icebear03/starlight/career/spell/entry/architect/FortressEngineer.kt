package world.icebear03.starlight.career.spell.entry.architect

import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.add
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging
import java.util.*

object FortressEngineer {

    val absorptionMap = mutableMapOf<UUID, Int>()

    val protecting = mutableMapOf<String, Pair<Location, Int>>()

    val obsidianWalls = mutableMapOf<UUID, List<Location>>()

    val bricks = Material.values().filter { it.toString().endsWith("_BRICKS") }
    val walls = Material.values().filter { it.toString().endsWith("_WALL") }
    fun initialize() {
        addLimit(HandlerType.CRAFT, "堡垒工程师" to 0, *walls.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "堡垒工程师" to 0, *bricks.toTypedArray())
        addLimit(HandlerType.PLACE, "堡垒工程师" to 0, Material.SCAFFOLDING)
        addLimit(HandlerType.USE, "堡垒工程师" to 0, Material.STONECUTTER)

        submit(period = 20L) {
            onlinePlayers.forEach {
                if (it.world.environment == World.Environment.NETHER) {
                    if (it.meetRequirement("下界开路者")) {
                        it.effect(PotionEffectType.FAST_DIGGING, 2, 1)
                    }
                }
            }
        }

        "庇护寻求".discharge { name, level ->
            val amount = getAbsorptionAmount(this, level)
            absorptionMap[this.uniqueId] = amount
            submit(delay = 20L * (level + 3)) {
                absorptionMap.remove(uniqueId)
            }
            "§a技能 ${display(name)} §7释放成功，接下来获得的伤害减少${amount}点"
        }.finish { _, _ ->
            absorptionMap.remove(uniqueId)
            null
        }

        "铁壁力场".discharge { name, level ->
            val range = 8 + 8 * level
            val key = location.clone() to range
            protecting[this.name] = key
            "§a技能 ${display(name)} §7释放成功，接下来${range}格内特定均会被保护"
        }.finish { _, _ ->
            protecting.remove(name)
            null
        }

        "便携式切石机".discharge { name, _ ->
            finish(name)
            this.openInventory(Bukkit.createInventory(null, InventoryType.STONECUTTER, "便携式切石机"))
            null
        }

        "淬炼固化".discharge skill@{ name, _ ->
            val trace = this.rayTraceBlocks(50.0, FluidCollisionMode.NEVER)
                ?: return@skill "§a技能 ${display(name)} §7释放成功，但是你没有指向§e50格§7内的地方"
            val block = trace.hitBlock
                ?: return@skill "§a技能 ${display(name)} §7释放成功，但是你没有指向§e50格§7内的地方"
            val loc = block.location
            val tmp = listOf(-1 to 0, 1 to 0, 0 to 1, 0 to -1)
            val blocks = mutableListOf<Location>()
            val thisTime = loc.clone()

            var tot = 0
            submit(period = 10L) {
                tot += 1
                if (tot > 8) {
                    cancel()
                    return@submit
                }
                tmp.forEach {
                    for (y in 1..3) {
                        val current = thisTime.clone()
                        current.add(it.first * tot, y, it.second * tot)
                        val currentBlock = current.block
                        if (currentBlock.type == Material.AIR) {
                            currentBlock.type = Material.OBSIDIAN
                            blocks += current
                        }
                    }
                }

            }
            obsidianWalls[this.uniqueId] = blocks
            "§d顿悟 ${display(name)} §7释放成功，黑曜石墙将会在§e15秒§7后消失"
        }.finish { _, _ ->
            obsidianWalls[this.uniqueId]?.forEach {
                it.block.type = Material.AIR
            }
            obsidianWalls.remove(this.uniqueId)
            null
        }
    }

    fun getAbsorptionAmount(player: Player, level: Int): Int {
        var brickExtra = 0
        if (player.hasBlockAside(bricks, 4)) {
            brickExtra = if (level == 3) 2 else 1
        }
        return brickExtra + level + 1
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damaged(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is Player)
            return
        if (entity.isDischarging("庇护寻求")) {
            val damage = event.damage - absorptionMap[entity.uniqueId]!!
            if (damage <= 0)
                event.damage = 0.1
            else
                event.damage = damage
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun breakBrick(event: BlockBreakEvent) {
        val player = event.player
        val loc = event.block.location
        val type = event.block.type

        if (bricks.contains(type) || walls.contains(type) || type == Material.IRON_BARS)
            protecting.forEach { (protector, pair) ->
                val centre = pair.first
                if (loc.world == centre.world) {
                    if (loc.distance(centre) <= pair.second) {
                        event.isCancelled = true
                        player.sendMessage("§a生涯系统 §7>> 你不能破坏这个方块，它正被§e${protector}§7保护着")
                        return
                    }
                }
            }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity !is Player)
            return

        if (event.cause != EntityDamageEvent.DamageCause.FALL)
            return

        val level = entity.spellLevel("缓冲装置")
        if (level in 0..3)
            event.damage = event.damage * (maxOf(0.4, 0.8 - 0.2 * level))
        if (level == 3 && Math.random() <= 0.2) {
            event.isCancelled = true
            return
        }

        if (entity.meetRequirement("重力轰击")) {
            var flag = false
            entity.getNearbyEntities(2.0, 2.0, 2.0).filterIsInstance<Player>().forEach {
                it.damage(event.damage)
                flag = true
            }
            if (flag)
                entity.sendMessage("§a生涯系统 §7>> ${display("重力轰击")} §7对周围玩家造成了§e${event.damage}点伤害")
        }
    }
}