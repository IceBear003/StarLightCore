package world.icebear03.starlight.career.point

import org.bukkit.event.enchantment.EnchantItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career

object Enchant {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun enchant(event: EnchantItemEvent) {
        val player = event.enchanter
        val career = player.career()
        val rate = if (career.hasClass("学者")) 0.005 else 0.0025
        if (Math.random() <= rate && event.whichButton() >= 1) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你附魔时偶然获得了§a1技能点")
        }
    }
}