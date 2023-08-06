package world.icebear03.starlight.tag

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.function.textured
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.get

@MenuComponent("MyTag")
object MyTagUI {

    @Config("tag/my_tag.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player) {
        if (!::config.isInitialized) {
            config = MenuConfiguration(source)
        }
        player.openMenu<Linked<Tag>>(config.title().colored()) {
            val (shape, templates) = config
            rows(shape.rows)
            val slots = shape["MyTag\$tag"].toList()
            slots(slots)
            elements { PlayerTag.tagList(player) }

            onBuild { _, inventory ->
                shape.all("MyTag\$tag", "MyTag\$current", "Previous", "Next") { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val template = templates.require("MyTag\$tag")
            onGenerate { _, element, index, slot ->
                template(slot, index, element)
            }

            onClick { event, element ->
                template.handle(event, element)
            }

            shape["MyTag\$current"].forEach { slot ->
                set(slot, templates("MyTag\$current", slot, 0, false, "Fallback", player))
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
    private val current = MenuFunctionBuilder {
        onBuild { (_, _, _, _, _, args) ->
            val player = args[0] as Player
            val tag = PlayerTag.currentTag(player) ?: run {
                return@onBuild ItemStack(Material.PLAYER_HEAD)
                    .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRkNWE3MDcwYjFlMjEyMDc3ZjdiNzdiY2RlMWFlMTkxN2VmNTYxYmNjMWFkMzgyMjk2ZGU0M2E2YjJkYjZhMiJ9fX0=")
                    .modifyMeta<ItemMeta> {
                        setDisplayName("§7称号未选择")
                    }
            }
            val item = tag.icon()

            item.modifyMeta<ItemMeta> {
                setDisplayName("§7当前称号 $displayName")
                lore!! += "§7"
                lore!! += "§8| §7点击卸下当前称号"
            }
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            PlayerTag.clearTag(player)
            open(player)
        }
    }

    @MenuComponent
    private val tag = MenuFunctionBuilder {
        onBuild { (_, _, _, _, _, args) ->
            val tag = args[0] as Tag
            tag.icon().modifyMeta<ItemMeta> {
                lore!! += "§7"
                lore!! += "§8| §7点击装备当前称号"
            }
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            val item = event.currentItem ?: return@onClick
            val meta = item.itemMeta ?: return@onClick
            val tag = meta["tag", PersistentDataType.STRING] ?: return@onClick

            player.sendMessage("§b繁星工坊 §7>> 称号更改成功")
            PlayerTag.setTag(player, tag)
            open(player)
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            AllTagUI.open(event.clicker)
        }
    }
}