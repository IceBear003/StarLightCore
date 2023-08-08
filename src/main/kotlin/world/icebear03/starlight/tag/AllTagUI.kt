package world.icebear03.starlight.tag

import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked

@MenuComponent("AllTag")
object AllTagUI {

    @Config("tag/all_tag.yml")
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
        player.openMenu<Linked<Tag>>(config.title().colored()) {
            val (shape, templates) = config
            rows(shape.rows)
            val slots = shape["AllTag\$tag"].toList()
            slots(slots)
            elements {
                when (filter) {
                    "private" -> TagLibrary.tags.values.filter { it.owner != null }
                    "activity" -> TagLibrary.tags.values.filter { it.activity != null }
                    "special" -> TagLibrary.tags.values.filter { it.owner == null && it.activity == null }
                    else -> TagLibrary.tags.values.filter { it.owner == null }
                }
            }

            onBuild { _, inventory ->
                shape.all("AllTag\$tag", "Previous", "Next") { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val template = templates.require("AllTag\$tag")
            onGenerate { _, element, index, slot ->
                template(slot, index, element)
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
    private val mine = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            MyTagUI.open(event.clicker)
        }
    }

    @MenuComponent
    private val all = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker)
        }
    }

    @MenuComponent
    private val activity = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "activity")
        }
    }

    @MenuComponent
    private val private = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "private")
        }
    }

    @MenuComponent
    private val special = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            open(event.clicker, "special")
        }
    }

    @MenuComponent
    private val tag = MenuFunctionBuilder {
        onBuild { (_, _, _, _, _, args) ->
            val tag = args[0] as Tag
            tag.icon()
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            event.clicker.performCommand("sl")
        }
    }
}