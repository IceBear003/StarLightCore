package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.addLowestListener
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.shapedRecipe

object Chef {
    fun initialize() {
        addLimit(HandlerType.USE, "主厨" to 0, Material.SMOKER)

        listOf(Material.HOPPER, Material.DISPENSER).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("主厨", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(Material.SMOKER, 6))
                return@addLowestListener false to "你不能在烟熏炉边放置这个方块，需要解锁 §e职业分支 ${display("主厨")}"
            return@addLowestListener true to null
        }

        addLimit(HandlerType.CRAFT, "主厨" to 0, Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.GLISTERING_MELON_SLICE)

        shapedRecipe(
            NamespacedKey.minecraft("enchant_apple"),
            ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            listOf("aaa", "aba", "aaa"),
            'a' to Material.GOLD_BLOCK,
            'b' to Material.APPLE
        ).addSpecialRecipe("配方传承", 1)
    }


    @SubscribeEvent
    fun eat(event: FoodLevelChangeEvent) {
        val item = event.item ?: return
        val type = item.type
        val player = event.entity as Player
        if (player.meetRequirement("主厨", 0)) {
            event.foodLevel += 1
        }
        if (type == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE) {
            event.foodLevel = 20
        }
    }
}