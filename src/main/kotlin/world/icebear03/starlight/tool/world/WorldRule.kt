package world.icebear03.starlight.tool.world

import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameRule
import taboolib.common.platform.function.submit

object WorldRule {
    fun initialize() {
        submit(delay = 20L) {
            Bukkit.getWorlds().forEach {
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                it.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                it.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100)
                it.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true)
                it.setGameRule(GameRule.DO_MOB_SPAWNING, true)
                it.difficulty = Difficulty.HARD
            }
        }
    }
}