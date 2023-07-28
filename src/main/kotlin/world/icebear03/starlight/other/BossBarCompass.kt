package world.icebear03.starlight.other

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

object BossBarCompass {

    val barMap = mutableMapOf<UUID, BossBar>()
    fun initialize() {
        submit(period = 1L) {
            onlinePlayers.forEach {
                updateCompass(it)
            }
        }
    }

    @SubscribeEvent
    fun move(event: PlayerMoveEvent) {
        updateCompass(event.player)
    }

    fun updateCompass(player: Player) {
        val barKey = NamespacedKey.minecraft("bar_${player.name.lowercase()}")
        val bar = barMap[player.uniqueId] ?: Bukkit.createBossBar(barKey, "", BarColor.BLUE, BarStyle.SOLID, BarFlag.CREATE_FOG)
        bar.removeFlag(BarFlag.CREATE_FOG)
        bar.setTitle(generateTitle(player))

        if (bar.players.isEmpty())
            bar.addPlayer(player)
        barMap[player.uniqueId] = bar
    }

    fun generateTitle(player: Player): String {
        var plain = "____________________________________________"
        val size = plain.length

        val yaw = player.location.yaw
        var left = (yaw - 50).roundToInt()
        if (left < -180)
            left += 360
        var right = (yaw + 50).roundToInt()
        if (right > 180)
            right -= 360

        val dValue = 100

        val loc = player.location
        val deathYaw = player.lastDeathLocation?.let { getVectorYaw(loc, it) } ?: -3600.0
        val nearestYaw = NearestPlayer.nearestMap[player.uniqueId]?.let nearest@{ (name, _) ->
            val other = Bukkit.getPlayer(name) ?: return@nearest -3600.0
            getVectorYaw(loc, other.location)
        } ?: -3600.0

        var tot = 0
        while (tot++ in 0..dValue) {
            val current = left + tot
            if (left < -180)
                left += 360
            val symbol = getSymbol(
                current,
                deathYaw to "☠",
                nearestYaw to "☺"
            )
            if (symbol.isNotBlank()) {
                val index = minOf(size - 1, (size.toDouble() * tot / dValue).roundToInt())
                plain = plain.replaceRange(index, index + 1, symbol)
            }
        }

        var result = ""
        plain.forEach {
            when (it) {
                '_' -> result += "§8§l_"
                'E' -> result += "§a§lE"
                'S' -> result += "§b§lS"
                'W' -> result += "§6§lW"
                'N' -> result += "§f§lN"
                '︲' -> result += "§7§l︲"
                '︱' -> result += "§f§l︱"
                '☠' -> result += "§c☠"
                '☺' -> result += "§e☺"
            }
        }

        return result
    }

    fun getSymbol(yaw: Int, vararg pairs: Pair<Double, String>): String {
        pairs.forEach { (goal, symbol) ->
            if (matchYaw(yaw, goal)) {
                return symbol
            }
        }
        when (yaw) {
            90 -> return "W"
            -90 -> return "E"
            180, -180 -> return "N"
            0 -> return "S"
            else -> {
                if (yaw % 45 == 0)
                    return "︱"
                if (yaw % 15 == 0)
                    return "︲"
            }
        }
        return ""
    }

    fun matchYaw(current: Int, goal: Double): Boolean {
        var dValue = abs(current - goal)
        dValue = minOf(abs(dValue + 360), dValue)
        dValue = minOf(abs(dValue - 360), dValue)
        return dValue <= 0.5
    }

    fun getVectorYaw(from: Location, to: Location): Double {
        if (from.world!!.name != to.world!!.name)
            return -3600.0
        val vector = from.subtract(to).toVector()
        val degree = Math.toDegrees(atan2(vector.x, vector.z))
        return if (degree >= 0)
            180 - degree
        else
            -180 - degree
    }
}