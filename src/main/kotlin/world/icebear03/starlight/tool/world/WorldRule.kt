package world.icebear03.starlight.tool.world

import org.bukkit.Bukkit
import org.bukkit.GameRule
import taboolib.common.platform.function.submit

object WorldRule {
    fun initialize() {
        submit(period = 20L) {
            Bukkit.getWorlds().forEach {
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                it.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                it.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100)
            }
        }
    }
}