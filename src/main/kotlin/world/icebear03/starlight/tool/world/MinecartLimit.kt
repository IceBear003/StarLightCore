package world.icebear03.starlight.tool.world

import org.bukkit.entity.Player
import org.bukkit.event.vehicle.VehicleEnterEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object MinecartLimit {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun getOn(event: VehicleEnterEvent) {
        val entity = event.entered
        if (entity !is Player)
            event.isCancelled = true
    }
}