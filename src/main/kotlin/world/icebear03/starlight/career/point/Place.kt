package world.icebear03.starlight.career.point

import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career

object Place {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun place(event: BlockPlaceEvent) {
        val player = event.player
        val career = player.career()
        val rate = if (career.hasClass("建筑师")) 0.00004 else 0.00002
        if (Math.random() <= rate) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你放置方块时偶然获得了§a1技能点")
        }
    }
}