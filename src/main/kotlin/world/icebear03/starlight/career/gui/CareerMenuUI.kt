package world.icebear03.starlight.career.gui

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
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
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.career.internal.Class
import world.icebear03.starlight.career.internal.ResonateType
import world.icebear03.starlight.career.mechanism.data.Forget
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.career.mechanism.discharge.DischargeHandler
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.loadCareerData
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.toRoman

@MenuComponent("CareerMenu")
object CareerMenuUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/career_menu.yml")
    }

    @Config("career/gui/career_menu.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player, classId: String? = null) {
        if (!::config.isInitialized) {
            config = MenuConfiguration(source)
        }
        player.openMenu<Basic>(config.title().colored()) {
            virtualize()
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
                    "CareerMenu\$career_class", "CareerMenu\$branch",
                    "CareerMenu\$resonate", "CareerMenu\$forget", "CareerMenu\$bind",
                    "CareerMenu\$resonate_type", "CareerMenu\$resonate_info"
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val data = loadCareerData(player)
            val demonstrating = Class.fromId(classId) ?: data.getClasses()[0]

            val classes = Class.classes.values.toList().sortedBy { it.id }
            setSlots("CareerMenu\$career_class", classes, player, "element")

            val branches = demonstrating.branches.toList().sortedBy { it.id }
            val tmp = listOf("branch", "resonate", "forget")
            tmp.forEach {
                setSlots("CareerMenu\$$it", branches, player, "element")
            }

            setSlots("CareerMenu\$bind", listOf(), player)
            setSlots("CareerMenu\$resonate_type", listOf(), player)
            setSlots("CareerMenu\$resonate_info", listOf(), player)

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it, demonstrating)
                }
            }
        }
    }

    val mark = NamespacedKey.minecraft("mark")

    @MenuComponent
    private val career_class = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val careerClass = args[1] as Class
            val data = loadCareerData(player)

            if (data.hasClass(careerClass.id)) {
                icon.textured(careerClass.skull)
            } else {
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZjOTExZGE2M2JlOTdlNjg5ODM1ZmFlNDM5OWI4ZDRiMjJjNGE2YzA5NjAxYTA0NzBjMjJmOTI5YWQ5NDVjZSJ9fX0=")
            }

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, careerClass.id)
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(careerClass.display())
                    "branches" -> careerClass.branchIds()
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, _) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val classId = item.itemMeta!!.persistentDataContainer.get(mark, PersistentDataType.STRING)

            open(player, classId)
        }
    }

    @MenuComponent
    private val branch = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val data = loadCareerData(player)

            if (data.getBranchLevel(branch) >= 0) {
                icon.textured(branch.skull)
            } else {
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZjOTExZGE2M2JlOTdlNjg5ODM1ZmFlNDM5OWI4ZDRiMjJjNGE2YzA5NjAxYTA0NzBjMjJmOTI5YWQ5NDVjZSJ9fX0=")
            }

            var state = "&e可解锁"
            if (!data.hasClass(branch.careerClass.id)) {
                state = "&c不可解锁(不是对应的职业)"
            }
            val level = data.getBranchLevel(branch.id)
            if (level >= 0) {
                state = "&a已解锁 &e${level}级"
            }

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, branch.id)
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
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val data = loadCareerData(player)
            val branchId = item.itemMeta!!.persistentDataContainer.get(mark, PersistentDataType.STRING)!!

            when (event.virtualEvent().clickType) {
                ClickType.LEFT ->
                    CareerBranchUI.open(player, branchId)

                ClickType.RIGHT -> {
                    val result = data.attemptToUnlockBranch(branchId)
                    player.sendMessage("§a生涯系统 §7>> " + result.second)
                    if (result.first)
                        open(player, args[0].toString())
                }

                else -> {}
            }
        }
    }

    @MenuComponent
    private val resonate = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val data = loadCareerData(player)

            var state = "&e可共鸣"
            icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0N2EzOTQ5OWRlNDllMjRjODkyYjA5MjU2OTQzMjkyN2RlY2JiNzM5OWUxMTg0N2YzMTA0ZmRiMTY1YjZkYyJ9fX0=")
            if (data.getBranchLevel(branch) < 0) {
                state = "&c不可共鸣"
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=")
            }
            if (data.resonantBranch == branch) {
                state = "&a正在共鸣"
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=")
            }

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, branch.id)
            }

            icon.variable("state", listOf(state))
        }
        onClick { (_, _, event, args) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val branchId = item.itemMeta!!.persistentDataContainer.get(mark, PersistentDataType.STRING)!!

            val result = Resonate.chooseResonate(player, branchId)
            player.sendMessage("§a生涯系统 §7>> " + result.second)
            if (result.first)
                open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val forget = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val data = loadCareerData(player)

            icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=")
            if (data.getBranchLevel(branch.id) < 0) {
                icon.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY5NTkwNThjMGMwNWE0MTdmZDc1N2NiODViNDQxNWQ5NjZmMjczM2QyZTdjYTU0ZjdiYTg2OGUzMjQ5MDllMiJ9fX0=")
            }

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, branch.id)
            }

            icon.variable("cost", listOf("${data.getBranchLevel(branch.id) * 2 + 2}"))
        }
        onClick { (_, _, event, args) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val branchId = item.itemMeta!!.persistentDataContainer.get(mark, PersistentDataType.STRING)!!

            val result = Forget.attemptToForget(player, branchId)
            player.sendMessage("§a生涯系统 §7>> " + result.second)
            if (result.first)
                open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val bind = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val data = loadCareerData(player)

            val list = data.shortCuts.map {
                it.value.display() + " &7- " + it.key
            }

            icon.variable("binds", list)
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            player.giveItem(DischargeHandler.item)
        }
    }

    @MenuComponent
    private val resonate_type = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val data = loadCareerData(player)
            val current = data.resonantType

            val list = ResonateType.values().map {
                (if (it == current) "&a" else "") + it.displayName
            }

            icon.variable("types", list)
        }
        onClick { (_, _, event, args) ->
            val player = event.clicker
            val data = loadCareerData(player)
            val values = ResonateType.values().toList()
            var index = values.indexOf(data.resonantType) + 1
            if (index >= values.size)
                index = 0
            data.resonantType = values[index]
            open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val resonate_info = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val data = loadCareerData(player)

            val list = Resonate.resonating[player.uniqueId]!!.map {
                it.key.display() + " " + it.value.second.toRoman() + "  &7来自 " + it.value.first
            }

            val my = data.resonantBranch?.display() ?: "N/A"

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