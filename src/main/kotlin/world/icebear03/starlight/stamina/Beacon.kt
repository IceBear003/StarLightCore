package world.icebear03.starlight.stamina

import org.bukkit.Location
import org.bukkit.entity.Player
import world.icebear03.starlight.utils.secToFormattedTime
import java.util.*

data class Beacon(
    var ownerId: UUID,
    var location: Location? = null,
    var lastRecycle: Long? = null,
    var level: Int = 1,
    var isAbandoned: Boolean = false
) {

    override fun hashCode(): Int {
        return ownerId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Beacon)
            return this.hashCode() == other.hashCode()
        return false
    }

    fun canRecycle(): Pair<Boolean, String> {
        return lastRecycle?.let {
            val period = (System.currentTimeMillis() - it) / 1000
            val cooldown = when (level) {
                2 -> 12 * 60 * 60
                3 -> 36 * 60 * 60
                else -> 60 * 60
            }
            if (period >= cooldown)
                true to ""
            else
                false to (cooldown - period).toInt().secToFormattedTime()
        } ?: (true to "")
    }

    fun isBelongedTo(player: Player): Boolean {
        return player.uniqueId == ownerId
    }
}

fun Player.getBeacon(): Beacon {
    return BeaconMechanism.beacons[this.uniqueId]!!
}