package world.icebear03.starlight.station.mechanism

import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.event.PlayerJumpEvent
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.stamina
import world.icebear03.starlight.utils.effect

object StaminaEffector {

    @SubscribeEvent
    fun jump(event: PlayerJumpEvent) {
        val player = event.player
        if (player.stamina().stamina !in 1600.0..1800.0)
            return
        if (!player.isSprinting)
            return
        val direction = player.eyeLocation.direction.normalize().multiply(0.6)
        direction.y += 0.1
        direction.y = maxOf(direction.y, 0.5)
        player.velocity = direction
    }

    fun initialize() {
        submit(period = 100) {
            onlinePlayers.forEach { player ->
                val stamina = player.stamina().stamina
                if (stamina in 750.0..1000.0) {
                    player.effect(PotionEffectType.SLOW, 6, 1)
                }
                if (stamina in 500.0..750.0) {
                    player.effect(PotionEffectType.SLOW, 6, 2)
                    player.effect(PotionEffectType.SLOW_DIGGING, 6, 1)
                }
                if (stamina in 250.0..500.0) {
                    player.effect(PotionEffectType.SLOW, 6, 2)
                    player.effect(PotionEffectType.SLOW_DIGGING, 6, 2)
                    player.effect(PotionEffectType.BLINDNESS, 8, 1)
                }
                if (stamina in 0.0..250.0) {
                    player.effect(PotionEffectType.SLOW, 6, 2)
                    player.effect(PotionEffectType.SLOW_DIGGING, 6, 2)
                    player.effect(PotionEffectType.BLINDNESS, 8, 2)
                }
            }
        }
    }
}