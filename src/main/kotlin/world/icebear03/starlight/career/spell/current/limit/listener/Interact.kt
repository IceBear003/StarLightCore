package world.icebear03.starlight.career.spell.current.limit.listener

import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isMainhand
import world.icebear03.starlight.career.spell.current.limit.LimitHandler
import world.icebear03.starlight.career.spell.current.limit.LimitType

object Interact {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickBlock(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return
        val type = event.clickedBlock!!.type
        val player = event.player

        val useResult = LimitHandler.check(LimitType.USE, player, type)
        if (!useResult.first) {
            event.isCancelled = true
            if (event.isMainhand()) {
                player.sendMessage("§a生涯系统 §7>> 无法使用此方块，需要解锁以下其中之一: ")
                useResult.second.forEach {
                    player.sendMessage("               §7|—— $it")
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickItem(event: PlayerInteractEvent) {
        if (!event.hasItem())
            return
        val type = event.item!!.type
        val player = event.player

        val useResult = LimitHandler.check(LimitType.USE, player, type)
        if (!useResult.first) {
            event.isCancelled = true
            if (event.isMainhand()) {
                player.sendMessage("§a生涯系统 §7>> 无法使用此物品，需要解锁以下其中之一: ")
                useResult.second.forEach {
                    player.sendMessage("               §7|—— $it")
                }
            }
        }
    }
}