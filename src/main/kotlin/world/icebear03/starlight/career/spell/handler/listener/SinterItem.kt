package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.isFull

object SinterItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun sinterLowest(event: InventoryClickEvent) {
        if (!isAvailable(event))
            return

        val type = event.currentItem!!.type
        val player = event.whoClicked as Player

        if (player.inventory.isFull() &&
            !event.action.toString().contains("DROP")
        ) {
            player.sendMessage("§b繁星工坊 §7>> 背包已满，请保证至少有一个空位")
            event.isCancelled = true
            return
        }

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

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun sinterHigh(event: InventoryClickEvent) {
        if (!isAvailable(event))
            return

        val type = event.currentItem!!.type
        val player = event.whoClicked as Player

        EventHandler.triggerHigh(event, type, HandlerType.SINTER, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun breakFurnace(event: BlockBreakEvent) {
        val block = event.block
        if (!block.type.toString().contains("FURNACE"))
            return
        val player = event.player
        val furnace = block.state as org.bukkit.block.Furnace
        val result = furnace.inventory.result ?: return
        val type = result.type
        val sinterResult = EventHandler.checkLimit(HandlerType.SINTER, player, type)
        if (!sinterResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法破坏这个熔炉，因为其中包含你无法烧炼获得的物品，需要解锁以下其中之一:")
            sinterResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
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