package world.icebear03.starlight.career.mechanism.passive.limit.listener

import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isMainhand
import world.icebear03.starlight.career.mechanism.checkAbility
import world.icebear03.starlight.career.mechanism.passive.limit.MaterialLimitLibrary

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

            if (event.isMainhand()) {
                player.sendMessage("§a生涯系统 §7>> 无法使用此方块，需要解锁以下其中之一: ")
                result.second.forEach {
                    player.sendMessage("               §7|—— $it")
                }
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

            if (event.isMainhand()) {
                player.sendMessage("§a生涯系统 §7>> 无法使用此物品，需要解锁以下其中之一: ")
                result.second.forEach {
                    player.sendMessage("               §7|—— $it")
                }
            }
        }
    }
}