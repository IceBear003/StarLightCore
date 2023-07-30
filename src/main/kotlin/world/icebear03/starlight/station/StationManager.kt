package world.icebear03.starlight.station

import world.icebear03.starlight.station.core.StationLoader
import world.icebear03.starlight.station.mechanism.*

object StationManager {

    fun initialize() {
        StationLoader.initialize()
        StationMechanism.initialize()
        StaminaModifier.initialize()
        StaminaEffector.initialize()
        StationCraft.initialize()
        NearestStation.initialize()
    }
}