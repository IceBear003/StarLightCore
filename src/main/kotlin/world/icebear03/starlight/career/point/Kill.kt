package world.icebear03.starlight.career.point

import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career
import world.icebear03.starlight.tool.mechanism.AFK

object Kill {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun kill(event: EntityDeathEvent) {
        val death = event.entity

        val lastDamage = death.lastDamageCause ?: return
        if (lastDamage !is EntityDamageByEntityEvent)
            return
        val damager = lastDamage.damager
        val player =
            if (damager is Projectile) {
                if (damager.shooter !is Player)
                    return
                damager.shooter as Player
            } else {
                if (damager !is Player)
                    return
                damager
            }
        if (AFK.isAFKing(player))
            return

        var rate = 0.0
        if (death is Animals)
            rate = 0.00015
        if (death is Monster)
            rate = 0.0003
        if (death is Boss)
            rate = 0.1

        val career = player.career()
        if (career.hasClass("战士"))
            rate *= 2

        if (Math.random() <= rate) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你击杀生物时偶然获得了§a1技能点")
        }
    }
}