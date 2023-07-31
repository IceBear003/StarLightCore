package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object SinterItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun sinterLowest(event: InventoryClickEvent) {
        if (!isAvailable(event))
            return

        val type = event.currentItem!!.type
        val player = event.whoClicked as Player

        val sinterResult = EventHandler.checkLimit(HandlerType.SINTER, player, type)
        if (!sinterResult.first) {
            event.isCancelled = true
            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 无法烧炼获得此方块，需要解锁以下其中之一:")
            sinterResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.SINTER, player)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun sinterHigh(event: InventoryClickEvent) {
        if (!isAvailable(event))
            return

        val type = event.currentItem!!.type
        val player = event.whoClicked as Player

        EventHandler.triggerHigh(event, type, HandlerType.SINTER, player)
    }

    fun isAvailable(event: InventoryClickEvent): Boolean {
        val inv = event.inventory
        if (!inv.type.toString().contains("FURNACE") && inv.type != InventoryType.SMOKER)
            return false
        if (event.slotType != InventoryType.SlotType.RESULT)
            return false
        event.currentItem ?: return false
        return true
    }
}