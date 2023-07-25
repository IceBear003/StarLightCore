package world.icebear03.starlight.career.mechanism.set.architect

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.mechanism.limit.LimitType

object FortressEngineerPassive {
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
        if (level == 3 && Math.random() <= 0.2)
            event.isCancelled = true
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