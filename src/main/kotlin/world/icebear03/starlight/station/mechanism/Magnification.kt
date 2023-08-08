package world.icebear03.starlight.station.mechanism

import org.bukkit.GameMode
import org.bukkit.World.Environment.*
import org.bukkit.entity.Boat
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spellLevel
import world.icebear03.starlight.station.station
import world.icebear03.starlight.tool.mechanism.RespawnProtection

object Magnification {
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
                    result = 0.5
                if (result == -1.0)
                    result = 0.45
            } else {
                if (player.isSneaking)
                    result = 0.15
                if (player.isRiptiding)
                    result = 0.75
                if (player.isSprinting)
                    result = 1.0
                if (player.isSwimming)
                    result = 1.0
                if (player.isGliding)
                    result = 2.0
            }
            if (result == -1.0) {
                result = 0.35
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

        result *= if (player.meetRequirement("山海一过")) 0.3
        else when (player.spellLevel("原野跋涉")) {
            0 -> 0.75
            1 -> 0.7
            2 -> 0.6
            3 -> 0.5
            else -> 1.0
        }

        return result
    }
}