package world.icebear03.starlight.career.mechanism.limit.listener

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.checkAbility
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary

object PlaceBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: BlockPlaceEvent) {
        val type = event.block.type

        val player = event.player

        val result = player.checkAbility(MaterialLimitLibrary.placeLimits[type])

        if (!result.first) {
            event.isCancelled = true

            player.sendMessage("§a生涯系统 §7>> 无法放置此方块，需要解锁以下其中之一: ")
            result.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }
    }
}