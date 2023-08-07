package world.icebear03.starlight.recipe

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.getI18nName
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.spell.handler.SpecialRecipeLibrary
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import world.icebear03.starlight.utils.takeItem

@MenuComponent("RecipeBook")
object RecipeBookUI {

    @Config("recipe/book.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player, filter: String? = null) {
        if (!::config.isInitialized) {
            config = MenuConfiguration(source)
        }
        player.openMenu<Linked<Recipe>>(config.title().colored()) {
            val (shape, templates) = config
            rows(shape.rows)
            val slots = shape["RecipeBook\$recipe"].toList()
            slots(slots)
            val filtered = (shapedRecipes + shapelessRecipes).filter {
                when (filter) {
                    "tribe" -> false
                    "career" -> it.key.key.contains("career")
                    "station" -> it.key.key.contains("station")
                    else -> true
                }
            }
            elements { filtered }

            onBuild { _, inventory ->
                shape.all("RecipeBook\$recipe", "Previous", "Next") { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val template = templates.require("RecipeBook\$recipe")
            onGenerate { _, element, index, slot ->
                template(slot, index, element, filter)
            }

            onClick { event, element ->
                template.handle(event, element)
            }

            shape["Previous"].first().let { slot ->
                setPreviousPage(slot) { it, _ ->
                    templates("Previous", slot, it)
                }
            }

            shape["Next"].first().let { slot ->
                setNextPage(slot) { it, _ ->
                    templates("Next", slot, it)
                }
            }

            onClick { event ->
                event.isCancelled = true
                if (event.rawSlot in shape && event.rawSlot !in slots) {
                    templates[event.rawSlot]?.handle(event)
                }
            }
        }
    }

    @MenuComponent
    private val recipe = MenuFunctionBuilder {
        onBuild { (_, _, _, _, _, args) ->
            val recipe = args[0] as Recipe
            val result = recipe.result.clone()
            var key: NamespacedKey? = null
            var type = "§7N/A"
            val ingredients = mutableListOf("§7N/A")
            var more = "§7无"
            if (recipe is ShapelessRecipe) {
                key = recipe.key
                type = "§c无序合成"
                ingredients.clear()
                ingredients += recipe.ingredientList.map { item -> "§a${item.amount} × §b${item.getI18nName()}" }
            }
            if (recipe is ShapedRecipe) {
                key = recipe.key
                type = "§a有序合成"
                ingredients.clear()
                ingredients += recipe.shape.map {
                    val s = it.uppercase()
                    "§7${s[0]} | ${s[1]} | ${s[2]}"
                }

                ingredients += recipe.ingredientMap
                    .filter { it.key != ' ' }
                    .map { (letter, type) -> "§7${letter.uppercase()} - §b${type.getI19nName()}" }
            }

            SpecialRecipeLibrary.recipeMap[key]?.let { pair ->
                more = "需要 ${display(pair.first, pair.second)}"
            }

            key?.let { result.modifyMeta<ItemMeta> { this["key", PersistentDataType.STRING] = it.key } }

            result.modifyLore {
                add("§8")
                add("§8| §7合成类型: $type".colored())
                add("§8| §7合成表:")
                addAll(ingredients.map { "       $it" })
                add("§8")
                add("§8| §7备注: $more")
                add("§8| §7点击§e自动放置合成")
            }
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            val item = event.currentItem ?: return@onClick
            val meta = item.itemMeta ?: return@onClick
            val key = meta["key", PersistentDataType.STRING] ?: return@onClick

            val recipe = Bukkit.getRecipe(NamespacedKey.minecraft(key))!!

            player.closeInventory()
            val view = player.openWorkbench(null, true)!!
            val inv = view.topInventory as CraftingInventory
            val playerInv = player.inventory
            submit {
                if (recipe is ShapelessRecipe) {
                    var tot = 1
                    recipe.ingredientList.forEach { ingredient ->
                        val single = ingredient.clone()
                        single.amount = 1
                        val amount = ingredient.amount
                        repeat(amount) {
                            if (player.takeItem(1) { it.isSimilar(single) }) {
                                inv.setItem(tot++, single)
                            }
                        }
                    }
                }
                if (recipe is ShapedRecipe) {
                    val string = recipe.shape.joinToString("")
                    repeat(9) repeat@{ i ->
                        val ingredient = recipe.ingredientMap[string[i]] ?: return@repeat
                        if (player.takeItem(1) { it.isSimilar(ingredient) }) {
                            inv.setItem(i + 1, ingredient)
                        }
                    }
                }
            }
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            event.clicker.performCommand("sl")
        }
    }

    @MenuComponent
    private val filter_clear = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker)
        }
    }

    @MenuComponent
    private val filter_tribe = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "tribe")
        }
    }

    @MenuComponent
    private val filter_career = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "career")
        }
    }

    @MenuComponent
    private val filter_station = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "station")
        }
    }

    fun ItemStack.getI19nName(): String {
        return when (type) {
            Material.NETHERITE_BLOCK -> "下界合金块"
            Material.NETHERITE_INGOT -> "下界合金锭"
            Material.CRYING_OBSIDIAN -> "哭泣的黑曜石"
            else -> getI18nName()
        }
    }
}