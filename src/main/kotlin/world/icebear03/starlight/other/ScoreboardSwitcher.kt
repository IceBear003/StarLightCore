package world.icebear03.starlight.other

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object ScoreboardSwitcher {

    val key = NamespacedKey.minecraft("scoreboard_switcher")

    fun switch(player: Player, scoreboard: String) {
        player.performCommand("asb switch $scoreboard")
        player.persistentDataContainer[key, PersistentDataType.STRING] = scoreboard
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        player.persistentDataContainer[key, PersistentDataType.STRING]?.let {
            player.performCommand("asb switch $it")
        }
    }
}