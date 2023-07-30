package world.icebear03.starlight.career.spell.old.passive.recipe

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement

object SpecialRecipeLibrary {

    val recipeMap = mutableMapOf<NamespacedKey, Pair<String, Int>>()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun craft(event: CraftItemEvent) {
        val recipe = event.recipe

        var key: NamespacedKey? = null
        if (recipe is ShapedRecipe) {
            key = recipe.key
        }
        if (recipe is ShapelessRecipe) {
            key = recipe.key
        }
        key ?: return

        val (name, level) = recipeMap[key] ?: return
        val player = event.whoClicked as Player
        if (!player.meetRequirement(name, level)) {
            event.isCancelled = true
            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 必须解锁 " + display(name, level) + " §7才可以使用此特殊合成")
        }
    }
}

fun ShapedRecipe.addSpecialRecipe(id: String, level: Int = 1) {
    SpecialRecipeLibrary.recipeMap[this.key] = id to level
}

fun ShapelessRecipe.addSpecialRecipe(id: String, level: Int = 1) {
    SpecialRecipeLibrary.recipeMap[this.key] = id to level
}