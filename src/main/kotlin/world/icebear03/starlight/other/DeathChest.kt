package world.icebear03.starlight.other

import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object DeathChest {
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        
    }
}