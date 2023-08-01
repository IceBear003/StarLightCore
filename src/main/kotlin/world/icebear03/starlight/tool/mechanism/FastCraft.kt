package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import taboolib.platform.util.takeItem

object FastCraft {

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun craft(event: CraftItemEvent) {
        if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            val player = event.whoClicked as Player
            val inv = event.inventory

            event.isCancelled = true

            player.giveItem(event.currentItem!!.clone())

            inv.matrix.forEach { itemInMatrix ->
                val amount = itemInMatrix?.amount ?: return@forEach
                if (amount == 1) {
                    inv.takeItem(1) { it.isSimilar(itemInMatrix) }
                } else {
                    itemInMatrix.amount = amount - 1
                }
            }
        }
    }
}