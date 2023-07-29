package world.icebear03.starlight.station.mechanism

import org.bukkit.Location
import taboolib.common.platform.function.submit
import world.icebear03.starlight.station.core.StationLoader
import world.icebear03.starlight.utils.verticalDistance
import java.util.*

object NearestStation {

    val nearestMap = mutableMapOf<UUID, Pair<Location?, Double>>()

    fun initialize() {
        submit(period = 20) {
            StationLoader.stationMap.forEach { (_, station) ->
                val stationLoc = station.location ?: return@forEach
                val world = stationLoc.world ?: return@forEach

                world.players.forEach { player ->
                    val uuid = player.uniqueId
                    val loc = player.location
                    nearestMap.putIfAbsent(uuid, null to 1000000000.0)

                    val distance = loc.verticalDistance(loc)
                    val current = nearestMap[uuid]!!.second
                    if (distance <= current) {
                        nearestMap[uuid] = stationLoc to distance
                    }
                }
            }
        }
    }
}