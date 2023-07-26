package world.icebear03.starlight.other

import org.bukkit.event.player.PlayerItemMendEvent
import taboolib.common.platform.event.SubscribeEvent

object WeakerMending {
    @SubscribeEvent
    fun mend(event: PlayerItemMendEvent) {
        event.repairAmount = maxOf(1, event.repairAmount / 5)
    }
}