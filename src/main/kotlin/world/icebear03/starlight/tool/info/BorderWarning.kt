package world.icebear03.starlight.tool.info

import org.bukkit.World
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.title

object BorderWarning {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                if (player.world.environment == World.Environment.NETHER) {
                    if (player.location.y >= 120) {
                        player.title("§c警告", "§7接近下界基岩层，有可能§e窒息而亡")
                    }
                }
            }
        }
    }
}