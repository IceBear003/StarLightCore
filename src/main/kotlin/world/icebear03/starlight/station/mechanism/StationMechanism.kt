package world.icebear03.starlight.station.mechanism

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.isMainhand
import taboolib.platform.util.isRightClickBlock
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.station.core.StationLoader
import world.icebear03.starlight.utils.*
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

                val horizontal = station.horizontal()
                val vertical = station.vertical()
                val halo = station.halo()
                
                stationLoc.world ?: return@forEach

                stationLoc.world!!.players.forEach players@{ player ->
                    val loc = player.location
                    val horizontalDistance = loc.horizontalDistance(stationLoc)
                    if (horizontalDistance > horizontal)
                        return@players
                    val verticalDistance = loc.verticalDistance(stationLoc)
                    if (verticalDistance > vertical) {
                        return@players
                    }

                    var add = halo * minOf(1.0, 1 - horizontalDistance / horizontal + 0.3)
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
        player.sendMessage("§b繁星工坊 §7>> " + station.destroy(player))
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun explode(event: BlockExplodeEvent) {
        event.blockList().filter { it.type == Material.CAMPFIRE }.forEach {
            if (it.loadPdc().containsKey("station_owner_id"))
                event.blockList().remove(it)
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun explode(event: EntityExplodeEvent) {
        event.blockList().filter { it.type == Material.CAMPFIRE }.forEach {
            if (it.loadPdc().containsKey("station_owner_id"))
                event.blockList().remove(it)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun rightClick(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return

        val block = event.clickedBlock!!
        if (block.type != Material.CAMPFIRE)
            return

        if (!event.isRightClickBlock())
            return

        event.isCancelled = true

        val player = event.player
        val ownerId = UUID.fromString(block.loadPdc()["station_owner_id"] ?: return)

        val station = StationLoader.stationMap[ownerId]!!
        val ownerName = ownerId.toName()

        if (event.isMainhand())
            player.sendMessage("§b繁星工坊 §7>> 这是 §e$ownerName §7的驻扎篝火，等级 ${station.level.toRoman()}")
    }
}