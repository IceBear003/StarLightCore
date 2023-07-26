package world.icebear03.starlight.career.mechanism.passive.recipe

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object CraftHandler {

    val listenersLowest = mutableMapOf<List<Material>, (Player, Material) -> Pair<CraftResult, String?>>()
    val listenersHigh = mutableMapOf<List<Material>, (Player, Material) -> String?>()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun craftLowest(event: CraftItemEvent) {
        val type = event.recipe.result.type
        val player = event.whoClicked as Player
        listenersLowest.filter { it.key.contains(type) }.forEach {
            val result = it.value.invoke(player, type)
            when (result.first) {
                CraftResult.ALLOW -> {}
                CraftResult.DENY -> {
                    event.isCancelled = true
                    player.closeInventory()
                    if (result.second != null)
                        player.sendMessage(result.second)
                }

                CraftResult.FAIL -> {
                    event.isCancelled = true
                    event.inventory.clear()
                    player.closeInventory()
                    if (result.second != null)
                        player.sendMessage(result.second)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun craftHigh(event: CraftItemEvent) {
        val type = event.recipe.result.type
        val player = event.whoClicked as Player
        listenersHigh.filter { it.key.contains(type) }.forEach {
            player.sendMessage(it.value.invoke(player, type) ?: return@forEach)
        }
    }

    fun registerLowest(type: Material, function: (Player, Material) -> Pair<CraftResult, String?>) {
        CraftHandler.listenersLowest[listOf(type)] = function
    }

    fun registerLowest(types: List<Material>, function: (Player, Material) -> Pair<CraftResult, String?>) {
        CraftHandler.listenersLowest[types] = function
    }

    fun registerHigh(type: Material, function: (Player, Material) -> String?) {
        CraftHandler.listenersHigh[listOf(type)] = function
    }

    fun registerHigh(types: List<Material>, function: (Player, Material) -> String?) {
        CraftHandler.listenersHigh[types] = function
    }

}