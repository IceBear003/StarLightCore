package world.icebear03.starlight.career.mechanism.listener

import org.bukkit.event.block.BlockIgniteEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary
import world.icebear03.starlight.career.mechanism.limit.checkAbility

object IgniteBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: BlockIgniteEvent) {
        val type = event.block.type

        val player = event.player ?: return

        val result = player.checkAbility(MaterialLimitLibrary.igniteLimits[type])
        if (!result.first) {
            event.isCancelled = true

            player.sendMessage("无法点燃此方块，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
        }
    }
}