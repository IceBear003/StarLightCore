package world.icebear03.starlight.career.spell.current.limit.listener

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.current.limit.LimitHandler
import world.icebear03.starlight.career.spell.current.limit.LimitType

object PlaceBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun place(event: BlockPlaceEvent) {
        val type = event.block.type
        val player = event.player

        val placeResult = LimitHandler.check(LimitType.PLACE, player, type)
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