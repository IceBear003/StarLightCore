package world.icebear03.starlight.career.mechanism.entry.architect

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
import world.icebear03.starlight.career.data.getSkillLevel
import world.icebear03.starlight.career.data.hasEureka
import world.icebear03.starlight.career.mechanism.discharge.defineDischarge
import world.icebear03.starlight.career.mechanism.discharge.defineFinish
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.passive.limit.LimitType
import world.icebear03.starlight.utils.add
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside
import java.util.*


object FortressEngineerActive {

    val absorptionMap = mutableMapOf<UUID, Int>()

    val protecting = mutableMapOf<String, Pair<Location, Int>>()

    val obsidianWalls = mutableMapOf<UUID, List<Location>>()

    fun initialize() {
        "庇护寻求".defineDischarge { id, level ->
            val player = this
            val amount = getAbsorptionAmount(this, level)
            absorptionMap[this.uniqueId] = amount
            submit(delay = 20L * (level + 3)) {
                absorptionMap.remove(player.uniqueId)
            }
            "§a技能 ${id.display()} §7释放成功，接下来获得的伤害减少${amount}点"
        }
        "庇护寻求".defineFinish { _, _ ->
            absorptionMap.remove(this.uniqueId)
        }
        "铁壁力场".defineDischarge { id, level ->
            val player = this
            val range = 8 + 8 * level
            val key = player.location.clone() to range
            protecting[player.name] = key
            "§a技能 ${id.display()} §7释放成功，接下来${range}格内特定均会被保护"
        }
        "铁壁力场".defineFinish { _, _ ->
            protecting.remove(this.name)
        }

        "便携式切石机".defineDischarge { _, _ ->
            this.openInventory(Bukkit.createInventory(null, InventoryType.STONECUTTER, "便携式切石机"))
            null
        }

        "淬炼固化".defineDischarge skill@{ id, _ ->
            val trace = this.rayTraceBlocks(50.0, FluidCollisionMode.NEVER)
                ?: return@skill "§a技能 ${id.display()} §7释放成功，但是你没有指向§e50格§7内的地方"
            val block = trace.hitBlock
                ?: return@skill "§a技能 ${id.display()} §7释放成功，但是你没有指向§e50格§7内的地方"
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
            "§d顿悟 ${id.display()} §7释放成功，黑曜石墙将会在§e15秒§7后消失"
        }

        "淬炼固化".defineFinish { _, _ ->
            obsidianWalls[this.uniqueId]?.forEach {
                it.world!!.spawnParticle(Particle.BLOCK_CRACK, it.block.location, 1, it.block.blockData)
                it.block.type = Material.AIR
            }
            obsidianWalls.remove(this.uniqueId)
        }
    }

    fun getAbsorptionAmount(player: Player, level: Int): Int {
        var brickExtra = 0
        if (player.hasBlockAside(FortressEngineerSet.BRICKS.types, 4)) {
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
}

object FortressEngineerPassive {

    fun initialize() {
        submit(period = 100L) {
            onlinePlayers.forEach {
                if (it.world.environment == World.Environment.NETHER) {
                    if (it.hasEureka("下界开路者")) {
                        it.effect(PotionEffectType.FAST_DIGGING, 200, 1)
                    }
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

        val level = entity.getSkillLevel("缓冲装置")
        if (level in 0..3)
            event.damage = event.damage * (maxOf(0.4, 0.8 - 0.2 * level))
        if (level == 3 && Math.random() <= 0.2) {
            event.isCancelled = true
            return
        }

        if (entity.hasEureka("重力轰击")) {
            var flag = false
            entity.getNearbyEntities(2.0, 2.0, 2.0).filterIsInstance<Player>().forEach {
                it.damage(event.damage)
                flag = true
            }
            if (flag)
                entity.sendMessage("§a生涯系统 §7>> ${"重力轰击".display()} §7对周围玩家造成了§e${event.damage}点伤害")
        }
    }
}

enum class FortressEngineerSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    WALL_AND_IRON_FENCE(
        listOf(Material.IRON_BARS).also {
            Material.values().filter {
                val string = it.toString()
                string.endsWith("_WALL")
            }
        }, listOf(
            LimitType.CRAFT to ("堡垒工程师" to 0)
        )
    ),
    BRICKS(
        Material.values().filter {
            val string = it.toString()
            string.endsWith("BRICKS")
        }, listOf(
            LimitType.DROP_IF_BREAK to ("堡垒工程师" to 0)
        )
    ),
    SCAFFOLDING(
        listOf(Material.SCAFFOLDING),
        listOf(LimitType.PLACE to ("堡垒工程师" to 0))
    ),
    STONECURRTER(
        listOf(Material.STONECUTTER),
        listOf(LimitType.USE to ("堡垒工程师" to 0))
    )
}