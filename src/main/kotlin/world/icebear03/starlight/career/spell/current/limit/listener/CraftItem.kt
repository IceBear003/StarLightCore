package world.icebear03.starlight.career.spell.current.limit.listener

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.current.limit.LimitHandler
import world.icebear03.starlight.career.spell.current.limit.LimitType

object CraftItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: CraftItemEvent) {
        val item = event.recipe.result
        val type = item.type
        val player = event.whoClicked as Player

        val craftResult = LimitHandler.check(LimitType.CRAFT, player, type)
        if (!craftResult.first) {
            event.isCancelled = true
            event.result = Event.Result.DENY
            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 无法合成此物品，需要解锁以下其中之一: ")
            craftResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }
}