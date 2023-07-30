package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object BreakBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun breakBlockLowest(event: BlockBreakEvent) {
        val type = event.block.type
        val player = event.player

        val breakResult = EventHandler.checkLimit(HandlerType.BREAK, player, type)
        if (!breakResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法破坏此方块，需要解锁以下其中之一: ")
            breakResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.BREAK, player)
        val result = EventHandler.triggerLowest(event, type, HandlerType.DROP_IF_BREAK, player)
        if (!result) {
            event.isDropItems = false
            event.expToDrop = 0
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun breakBlockHigh(event: BlockBreakEvent) {
        val type = event.block.type
        val player = event.player

        val dropResult = EventHandler.checkLimit(HandlerType.DROP_IF_BREAK, player, type)
        if (!dropResult.first) {
            event.isDropItems = false
            event.expToDrop = 0
            player.sendMessage("§a生涯系统 §7>> 无法获得掉落物，需要解锁以下其中之一: ")
            dropResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        EventHandler.triggerHigh(event, type, HandlerType.DROP_IF_BREAK, player)
        EventHandler.triggerHigh(event, type, HandlerType.BREAK, player)
    }
}