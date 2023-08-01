package world.icebear03.starlight.tool.info

import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.tool.mechanism.AFK
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set

object CareerOnline {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                val time = (player["career_time", PersistentDataType.INTEGER] ?: 0) + 1
                if (!AFK.isAFKing(player))
                    player["career_time", PersistentDataType.INTEGER] = time
            }
        }
    }

    @SubscribeEvent
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        player["career_time", PersistentDataType.INTEGER] = 0
        player["career_rewards_received", PersistentDataType.INTEGER_ARRAY] = listOf<Int>().toIntArray()
    }
}