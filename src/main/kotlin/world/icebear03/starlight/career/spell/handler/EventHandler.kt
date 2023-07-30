package world.icebear03.starlight.career.spell.handler

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
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

    val lowestListeners =
        mutableMapOf<Material, MutableMap<HandlerType, MutableList<(Event, Player, Material) -> Pair<Boolean, String?>>>>()
    val highListeners = mutableMapOf<Material, MutableMap<HandlerType, MutableList<(Event, Player, Material) -> String?>>>()
    fun addLowestListener(
        handlerType: HandlerType,
        function: (Event, Player, Material) -> Pair<Boolean, String?>,
        vararg types: Material
    ) {
        types.forEach { type ->
            lowestListeners.putIfAbsent(type, mutableMapOf())
            val map = lowestListeners[type]!!
            map.putIfAbsent(handlerType, mutableListOf())
            map[handlerType]!! += function
        }
    }

    fun addHighListener(
        handlerType: HandlerType,
        function: (Event, Player, Material) -> String?,
        vararg types: Material
    ) {
        types.forEach { type ->
            highListeners.putIfAbsent(type, mutableMapOf())
            val map = highListeners[type]!!
            map.putIfAbsent(handlerType, mutableListOf())
            map[handlerType]!! += function
        }
    }

    fun triggerLowest(event: Event, type: Material, handlerType: HandlerType, player: Player): Boolean {
        lowestListeners[type]?.get(handlerType)?.forEach { func ->
            val result = func.invoke(event, player, type)
            if (!result.first) {
                result.second?.let { player.sendMessage("§b繁星工坊 §7>> $it") }
                return false
            }
        }
        return true
    }

    fun triggerHigh(event: Event, type: Material, handlerType: HandlerType, player: Player) {
        highListeners[type]?.get(handlerType)?.forEach { func ->
            func.invoke(event, player, type)?.let { player.sendMessage("§b繁星工坊 §7>> $it") }
        }
    }
}