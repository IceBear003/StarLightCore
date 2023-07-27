package world.icebear03.starlight.career.spell.passive.limit.listener

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirements
import world.icebear03.starlight.career.spell.passive.limit.LimitType
import world.icebear03.starlight.career.spell.passive.limit.limit

object PlaceBlock {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: BlockPlaceEvent) {
        val type = event.block.type

        val player = event.player

        if (!player.meetRequirements(type.limit(LimitType.PLACE))) {
            event.isCancelled = true

            player.sendMessage("§a生涯系统 §7>> 无法放置此方块，需要解锁以下其中之一: ")
            type.limit(LimitType.PLACE).forEach {
                player.sendMessage("               §7|—— ${display(it.first, it.second)}")
            }
            return
        }
    }
}