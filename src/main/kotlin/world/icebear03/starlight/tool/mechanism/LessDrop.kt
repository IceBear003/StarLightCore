package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.attacker

object LessDrop {

    @SubscribeEvent
    fun drop(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is Player)
            return
        val killer = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker
        if (killer !is Player) {
            event.drops.clear()
            event.droppedExp = 0
        }
    }
}