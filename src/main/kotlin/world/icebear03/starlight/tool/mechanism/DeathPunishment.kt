package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import java.util.*

object DeathPunishment {

    val punishing = mutableListOf<UUID>()

    fun initialize() {
        submit(period = 20 * 3600) {
            onlinePlayers.forEach {
                it["death_times_this_hour", PersistentDataType.INTEGER] = 0
                punishing.clear()
            }
        }
    }

    @SubscribeEvent
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        val times = player["death_times_this_hour", PersistentDataType.INTEGER] ?: 0
        player["death_times_this_hour", PersistentDataType.INTEGER] = times + 1
        if (times + 1 >= 4) {
            punishing += player.uniqueId
        }
    }

    fun isPunished(player: Player): Boolean {
        return punishing.contains(player.uniqueId)
    }
}