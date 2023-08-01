package world.icebear03.starlight.tool.world

import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object NoHeadPlace {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun place(event: BlockPlaceEvent) {
        if (event.itemInHand.type == Material.PLAYER_HEAD) {
            event.isCancelled = true
        }
    }
}