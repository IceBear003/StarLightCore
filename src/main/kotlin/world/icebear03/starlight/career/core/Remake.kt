package world.icebear03.starlight.career.core

import org.bukkit.event.player.PlayerRespawnEvent
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career
import world.icebear03.starlight.career.spell.clearCooldownStamp
import world.icebear03.starlight.career.spell.clearDischargeStamp

object Remake {
    @SubscribeEvent
    fun respawn(event: PlayerRespawnEvent) {
        val player = event.player
        player.career().remake()
        player.clearCooldownStamp()
        player.clearDischargeStamp()
    }
}