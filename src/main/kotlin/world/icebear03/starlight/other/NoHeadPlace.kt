package world.icebear03.starlight.other

import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object NoHeadPlace {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun place(event: BlockPlaceEvent) {
        if (event.itemInHand.type == Material.PLAYER_HEAD) {
            event.isCancelled = true
            event.player.sendMessage("§b繁星工坊 §7>> 不允许放置头颅")
        }
    }
}