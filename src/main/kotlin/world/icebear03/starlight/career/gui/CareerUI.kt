package world.icebear03.starlight.career.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.function.textured
import org.serverct.parrot.parrotx.function.variable
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
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.forget
import world.icebear03.starlight.career.getClass
import world.icebear03.starlight.career.spell.DischargeHandler
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import world.icebear03.starlight.utils.toRoman

@MenuComponent("Career")
object CareerUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/career.yml")
    }

    @Config("career/gui/career.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player, name: String? = null) {
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
                    "Career\$career_class", "Career\$branch",
                    "Career\$resonate", "Career\$forget", "Career\$bind",
                    "Career\$resonate_type", "Career\$resonate_info"
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val career = player.career()
            if (career.canChoose()) {
                ChooseUI.open(player)
                return
            }

            val clazz = getClass(name) ?: career.getClasses()[0]

            val classes = ClassLoader.classes.values.toList().sortedBy { it.name }
            setSlots("Career\$career_class", classes, player, "element")

            val branches = clazz.branches.values.toList().sortedBy { it.name }
            listOf("branch", "resonate", "forget").forEach {
                setSlots("Career\$$it", branches, player, "element")
            }

            setSlots("Career\$bind", listOf(), player)
            setSlots("Career\$resonate_type", listOf(), player)
            setSlots("Career\$resonate_info", listOf(), player)

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it, clazz)
                }
            }
        }
    }

    val unlock =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZjOTExZGE2M2JlOTdlNjg5ODM1ZmFlNDM5OWI4ZDRiMjJjNGE2YzA5NjAxYTA0NzBjMjJmOTI5YWQ5NDVjZSJ9fX0="

    @MenuComponent
    private val career_class = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val clazz = args[1] as Class
            val career = player.career()

            icon.textured(if (career.hasClass(clazz)) clazz.skull else unlock)

            icon.modifyMeta<ItemMeta> {
                set("mark", PersistentDataType.STRING, clazz.name)
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(clazz.display())
                    "branches" -> clazz.branchNames()
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, _) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val name = item.itemMeta!!.get("mark", PersistentDataType.STRING)
            open(event.clicker, name)
        }
    }

    @MenuComponent
    private val branch = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val career = player.career()

            icon.textured(if (career.getBranchLevel(branch) >= 0) branch.skull else unlock)

            var state = "&e可解锁"
            if (!career.hasClass(branch.clazz)) {
                state = "&c不可解锁(不是对应的职业)"
            }
            val level = career.getBranchLevel(branch)
            if (level >= 0) {
                state = "&a已解锁 &e${level}级"
            }

            icon.modifyMeta<ItemMeta> {
                set("mark", PersistentDataType.STRING, branch.name)
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(branch.display())
                    "state" -> listOf(state)
                    "description" -> branch.description
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val career = player.career()
            val click = event.clickEvent()
            val name = item.itemMeta!!.get("mark", PersistentDataType.STRING)!!

            if (click.isLeftClick)
                BranchUI.open(player, name)

            if (click.isRightClick) {
                player.sendMessage("§a生涯系统 §7>> " + career.unlockBranch(name).second)
                open(player, args[0].toString())
            }
        }
    }

    @MenuComponent
    private val resonate = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val career = player.career()

            var state = "&e可共鸣"
            icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0N2EzOTQ5OWRlNDllMjRjODkyYjA5MjU2OTQzMjkyN2RlY2JiNzM5OWUxMTg0N2YzMTA0ZmRiMTY1YjZkYyJ9fX0=")
            if (career.getBranchLevel(branch) < 0) {
                state = "&c不可共鸣"
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=")
            }
            if (career.resonantBranch == branch) {
                state = "&a正在共鸣"
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=")
            }

            icon.modifyMeta<ItemMeta> {
                set("mark", PersistentDataType.STRING, branch.name)
            }

            icon.variable("state", listOf(state))
        }
        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val name = item.itemMeta!!.get("mark", PersistentDataType.STRING)!!

            player.sendMessage("§a生涯系统 §7>> " + Resonate.resonate(player, name).second)
            open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val forget = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val career = player.career()

            icon.textured(
                if (career.getBranchLevel(branch) < 0) "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0="
                else "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY5NTkwNThjMGMwNWE0MTdmZDc1N2NiODViNDQxNWQ5NjZmMjczM2QyZTdjYTU0ZjdiYTg2OGUzMjQ5MDllMiJ9fX0="
            )

            icon.modifyMeta<ItemMeta> {
                set("mark", PersistentDataType.STRING, branch.name)
            }

            icon.variable("cost", listOf("${career.getBranchLevel(branch) * 2 + 2}"))
        }
        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val name = item.itemMeta!!.get("mark", PersistentDataType.STRING)!!

            player.sendMessage("§a生涯系统 §7>> " + player.forget(name).second)
            open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val bind = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val career = player.career()

            val list = career.shortCuts.map { (level, name) ->
                display(name) + " &7- " + level
            }

            icon.variable("binds", list)
        }
        onClick { (_, _, event, _) ->
            event.clicker.giveItem(DischargeHandler.item)
        }
    }

    @MenuComponent
    private val resonate_type = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val career = player.career()
            val current = career.resonantType

            val list = Resonate.ResonateType.values().map {
                (if (it == current) "&a" else "") + it.displayName
            }

            icon.variable("types", list)
        }
        onClick { (_, _, event, args) ->
            val player = event.clicker
            val career = player.career()
            val values = Resonate.ResonateType.values().toList()
            var index = values.indexOf(career.resonantType) + 1
            if (index >= values.size)
                index = 0
            career.resonantType = values[index]
            open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val resonate_info = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val career = player.career()

            val list = Resonate.resonateMap[player.uniqueId]!!.map { (name, pair) ->
                name.display() + " " + pair.second.toRoman() + "  &7来自 " + pair.first
            }

            val my = career.resonantBranch?.display() ?: "N/A"

            icon.variables {
                when (it) {
                    "my" -> listOf(my)
                    "resonated" -> list
                    else -> listOf()
                }
            }
        }
    }
}