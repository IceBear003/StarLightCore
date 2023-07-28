package world.icebear03.starlight.other

import org.bukkit.event.server.ServerListPingEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object CustomMotd {

    @SubscribeEvent(EventPriority.MONITOR)
    fun ping(event: ServerListPingEvent) {
        event.motd = "§6§l                 繁星工坊-StarLight               \n" +
                "   §e§l1.9-1.19 §b§l生涯2.0 多人配合 §c§l全新玩法机制 告别无聊   "
        event.maxPlayers = -1
    }
}