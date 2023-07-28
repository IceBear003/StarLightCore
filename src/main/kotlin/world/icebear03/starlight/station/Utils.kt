package world.icebear03.starlight.station

import org.bukkit.entity.Player
import world.icebear03.starlight.stamina
import world.icebear03.starlight.station.core.Station
import world.icebear03.starlight.station.core.StationLoader

fun Player.addStamina(amount: Double) {
    return stamina().addStamina(amount)
}

fun Player.takeStamina(amount: Double) {
    return stamina().takeStamina(amount)
}

fun Player.setStamina(amount: Double) {
    return stamina().set(amount)
}

fun Player.station(): Station {
    return StationLoader.stationMap[uniqueId]!!
}