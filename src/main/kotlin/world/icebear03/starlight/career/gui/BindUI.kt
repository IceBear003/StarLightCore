package world.icebear03.starlight.career.gui

import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.function.textured
import org.serverct.parrot.parrotx.function.variables
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.compileToJexl
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.utils.YamlUpdater

@MenuComponent("Bind")
object BindUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/bind.yml")
    }

    @Config("career/gui/bind.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player, name: String) {
        if (!::config.isInitialized) {
            config = MenuConfiguration(source)
        }
        player.openMenu<Basic>(config.title().colored()) {
            val (shape, templates) = config
            rows(shape.rows)
            map(*shape.array)

            fun setSlots(key: String, elements: List<Any?>, vararg args: Any?) {
                var tot = 0
                shape[key].forEach { slot ->
                    set(
                        slot, templates(
                            key, slot, 0, false, "Fallback",
                            *args.map {
                                val string = it.toString()
                                if (string.startsWith("expression="))
                                    return@map string.replace("expression=", "").replace("tot", "$tot")
                                        .compileToJexl().eval()
                                if (string.startsWith("element=")) {
                                    val index = string.replace("element=", "").replace("tot", "$tot")
                                        .compileToJexl().eval() as Int
                                    if (index >= elements.size && elements.isNotEmpty()) {
                                        return
                                    }
                                    return@map elements[index]
                                }
                                if (string == "element") {
                                    if (tot >= elements.size && elements.isNotEmpty()) {
                                        return
                                    }
                                    return@map elements[tot]
                                }
                                return@map it
                            }.toTypedArray()
                        )
                    )
                    tot++
                }
            }

            onBuild { _, inventory ->
                shape.all(
                    "Bind\$bind", "Bind\$unbind"
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val career = player.career()
            val spell = getSpell(name)!!

            val binds = mutableListOf<String?>()
            repeat(9) {
                binds += if (career.shortCuts.containsKey(it)) career.shortCuts[it]!! else null
            }

            setSlots("Bind\$bind", binds, "expression=tot", "element", spell)
            setSlots("Bind\$unbind", listOf(), spell, binds)

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it, spell)
                }
            }
        }
    }

    @MenuComponent
    private val bind = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val index = args[0] as Int
            val name = args[1]
            val spell = args[2] as Spell

            icon.amount = index + 1

            val state =
                if (name == null) {
                    icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjViOTVkYTEyODE2NDJkYWE1ZDAyMmFkYmQzZTdjYjY5ZGMwOTQyYzgxY2Q2M2JlOWMzODU3ZDIyMmUxYzhkOSJ9fX0=")
                    "§f空闲"
                } else {
                    val current = getSpell(name.toString())!!
                    icon.textured(current.skull)
                    "§a已绑定 ${current.display()}"
                }

            icon.variables {
                when (it) {
                    "int" -> listOf("${index + 1}")
                    "display" -> listOf(spell.display())
                    "state" -> listOf(state)
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val career = player.career()
            val spell = args[0] as Spell
            val result = career.addShortCut(spell, item.amount)
            player.sendMessage("§6生涯系统 §7>> " + result.second)
            if (result.first) {
                BranchUI.open(player, spell.branch.name)
            } else {
                player.closeInventory()
            }
        }
    }

    @MenuComponent
    private val unbind = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val spell = args[0] as Spell
            val binds = (args[1] as List<*>).map { it?.toString() ?: "" }

            val state = if (binds.contains(spell.name)) {
                val indexes = mutableListOf<Int>()
                binds.forEachIndexed { index, name ->
                    if (name == spell.name)
                        indexes += index
                }
                "§a已绑定: $indexes"
            } else {
                "§7未绑定"
            }

            icon.textured(spell.skull)

            icon.variables {
                when (it) {
                    "state" -> listOf(state)
                    "display" -> listOf(spell.display())
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, args) ->
            val player = event.clicker
            val career = player.career()
            val spell = args[0] as Spell
            career.shortCuts.filterValues { it == spell.name }.forEach { career.shortCuts.remove(it.key, it.value) }
            BranchUI.open(player, spell.branch.name)
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, args) ->
            val spell = args[0] as Spell
            BranchUI.open(event.clicker, spell.branch.name)
        }
    }
}