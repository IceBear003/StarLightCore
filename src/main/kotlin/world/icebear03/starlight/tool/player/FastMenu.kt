package world.icebear03.starlight.tool.player

import org.bukkit.event.player.PlayerSwapHandItemsEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object FastMenu {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun swap(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (player.isSneaking) {
            event.isCancelled = true
            val isOp = player.isOp
            player.isOp = true
            try {
                player.performCommand("bs 主菜单")
            } catch (ignored: Exception) {
            } finally {
                player.isOp = isOp
            }
        }
    }
}