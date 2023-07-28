package world.icebear03.starlight.station.core

import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import world.icebear03.starlight.utils.toLocation
import world.icebear03.starlight.utils.toSavableString
import java.io.File
import java.util.*

object StationLoader {

    val stationMap = mutableMapOf<UUID, Station>()

    fun initialize() {
        val directory = File(getDataFolder().absolutePath + "/station/")
        if (!directory.exists())
            directory.mkdirs()
        directory.listFiles()?.let {
            it.forEach { file ->
                val config = Configuration.loadFromFile(file)
                val station = Station(
                    UUID.fromString(config.getString("ownerId")!!),
                    config.getInt("level"),
                    config.getString("location")?.toLocation(),
                    config.getLong("stamp")
                )
                stationMap[station.ownerId] = station
            }
        }

        submit(delay = 200, period = 200) {
            stationMap.forEach { (ownerID, station) ->
                val file = File(getDataFolder().absolutePath + "/station/", "$ownerID.yml")
                if (!file.exists())
                    file.createNewFile()
                val config = Configuration.loadFromFile(file)
                config["ownerId"] = ownerID.toString()
                config["level"] = station.level
                config["location"] = station.location?.toSavableString()
                config["stamp"] = station.stamp
                config.saveToFile(file)
            }
        }
    }
}