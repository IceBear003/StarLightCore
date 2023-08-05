package world.icebear03.starlight.tool.world

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import world.icebear03.starlight.utils.YamlUpdater
import kotlin.math.abs
import kotlin.math.roundToInt

object WorldBorder {

    val config = YamlUpdater.loadAndUpdate("other/border.yml")
    var current = config.getDouble("current", 20000.0)
    val extendPerNewPlayer = config.getDouble("extend_per_new_player", 200.0)

    fun initialize() {
        submit(period = 20L) {
            updateBorder()
        }
    }

    private fun updateBorder() {
        Bukkit.getWorlds().forEach {
            val border = it.worldBorder
            border.center = Location(it, 0.0, 0.0, 0.0)
            val tmp = border.size
            if (it.environment == World.Environment.NETHER) {
                if (abs(tmp - current / 8) > 1.0) {
                    border.setSize(current / 8, 1)
                }
            } else {
                if (tmp != current) {
                    border.setSize(current, 1)
                }
            }
        }
    }

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        if (!event.player.hasPlayedBefore()) {
            current += extendPerNewPlayer
            config["current"] = current
            config.saveToFile()
        }
    }

    fun randomLocation(world: World, player: Player): Location {
        val others = world.players.filter { it.uniqueId != player.uniqueId }
        return if (others.isNotEmpty()) {
            val other = others.random()
            val loc = other.location.add(500 + 500 * Math.random(), 0.0, 500 + 500 * Math.random())
            world.getHighestBlockAt(loc).location
        } else {
            val max = world.worldBorder.size * 0.2
            val block = world.getHighestBlockAt((max * Math.random()).roundToInt(), (max * Math.random()).roundToInt())
            block.location
        }
    }
}
