package world.icebear03.starlight.station.mechanism

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.station.takeStamina
import world.icebear03.starlight.tool.player.RespawnProtection
import java.util.*

object StaminaModifier {

    val magnificationMap = mutableMapOf<UUID, Double>()
    val locationMap = mutableMapOf<UUID, Location>()
    val resting = mutableListOf<UUID>()

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                if (player.isSleeping)
                    player.addStamina(0.5)

                val uuid = player.uniqueId
                magnificationMap[uuid] = Magnification.getMagnification(player)
                val mag = magnificationMap[uuid]!!

                val currentLoc = player.location
                val lastLoc = locationMap[uuid] ?: run {
                    locationMap[uuid] = currentLoc
                    return@forEach
                }

                if (lastLoc.world != currentLoc.world) {
                    locationMap[uuid] = currentLoc
                    return@forEach
                }

                val distance = lastLoc.distance(currentLoc)
                if (distance <= 0.02) {
                    resting += player.uniqueId
                    player.addStamina(0.2)
                } else {
                    resting -= player.uniqueId
                    locationMap[uuid] = currentLoc
                    if (RespawnProtection.isInProtection(player, 5)) {
                        return@forEach
                    }
                    player.takeStamina(mag * distance)
                }
            }
        }
    }

    @SubscribeEvent
    fun teleport(event: PlayerTeleportEvent) {
        val player = event.player
        val uuid = player.uniqueId
        locationMap.remove(uuid)
        if (event.cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
            event.cause != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
        )
            return
        val from = event.from
        val to = event.to ?: return
        if (from.world != to.world)
            return

        magnificationMap[uuid] = Magnification.getMagnification(player, true)
        val mag = magnificationMap[uuid]!!

        val distance = from.distance(to)
        player.takeStamina(distance * mag)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun eat(event: FoodLevelChangeEvent) {
        val player = event.entity as Player
        val new = event.foodLevel
        val old = player.foodLevel
        if (new > old) {
            player.addStamina((new - old) * 3.0)
        }
    }
}