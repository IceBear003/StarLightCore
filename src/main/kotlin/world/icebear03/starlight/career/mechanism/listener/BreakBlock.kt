package world.icebear03.starlight.career.mechanism.listener

import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary
import world.icebear03.starlight.career.mechanism.limit.checkAbility

object BreakBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: BlockBreakEvent) {
        val type = event.block.type

        val player = event.player

        var result = player.checkAbility(MaterialLimitLibrary.breakLimits[type])
        if (!result.first) {
            event.isCancelled = true

            player.sendMessage("无法破坏此方块，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
            return
        }

        result = player.checkAbility(MaterialLimitLibrary.dropIfBreakLimits[type])
        if (!result.first) {
            event.isDropItems = false
            event.expToDrop = 0

            player.sendMessage("无法获得掉落物，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
        }
    }
}