package world.icebear03.starlight.tool.info

import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.event.SubscribeEvent

object AntiMe {

    @SubscribeEvent
    fun me(event: PlayerCommandPreprocessEvent) {
        val cmd = event.message
        if (cmd.startsWith("/me") || cmd.startsWith("/minecraft:me")) {
            event.isCancelled = true
            event.player.sendMessage("§b繁星工坊 §7>> 服务器不允许使用/me")
        }
    }
}