package world.icebear03.starlight.station.mechanism

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.station.core.StationLoader
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.loadPdc
import world.icebear03.starlight.utils.toName
import java.util.*

object StationMechanism {

    val haloMap = mutableMapOf<UUID, MutableMap<String, Double>>()

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                haloMap[player.uniqueId] = mutableMapOf()
            }
            StationLoader.stationMap.values.forEach { station ->
                val stationLoc = station.location ?: return@forEach
                val ownerName = station.ownerId.toName()

                val range = when (station.level) {
                    1 -> 16
                    2 -> 48
                    3 -> 96
                    else -> 0
                }
                val halo = when (station.level) {
                    1 -> 1.5
                    2 -> 3.0
                    3 -> 6.0
                    else -> 0.0
                }
                stationLoc.world ?: return@forEach

                stationLoc.world!!.players.forEach players@{ player ->
                    val loc = player.location
                    val distance = loc.distance(stationLoc)
                    if (distance > range)
                        return@players

                    var add = halo * minOf(1.0, 1 - distance / range + 0.3)
                    if (player.uniqueId == station.ownerId)
                        add *= 2

                    player.addStamina(add)
                    haloMap[player.uniqueId]!![ownerName] = add
                }

                for (y in stationLoc.blockY + 1..256) {
                    val current = stationLoc.clone()
                    current.y = y.toDouble()
                    val block = current.block
                    if (block.type != Material.AIR) {
                        block.type = Material.AIR
                        block.world.spawnParticle(Particle.BLOCK_DUST, current, 2, block.blockData)
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCancelled = true)
    fun place(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val player = event.player
        item.itemMeta?.let { meta ->
            val ownerId = UUID.fromString(meta.get("station_owner_id", PersistentDataType.STRING) ?: return)
            val station = StationLoader.stationMap[ownerId]!!
            val result = station.place(player, event.block.location)
            player.sendMessage("§b繁星工坊 §7>> " + result.second)
            if (!result.first)
                event.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCancelled = true)
    fun breakBlock(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        val pdc = block.loadPdc()
        val ownerId = UUID.fromString(pdc["station_owner_id"] ?: return)

        val station = StationLoader.stationMap[ownerId]!!
        val result = station.destroy(player)
        player.sendMessage("§b繁星工坊 §7>> " + result.second)
        if (!result.first)
            event.isCancelled = true
    }
}