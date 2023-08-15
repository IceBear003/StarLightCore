package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object PlaceBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun placeLowest(event: BlockPlaceEvent) {
        val type = event.itemInHand.type
        val player = event.player

        val placeResult = EventHandler.checkLimit(HandlerType.PLACE, player, type)
        if (!placeResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法放置此方块，需要解锁以下其中之一: ")
            placeResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.PLACE, player)
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun placeHigh(event: BlockPlaceEvent) {
        val type = event.itemInHand.type
        val player = event.player

        EventHandler.triggerHigh(event, type, HandlerType.PLACE, player)
    }
}