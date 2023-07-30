package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.addLowestListener
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.hasBlockAside

object Brewer {
    fun initialize() {
        addLimit(HandlerType.USE, "药剂师" to 0, Material.BREWING_STAND)

        Material.SUSPICIOUS_STEW.addLowestListener(HandlerType.CRAFT) { _, player, _ ->
            if (!player.meetRequirement("药剂师", 0) && Math.random() <= 0.5) {
                false to "合成谜之炖菜失败，解锁 §e职业分支 §7${display("药剂师")} §7可以提高成功率"
            } else true to null
        }

        listOf(Material.HOPPER, Material.DISPENSER).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("药剂师", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(Material.BREWING_STAND, 6))
                return@addLowestListener false to "你不能在酿造台边放置这个方块，需要解锁 §e职业分支 ${display("药剂师")}"
            return@addLowestListener true to null
        }
    }
}