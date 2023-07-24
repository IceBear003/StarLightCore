package world.icebear03.starlight.career.mechanism.set.architect

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.mechanism.limit.LimitType
import world.icebear03.starlight.loadCareerData

object FortressEngineerPassive {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity !is Player)
            return

        if (event.cause != EntityDamageEvent.DamageCause.FALL)
            return

        if (entity.hasBranch("堡垒工程师")) {
            event.damage = event.damage * 0.8
        }

        val level = loadCareerData(entity).getSkillLevel("缓冲装置")
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