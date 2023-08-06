package world.icebear03.starlight.career.spell.entry

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.meetRequirement

object WeaponDamage {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun attack(event: EntityDamageByEntityEvent) {
        val player = event.damager
        val entity = event.entity
        if (player !is Player || entity !is LivingEntity) {
            return
        }
        val typeString = player.inventory.itemInMainHand.type.toString()
        val usingAxe = typeString.contains("_AXE")
        val usingPickAxe = typeString.contains("_PICKAXE")
        val usingSpade = typeString.contains("_SHOVEL")

        if (usingAxe)
            if (!player.meetRequirement("屠夫", 0) &&
                !player.meetRequirement("伐木工", 0) &&
                !player.meetRequirement("工具制造商", 0)
            )
                event.damage *= 0.5

        if (usingPickAxe || usingSpade)
            if (!player.meetRequirement("矿工", 0) &&
                !player.meetRequirement("工具制造商", 0)
            )
                event.damage *= 0.5
    }
}