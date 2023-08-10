package world.icebear03.starlight.station.mechanism

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World.Environment.*
import org.bukkit.block.Biome
import org.bukkit.entity.Boat
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.entry.worker.Lumberjack
import world.icebear03.starlight.career.spell.entry.worker.Miner
import world.icebear03.starlight.career.spellLevel
import world.icebear03.starlight.station.station
import world.icebear03.starlight.tool.mechanism.RespawnProtection

object Magnification {

    val pathTypes = listOf(Material.DIRT_PATH)
    val roadTypes = Miner.stoneBlocks + Lumberjack.woodenBlocks

    fun getMagnification(player: Player, isTeleport: Boolean = false): Double {
        if (player.isFlying || player.gameMode == GameMode.SPECTATOR)
            return 0.0

        var result = -1.0

        if (isTeleport) {
            result = 0.5
        } else {
            if (player.isInsideVehicle) {
                val vehicle = player.vehicle
                if (vehicle is Boat)
                    result = 0.75
                if (vehicle is Minecart)
                    result = 0.3
                if (result == -1.0)
                    result = 0.4
            } else {
                if (player.isRiptiding)
                    result = 0.75
                if (player.isSwimming)
                    result = 1.0
                if (player.isGliding)
                    result = 1.5

                //在跑跳蹲
                if (result == -1.0) {
                    if (player.isSneaking)
                        result = 0.15
                    if (player.isSprinting)
                        result = 1.0
                    if (result == -1.0) {
                        result = 0.35
                    }

                    val world = player.world
                    if (world.environment == NORMAL) {
                        var groundBiome: Biome? = null
                        var groundType = Material.AIR
                        var tot = player.location.y
                        while (tot-- >= -100) {
                            val current = player.location.clone()
                            current.y = tot
                            val block = world.getBlockAt(current)
                            if (block.type != Material.AIR) {
                                groundBiome = block.biome
                                groundType = block.type
                                break
                            }
                        }
                        val y = player.location.y
                        if (y >= 60) {
                            if (roadTypes.contains(groundType))
                                result *= 0.5
                            if (pathTypes.contains(groundType))
                                result *= 0.75
                            if (groundBiome.toString().contains("SWAMP")) {
                                result *= 1.5
                            }
                        } else {
                            result *= 1 + 0.01 * (60 - y)
                        }
                    }
                }
            }
        }

        result *= when (player.world.environment) {
            NORMAL -> 1.0
            NETHER -> 4.0
            THE_END -> 1.5
            else -> 1.0
        }

        if (RespawnProtection.isInProtection(player)) {
            result *= 0.2
        } else {
            val station = player.station()
            if (station.location == null)
                result *= 1.25
            if (StationMechanism.haloMap[player.uniqueId]!!.isNotEmpty()) {
                result *= 0.25
            }
        }

        result *= if (player.meetRequirement("山海一过")) 0.5
        else when (player.spellLevel("原野跋涉")) {
            0 -> 0.9
            1 -> 0.8
            2 -> 0.7
            3 -> 0.6
            else -> 1.0
        }

        return result
    }
}