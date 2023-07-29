package world.icebear03.starlight.other

import org.bukkit.Bukkit
import org.bukkit.World
import taboolib.common.platform.function.submit

object FasterNight {

    fun initialize() {
        submit(period = 1L) {
            Bukkit.getWorlds().forEach { world ->
                val time = world.time
                if (time in 0..12500)
                    return@forEach
                if (world.environment != World.Environment.NORMAL)
                    return@forEach

                var amount = 0.0
                val total = world.players.size
                world.players.forEach { player ->
                    if (player.isSleeping)
                        amount += 1.0
                }
                var percentage = amount / total + 0.00001
                if (percentage in 0.01..0.2) {
                    world.time = time + 2
                }
                if (percentage in 0.2..0.3) {
                    world.time = time + 4
                }
                if (percentage in 0.3..0.5) {
                    world.time = time + 9
                }
                if (percentage in 0.5..0.7) {
                    world.time = time + 19
                }
                if (percentage in 0.7..1.0) {
                    world.time = time + 49
                }
            }
        }
    }
}