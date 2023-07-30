package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object CraftItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun craftLowest(event: CraftItemEvent) {
        val item = event.recipe.result
        val type = item.type
        val player = event.whoClicked as Player

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

        event.isCancelled = EventHandler.triggerLowest(type, HandlerType.CRAFT, player)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun craftHigh(event: CraftItemEvent) {
        val type = event.recipe.result.type
        val player = event.whoClicked as Player

        EventHandler.triggerHigh(type, HandlerType.CRAFT, player)
    }
}