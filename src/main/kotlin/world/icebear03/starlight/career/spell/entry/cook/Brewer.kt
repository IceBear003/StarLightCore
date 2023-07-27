package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.passive.limit.LimitType
import world.icebear03.starlight.career.spell.passive.recipe.CraftHandler
import world.icebear03.starlight.career.spell.passive.recipe.CraftResult
import world.icebear03.starlight.utils.hasBlockAside

object BrewerPassive {

    fun initialize() {
        CraftHandler.registerLowest(Material.SUSPICIOUS_STEW) { player, _ ->
            println("awa")
            if (!player.meetRequirement("药剂师", 0) && Math.random() <= 0.5) {
                CraftResult.FAIL to "§a生涯系统 §7>> 合成谜之炖菜失败，解锁 §e职业分支 §7${display("药剂师")} §7可以提高成功率"
            } else CraftResult.ALLOW to null
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun place(event: BlockPlaceEvent) {
        val type = event.itemInHand.type
        val player = event.player
        if (BrewerSet.MACHINES.types.contains(type) && !player.meetRequirement("药剂师", 0)) {
            if (event.blockPlaced.location.hasBlockAside(Material.BREWING_STAND, 6)) {
                event.isCancelled = true
                player.sendMessage("§a生涯系统 §7>> 你不能在酿造台边放置这个方块，需要解锁 §e职业分支 ${display("药剂师")}")
            }
        }
    }
}

enum class BrewerSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    BREWING_STAND(listOf(Material.BREWING_STAND), listOf(LimitType.USE to ("药剂师" to 0))),
    MACHINES(listOf(Material.HOPPER, Material.DISPENSER), listOf())
}