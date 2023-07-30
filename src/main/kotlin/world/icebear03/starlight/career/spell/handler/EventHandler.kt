package world.icebear03.starlight.career.spell.handler

import org.bukkit.Material
import org.bukkit.entity.Player
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.handler.limit.Limit
import world.icebear03.starlight.career.spell.handler.limit.LimitType

object EventHandler {
    val limitSets = mutableMapOf<Material, MutableMap<LimitType, MutableList<Limit>>>()
    fun add(limitType: LimitType, limit: Pair<String, Int>, vararg types: Material) {
        types.forEach { type ->
            limitSets.putIfAbsent(type, mutableMapOf())
            val map = limitSets[type]!!
            map.putIfAbsent(limitType, mutableListOf())
            map[limitType]!! += Limit(limit)
        }
    }

    fun check(limitType: LimitType, player: Player, type: Material): Pair<Boolean, List<String>> {
        val map = limitSets[type] ?: return true to listOf()
        val limits = map[limitType] ?: return true to listOf()
        var flag = false
        limits.forEach { limit ->
            if (player.meetRequirement(limit)) {
                flag = true
            }
        }
        return flag to limits.map { it.string() }
    }

}