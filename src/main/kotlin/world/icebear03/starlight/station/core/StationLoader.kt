package world.icebear03.starlight.station.core

import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getLocation
import taboolib.platform.util.toBukkitLocation
import java.io.File
import java.util.*

object StationLoader {

    val stationMap = mutableMapOf<UUID, Station>()

    fun initialize() {
        val directory = File(getDataFolder().absolutePath + "/station/")
        directory.listFiles()?.let {
            it.forEach { file ->
                val config = Configuration.loadFromFile(file)
                val station = Station(
                    UUID.fromString(config.getString("ownerId")!!),
                    config.getInt("level"),
                    config.getLocation("location")?.toBukkitLocation(),
                    config.getLong("stamp")
                )
                stationMap[station.ownerId] = station
            }
        }

        submit(period = 200) {
            stationMap.forEach { (ownerID, station) ->
                val file = File(getDataFolder().absolutePath + "/station/", "$ownerID.yml")
                val config = Configuration.loadFromFile(file)
                config["ownerId"] = ownerID.toString()
                config["level"] = station.level
                config["location"] = station.location
                config["stamp"] = station.stamp
            }
        }
    }
}