package world.icebear03.starlight.career.point

import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career

object Eat {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun eat(event: FoodLevelChangeEvent) {
        val player = event.entity as Player
        val dValue = event.foodLevel - player.foodLevel
        val career = player.career()
        val rate = if (career.hasClass("厨师")) 0.0004 else 0.0002
        if (Math.random() <= rate * dValue && !player.hasPotionEffect(PotionEffectType.SATURATION)) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你吃食物时偶然获得了§a1技能点")
        }
    }
}