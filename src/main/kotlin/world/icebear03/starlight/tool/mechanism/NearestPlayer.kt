package world.icebear03.starlight.tool.mechanism

import org.bukkit.Bukkit
import org.bukkit.GameMode
import taboolib.common.platform.function.submit
import java.util.*
import kotlin.math.roundToInt

object NearestPlayer {

    val nearestMap = mutableMapOf<UUID, Pair<String, String>>()

    fun initialize() {
        submit(period = 20) {
            Bukkit.getWorlds().forEach { world ->
                world.players.forEach { player ->
                    val loc = player.location
                    var distance = 1000000000
                    var target = "无"
                    world.players.forEach other@{ other ->
                        if (other.uniqueId == player.uniqueId)
                            return@other
                        if (other.gameMode != GameMode.SURVIVAL)
                            return@other
                        val otherLoc = other.location
                        val tmp = loc.distance(otherLoc)
                        if (tmp <= distance && tmp >= 100) {
                            distance = tmp.roundToInt()
                            target = other.name
                        }
                    }
                    nearestMap[player.uniqueId] = target to if (distance == 1000000000) "N/A" else distance.toString()
                }
            }
        }
    }
}