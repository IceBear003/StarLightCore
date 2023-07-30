package world.icebear03.starlight.other

import org.bukkit.World
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.submit
import taboolib.platform.util.actionBar
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.title
import java.util.*

object DarkMare {

    val timeInDark = mutableMapOf<UUID, Double>()

    fun initialize() {
        submit(period = 5) {
            onlinePlayers.forEach { player ->
                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                    return@forEach
                if (player.world.environment == World.Environment.THE_END)
                    return@forEach
                val block = player.eyeLocation.block
                if (block.lightLevel == 0.toByte()) {
                    val current = timeInDark.getOrDefault(player.uniqueId, 0.0) + 0.25
                    timeInDark[player.uniqueId] = current
                    if (current >= 5) {
                        player.damage(0.2)
                        if (player.health >= 1)
                            player.health = player.health - 0.75
                        player.title("§7黑暗", "§7受到未知袭击...", 0, 10, 0)
                        player.actionBar("§7请尽一切可能离开黑暗")
                        return@forEach
                    }
                    if (current >= 2.5) {
                        player.title("§7黑暗", "§7恐惧正在袭来...", 0, 10, 0)
                        player.actionBar("§7请尽快离开黑暗")
                    }
                } else {
                    timeInDark.remove(player.uniqueId)
                }
            }
        }
    }
}