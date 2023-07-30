package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.limit.LimitType

object SinterItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun sinter(event: InventoryClickEvent) {
        val inv = event.inventory
        if (!inv.type.toString().contains("FURNACE"))
            return

        if (event.slotType != InventoryType.SlotType.RESULT)
            return

        val item = event.currentItem ?: return
        val type = item.type
        val player = event.whoClicked as Player

        val sinterResult = EventHandler.check(LimitType.SINTER, player, type)
        if (!sinterResult.first) {
            event.isCancelled = true
            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 无法烧炼获得此方块，需要解锁以下其中之一:")
            sinterResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }
}