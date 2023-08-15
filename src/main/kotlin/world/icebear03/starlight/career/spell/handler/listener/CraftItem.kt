package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.isFull
import world.icebear03.starlight.utils.takeItem

object CraftItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun craftLowest(event: CraftItemEvent) {
        val item = event.currentItem ?: return
        val type = item.type
        val player = event.whoClicked as Player

        if (player.inventory.isFull() &&
            !event.action.toString().contains("DROP")
        ) {
            player.sendMessage("§b繁星工坊 §7>> 背包已满，请保证至少有一个空位")
            event.isCancelled = true
            return
        }

        val craftResult = EventHandler.checkLimit(HandlerType.CRAFT, player, type)
        if (!craftResult.first) {
            event.isCancelled = true
            event.result = Event.Result.DENY
            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 无法合成此物品，需要解锁以下其中之一: ")
            craftResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.CRAFT, player)
        if (event.isCancelled) {
            val recipe = event.recipe
            val inv = event.inventory
            inv.matrix.toList().forEach { itemInMatrix -> player.takeItem(1) { it.isSimilar(itemInMatrix) } }
            player.closeInventory()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun craftHigh(event: CraftItemEvent) {
        val type = event.currentItem?.type ?: return
        val player = event.whoClicked as Player

        EventHandler.triggerHigh(event, type, HandlerType.CRAFT, player)
    }
}