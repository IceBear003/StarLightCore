package world.icebear03.starlight.other

import org.bukkit.Bukkit
import org.bukkit.GameRule
import taboolib.common.platform.function.submit

object WorldRule {
    fun initialize() {
        submit(period = 20L) {
            Bukkit.getWorlds().forEach {
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
            }
        }
    }
}