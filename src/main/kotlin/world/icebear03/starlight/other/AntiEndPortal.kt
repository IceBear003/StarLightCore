package world.icebear03.starlight.other

import org.bukkit.PortalType
import org.bukkit.event.entity.EntityCreatePortalEvent
import org.bukkit.event.world.PortalCreateEvent
import taboolib.common.platform.event.SubscribeEvent

object AntiEndPortal {

    @SubscribeEvent
    fun portal(event: PortalCreateEvent) {
        if (event.reason == PortalCreateEvent.CreateReason.END_PLATFORM)
            event.isCancelled = true
    }

    @SubscribeEvent
    fun portal(event: EntityCreatePortalEvent) {
        if (event.portalType == PortalType.ENDER)
            event.isCancelled = true
    }
}