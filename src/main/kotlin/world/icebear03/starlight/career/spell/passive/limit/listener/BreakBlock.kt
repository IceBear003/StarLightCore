package world.icebear03.starlight.career.spell.passive.limit.listener

import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.meetRequirements
import world.icebear03.starlight.career.spell.passive.limit.LimitType
import world.icebear03.starlight.career.spell.passive.limit.limit

object BreakBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: BlockBreakEvent) {
        val type = event.block.type

        val player = event.player

        if (!player.meetRequirements(type.limit(LimitType.BREAK))) {
            event.isCancelled = true

            player.sendMessage("§a生涯系统 §7>> 无法破坏此方块，需要解锁以下其中之一: ")
            type.limit(LimitType.BREAK).forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        if (!player.meetRequirements(type.limit(LimitType.DROP_IF_BREAK))) {
            event.isDropItems = false
            event.expToDrop = 0

            player.sendMessage("§a生涯系统 §7>> 无法获得掉落物，需要解锁以下其中之一: ")
            type.limit(LimitType.DROP_IF_BREAK).forEach {
                player.sendMessage("               §7|—— $it")
            }
        }
    }
}