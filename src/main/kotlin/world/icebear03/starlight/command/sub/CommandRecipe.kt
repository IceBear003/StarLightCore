package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.recipe.RecipeBookUI


val commandRecipe = subCommand {
    execute<Player> { sender, _, _ ->
        RecipeBookUI.open(sender)
    }
}