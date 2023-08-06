package world.icebear03.starlight.tool.mechanism

import org.bukkit.event.player.PlayerSwapHandItemsEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.discharge.ShortcutDischarge.isSignal

object FastMenu {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun swap(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        val main = player.inventory.itemInMainHand
        val off = player.inventory.itemInOffHand
        if (player.isSneaking && !main.isSignal() && !off.isSignal()) {
            event.isCancelled = true
            val isOp = player.isOp
            player.isOp = true
            try {
                player.performCommand("sl")
            } catch (ignored: Exception) {
            } finally {
                player.isOp = isOp
            }
        }
    }
}