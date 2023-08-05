package world.icebear03.starlight.tool.mechanism

import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object LessFallDamage {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun fall(event: EntityDamageEvent) {
        val cause = event.cause
        if (event.damage <= 1.5 && cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
    }
}