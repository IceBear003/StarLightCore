package world.icebear03.starlight.tool.info

import org.bukkit.Sound
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.onlinePlayers

object MessageSound {

    @SubscribeEvent
    fun msg(event: PlayerCommandPreprocessEvent) {
        val cmd = event.message
        val args = cmd.split(" ")
        if (args.size <= 2)
            return
        if (!args[0].startsWith("/tell") && !args[0].startsWith("/msg"))
            return
        val receiver = onlinePlayers.find { it.name == args[1] } ?: return
        receiver.playSound(receiver.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
}