package world.icebear03.starlight.other

import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import java.awt.desktop.QuitEvent

object WelcomeMsg {
    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        event.joinMessage = null
        val player = event.player
        if (player.hasPlayedBefore()) {
            Bukkit.broadcastMessage("§b繁星工坊 §7>> 欢迎玩家§e${player.name}§7进入服务器")
        } else {
            Bukkit.broadcastMessage("§b繁星工坊 §7>> 欢迎新玩家§e${player.name}§7进入服务器，请大家多多关照")
        }
    }

    @SubscribeEvent
    fun quit(event: PlayerQuitEvent) {
        event.quitMessage = null
    }
}