package world.icebear03.starlight.station.core

import org.bukkit.event.player.PlayerRespawnEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.station.setStamina
import world.icebear03.starlight.station.station

object Remake {

    @SubscribeEvent
    fun respawn(event: PlayerRespawnEvent) {
        val player = event.player
        player.setStamina(1800.0)
        player.station().deleteFromWorld()
        player.station().level = 1
        player.station().stamp = System.currentTimeMillis() - 100000000

        submit {
            player.giveItem(player.station().generateItem())
        }
    }
}