package world.icebear03.starlight.tool.mechanism

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent

object WeakerShield {

    @SubscribeEvent
    fun block(event: EntityDamageByEntityEvent) {
        val player = event.entity
        if (player !is Player)
            return
        if (player.isBlocking) {
            val damager = event.damager
            val type = damager.type
            if (type == EntityType.ARROW) {
                player.setCooldown(Material.SHIELD, 20)
            }
            if (type == EntityType.TRIDENT) {
                player.setCooldown(Material.SHIELD, 40)
            }
        }
    }
}