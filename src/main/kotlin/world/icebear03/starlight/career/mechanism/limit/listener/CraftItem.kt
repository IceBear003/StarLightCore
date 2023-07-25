package world.icebear03.starlight.career.mechanism.limit.listener

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.checkAbility
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary

object CraftItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: CraftItemEvent) {
        val item = event.recipe.result
        val type = item.type

        val player = event.whoClicked as Player

        val result = player.checkAbility(MaterialLimitLibrary.craftLimits[type])

        if (!result.first) {
            event.isCancelled = true
            event.result = Event.Result.DENY

            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 无法合成此物品，需要解锁以下其中之一: ")
            result.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }
}