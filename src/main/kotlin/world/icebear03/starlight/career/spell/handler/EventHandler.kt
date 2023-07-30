package world.icebear03.starlight.career.spell.handler

import org.bukkit.Material
import org.bukkit.entity.Player
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.career.spell.handler.internal.Limit

object EventHandler {
    val limitSets = mutableMapOf<Material, MutableMap<HandlerType, MutableList<Limit>>>()
    fun addLimit(handlerType: HandlerType, limit: Pair<String, Int>, vararg types: Material) {
        types.forEach { type ->
            limitSets.putIfAbsent(type, mutableMapOf())
            val map = limitSets[type]!!
            map.putIfAbsent(handlerType, mutableListOf())
            map[handlerType]!! += Limit(limit)
        }
    }

    fun checkLimit(handlerType: HandlerType, player: Player, type: Material): Pair<Boolean, List<String>> {
        val map = limitSets[type] ?: return true to listOf()
        val limits = map[handlerType] ?: return true to listOf()
        var flag = false
        limits.forEach { limit ->
            if (player.meetRequirement(limit)) {
                flag = true
            }
        }
        return flag to limits.map { it.string() }
    }

    val lowestListeners = mutableMapOf<Material, MutableMap<HandlerType, (Player, Material) -> Pair<Boolean, String?>>>()
    val highListeners = mutableMapOf<Material, MutableMap<HandlerType, (Player, Material) -> String?>>()
    fun addLowestListener(handlerType: HandlerType, function: (Player, Material) -> Pair<Boolean, String?>, vararg types: Material) {
        types.forEach { type ->
            lowestListeners.putIfAbsent(type, mutableMapOf())
            val map = lowestListeners[type]!!
            map[handlerType] = function
        }
    }

    fun addLowestListener(handlerType: HandlerType, function: (Player, Material) -> String?, vararg types: Material) {
        types.forEach { type ->
            highListeners.putIfAbsent(type, mutableMapOf())
            val map = highListeners[type]!!
            map[handlerType] = function
        }
    }

    fun triggerLowest(type: Material, handlerType: HandlerType, player: Player): Boolean {
        val result = lowestListeners[type]?.get(handlerType)?.invoke(player, type) ?: (true to null)
        result.second?.let { player.sendMessage("§b繁星工坊 §7>> $it") }
        return result.first
    }

    fun triggerHigh(type: Material, handlerType: HandlerType, player: Player) {
        highListeners[type]?.get(handlerType)?.invoke(player, type)?.let { player.sendMessage("§b繁星工坊 §7>> $it") }
    }
}