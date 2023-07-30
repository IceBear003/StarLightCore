package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.limit.LimitType

object PlaceBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun place(event: BlockPlaceEvent) {
        val type = event.block.type
        val player = event.player

        val placeResult = EventHandler.check(LimitType.PLACE, player, type)
        if (!placeResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法放置此方块，需要解锁以下其中之一: ")
            placeResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }
    }
}