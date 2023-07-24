package world.icebear03.starlight.career.mechanism.listener

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary
import world.icebear03.starlight.career.mechanism.limit.displayLimit
import world.icebear03.starlight.career.mechanism.limit.hasAbility

object CraftItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: CraftItemEvent) {
        val item = event.recipe.result
        val type = item.type

        val player = event.whoClicked as Player

        var result = false
        val list = mutableListOf<String>()
        (MaterialLimitLibrary.craftMaps[type] ?: return).forEach {
            if (player.hasAbility(it))
                result = true
            else {
                list += displayLimit(it)
            }
        }
        if (!result) {
            event.isCancelled = true
            event.result = Event.Result.DENY

            player.closeInventory()
            player.sendMessage("&7无法合成此物品，需要解锁以下条件其中之一: ")
            list.forEach {
                player.sendMessage("                &7- $it")
            }
        }
    }
}