package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.limit.LimitType

object BreakBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun breakBlock(event: BlockBreakEvent) {
        val type = event.block.type
        val player = event.player

        val breakResult = EventHandler.check(LimitType.BREAK, player, type)
        if (!breakResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法破坏此方块，需要解锁以下其中之一: ")
            breakResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun drop(event: BlockBreakEvent) {
        val type = event.block.type
        val player = event.player

        val dropResult = EventHandler.check(LimitType.DROP_IF_BREAK, player, type)
        if (!dropResult.first) {
            event.isDropItems = false
            event.expToDrop = 0
            player.sendMessage("§a生涯系统 §7>> 无法获得掉落物，需要解锁以下其中之一: ")
            dropResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }
}