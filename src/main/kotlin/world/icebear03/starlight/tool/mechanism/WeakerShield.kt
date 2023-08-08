package world.icebear03.starlight.tool.mechanism

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.spellLevel
import world.icebear03.starlight.utils.isDischarging
import kotlin.math.roundToInt

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
                var duration = 20.0
                if (player.isDischarging("决战冲锋")) {
                    duration *= 1.25 + 0.25 * player.spellLevel("决战冲锋")
                }
                player.setCooldown(Material.SHIELD, duration.roundToInt())
            }
            if (type == EntityType.TRIDENT) {
                player.setCooldown(Material.SHIELD, 40)
            }
        }
    }
}