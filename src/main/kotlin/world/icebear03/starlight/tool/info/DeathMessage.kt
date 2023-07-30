package world.icebear03.starlight.tool.info

import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object DeathMessage {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        val message = event.deathMessage
        if (message != null) {
            event.deathMessage = "§b繁星工坊 §7>> " + message.replace(
                player.name,
                if (message.startsWith(player.name)) "§e${player.name} §7"
                else " §e${player.name} §7"
            )
        }
    }
}