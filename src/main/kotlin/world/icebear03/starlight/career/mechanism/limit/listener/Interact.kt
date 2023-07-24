package world.icebear03.starlight.career.mechanism.limit.listener

import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.checkAbility
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary

object Interact {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun eventBlock(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return
        val type = event.clickedBlock!!.type

        val player = event.player

        val result = player.checkAbility(MaterialLimitLibrary.useLimits[type])
        if (!result.first) {
            event.isCancelled = true

            player.sendMessage("无法使用此方块，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun eventItem(event: PlayerInteractEvent) {
        if (!event.hasItem())
            return
        val type = event.item!!.type

        val player = event.player

        val result = player.checkAbility(MaterialLimitLibrary.useLimits[type])
        if (!result.first) {
            event.isCancelled = true

            player.sendMessage("无法使用此物品，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
        }
    }
}