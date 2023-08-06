package world.icebear03.starlight.tool.world

import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.submit
import taboolib.platform.util.actionBar
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.title
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.utils.realDamage
import java.util.*

object DarkMare {

    val timeInDark = mutableMapOf<UUID, Double>()

    fun initialize() {
        submit(period = 5) {
            onlinePlayers.forEach { player ->
                if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR)
                    return@forEach
                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                    return@forEach
                if (player.world.environment == World.Environment.THE_END)
                    return@forEach

                var extraTime = 0
                var lessDamage = 0.0
                if (player.meetRequirement("自然矿洞勘探"))
                    if (player.location.block.biome.toString().contains("CAVE"))
                        extraTime += 5
                if (player.meetRequirement("黑暗适应")) {
                    extraTime += 10
                    lessDamage = 0.25
                }

                val block = player.eyeLocation.block
                if (block.lightLevel == 0.toByte()) {
                    val current = timeInDark.getOrDefault(player.uniqueId, 0.0) + 0.25
                    timeInDark[player.uniqueId] = current
                    if (current >= 7.5 + extraTime) {
                        player.realDamage(0.5 - lessDamage)
                        player.title("§7黑暗", "§7受到未知袭击...", 0, 10, 0)
                        player.actionBar("§7请尽一切可能离开黑暗")
                        return@forEach
                    }
                    if (current >= 2.5 + extraTime) {
                        player.title("§7黑暗", "§7恐惧正在袭来...", 0, 10, 0)
                        player.actionBar("§7请尽快离开黑暗，否则将会持续受到攻击")
                    }
                } else {
                    timeInDark.remove(player.uniqueId)
                }
            }
        }
    }
}