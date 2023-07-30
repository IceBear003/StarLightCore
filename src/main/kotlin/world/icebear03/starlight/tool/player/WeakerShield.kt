package world.icebear03.starlight.tool.player

import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent

object WeakerShield {

    @SubscribeEvent
    fun block(event: EntityDamageByEntityEvent) {
        val player = event.entity
        if (player !is Player)
            return
        if (player.isBlocking) {
            val damaged = event.damager
            if (damaged is Arrow) {
                player.setCooldown(Material.SHIELD, 1)
            }
            if (damaged is Trident) {
                player.setCooldown(Material.SHIELD, 2)
            }
            if (damaged is LivingEntity) {
                damaged.equipment?.let {
                    val item = it.itemInMainHand ?: return
                    if (item.type.toString().contains("SWORD")) {
                        player.setCooldown(Material.SHIELD, 3)
                    }
                    if (item.type.toString().contains("_AXE")) {
                        player.setCooldown(Material.SHIELD, 5)
                    }
                    if (item.type == Material.TRIDENT) {
                        player.setCooldown(Material.SHIELD, 2)
                    }
                }
            }
        }
    }
}