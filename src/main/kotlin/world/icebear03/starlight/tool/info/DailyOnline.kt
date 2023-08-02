package world.icebear03.starlight.tool.info

import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.tool.mechanism.AFK
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import java.time.ZonedDateTime

object DailyOnline {

    fun initialize() {
        submit(period = 20L) {
            val today = ZonedDateTime.now().dayOfMonth
            onlinePlayers.forEach { player ->
                val date = player["date", PersistentDataType.INTEGER] ?: -1
                val time = (player["daily_time", PersistentDataType.INTEGER] ?: 0) + 1

                if (date != today) {
                    player["daily_time", PersistentDataType.INTEGER] = 0
                    player["date", PersistentDataType.INTEGER] = today
                    player["daily_rewards_received", PersistentDataType.INTEGER_ARRAY] = listOf<Int>().toIntArray()
                } else {
                    if (!AFK.isAFKing(player))
                        player["daily_time", PersistentDataType.INTEGER] = time
                }
            }
        }
    }
}