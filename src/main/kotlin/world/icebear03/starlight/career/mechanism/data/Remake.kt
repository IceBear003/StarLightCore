package world.icebear03.starlight.career.mechanism.data

import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.discharge.clearCooldownStamp
import world.icebear03.starlight.loadCareerData

object Remake {
    @SubscribeEvent
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        loadCareerData(player).remake()
        player.clearCooldownStamp()
    }
}