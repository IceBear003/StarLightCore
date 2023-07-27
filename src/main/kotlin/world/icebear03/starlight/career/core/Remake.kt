package world.icebear03.starlight.career.core

import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career
import world.icebear03.starlight.career.spell.clearCooldownStamp

object Remake {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        player.career().remake()
        player.clearCooldownStamp()
    }
}