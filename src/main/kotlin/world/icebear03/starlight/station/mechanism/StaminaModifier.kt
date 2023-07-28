package world.icebear03.starlight.station.mechanism

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.station.takeStamina
import java.util.*

object StaminaModifier {

    val magnificationMap = mutableMapOf<UUID, Double>()
    val locationMap = mutableMapOf<UUID, Location>()

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                val uuid = player.uniqueId
                magnificationMap[uuid] = Magnification.getMagnification(player)
                val mag = magnificationMap[uuid]!!

                val currentLoc = player.location
                val lastLoc = locationMap[uuid] ?: run {
                    locationMap[uuid] = currentLoc
                    return@forEach
                }

                if (lastLoc.world!!.name != currentLoc.world!!.name)
                    return@forEach

                val distance = lastLoc.distance(currentLoc)
                if (distance <= 0.02) {
                    player.addStamina(0.2)
                } else {
                    player.takeStamina(mag * distance)
                    locationMap[uuid] = currentLoc
                }
                //TODO
            }
        }
    }

    @SubscribeEvent
    fun teleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (event.cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
            event.cause != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
        )
            return
        val from = event.from
        val to = event.to ?: return
        if (from.world!!.name != to.world!!.name)
            return

        val uuid = player.uniqueId
        magnificationMap[uuid] = Magnification.getMagnification(player, true)
        val mag = magnificationMap[uuid]!!

        locationMap.remove(uuid)

        val distance = from.distance(to)
        player.takeStamina(distance * mag)
    }

    @SubscribeEvent
    fun eat(event: FoodLevelChangeEvent) {
        val player = event.entity as Player
        val new = event.foodLevel
        val old = player.foodLevel
        if (new > old) {
            player.addStamina((new - old) * 3.0)
        }
    }
}