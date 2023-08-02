package world.icebear03.starlight.career.point

import org.bukkit.event.player.PlayerFishEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career

object Fish {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun fish(event: PlayerFishEvent) {
        val player = event.player
        val career = player.career()
        val rate = if (career.hasClass("农夫")) 0.0005 else 0.00025
        if (Math.random() <= rate) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你钓鱼时偶然获得了§a1技能点")
        }
    }
}