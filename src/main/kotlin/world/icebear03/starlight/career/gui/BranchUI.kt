package world.icebear03.starlight.career.gui

import org.bukkit.Material
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
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.getBranch
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import world.icebear03.starlight.utils.toRoman

@MenuComponent("Branch")
object BranchUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/branch.yml")
    }

    @Config("career/gui/branch.yml")
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
                    "Branch\$branch",
                    "Branch\$mine",
                    "Branch\$skill",
                    "Branch\$level",
                    "Branch\$eureka_guide",
                    "Branch\$eureka",
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val branch = getBranch(name)!!

            setSlots("Branch\$branch", listOf(), player, branch)
            setSlots("Branch\$mine", listOf(), player)

            branch.spells.filterValues { spell -> !spell.isEureka }.values.sortedBy { it.name }.let {
                setSlots("Branch\$skill", it, player, "element")
                setSlots("Branch\$level", it, player, "element=tot-tot/3*3", "expression=tot/3+1")
            }

            setSlots("Branch\$eureka_guide", listOf(), player, branch)
            branch.spells.filterValues { spell -> spell.isEureka }.values.sortedBy { it.name }.let {
                setSlots("Branch\$eureka", it, player, branch, "element")
            }

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it, branch)
                }
            }
        }
    }

    @MenuComponent
    private val branch = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val career = player.career()

            val level = career.getBranchLevel(branch)
            val state = if (level < 0) "&c未解锁" else "&a已解锁 &e${level}级"

            icon.textured(branch.skull)

            icon.variables {
                when (it) {
                    "display" -> listOf(branch.display())
                    "state" -> listOf(state)
                    "skills" -> branch.spellNames(skillOrEureka = true, displayed = true)
                    "eurekas" -> branch.spellNames(skillOrEureka = false, displayed = true)
                    else -> listOf()
                }
            }
        }
    }

    @MenuComponent
    private val mine = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val career = player.career()
            icon.variable("amount", listOf("${career.points}"))
        }
    }

    @MenuComponent
    private val skill = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val spell = args[1] as Spell
            val career = player.career()

            val state = if (!career.hasBranch(spell.branch)) "&c未解锁分支" else "&a已解锁 &e${career.getSpellLevel(spell)}级"

            icon.modifyMeta<ItemMeta> {
                this["mark", PersistentDataType.STRING] = spell.name
            }

            icon.textured(spell.skull)

            icon.variables {
                when (it) {
                    "display" -> listOf(spell.display())
                    "state" -> listOf(state)
                    else -> listOf()
                }
            }

            if (career.shortCuts.values.contains(spell.name)) {
                val shortcut = career.shortCuts.filterValues { it == spell.name }.toList()[0].first
                icon.modifyLore {
                    this.add(1, "§8| §7快捷键: §a$shortcut")
                }
            }

            if (spell.canAutoDischarge()) {
                icon.modifyLore {
                    val auto = if (career.autoDischarges.contains(spell.name)) "§a✔" else "§c✘"
                    this.add(1, "§8| §7自动释放: $auto")
                    this.add("§8| §6右击本按钮§7切换其自动释放模式")
                }
            } else icon
        }
        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val career = player.career()
            val name = item.itemMeta!!["mark", PersistentDataType.STRING]!!

            val click = event.clickEvent().click
            if (click.isLeftClick) {
                BindUI.open(player, name)
            }
            if (click.isRightClick) {
                player.sendMessage("§a生涯系统 §7>> " + career.switchAutoDischarge(name).second)
                open(player, args[0].toString())
            }
        }
    }

    @MenuComponent
    private val level = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val spell = args[1] as Spell
            val slotLevel = args[2] as Int
            val career = player.career()

            val level = career.getSpellLevel(spell)
            var state = "&c请先解锁上一等级"

            if (level == slotLevel - 1) {
                icon.type = Material.YELLOW_STAINED_GLASS_PANE
                state = "&e点击消耗技能点解锁"
            }
            if (level >= slotLevel) {
                icon.type = Material.GREEN_STAINED_GLASS_PANE
                state = "&a已解锁"
            }
            if (level == -1) {
                state = "&c请先解锁本分支"
            }

            icon.modifyMeta<ItemMeta> {
                this["mark", PersistentDataType.STRING] = "${spell.name}=$slotLevel"
            }

            icon.amount = slotLevel

            icon.variables {
                when (it) {
                    "display" -> listOf(spell.display())
                    "roman" -> listOf(slotLevel.toRoman())
                    "state" -> listOf(state)
                    "description" -> spell.description(slotLevel)
                    else -> listOf()
                }
            }
        }

        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val career = player.career()
            val string = item.itemMeta!!["mark", PersistentDataType.STRING]!!
            val name = string.split("=")[0]
            val level = string.split("=")[1].toInt()

            if (career.getSpellLevel(name) >= level || career.getSpellLevel(name) < level - 1) {
                return@onClick
            }

            player.sendMessage("§a生涯系统 §7>> " + career.upgradeSpell(name).second)
            open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val eureka_guide = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val career = player.career()

            val state =
                if (career.getBranchLevel(branch) < 10) {
                    if (career.getBranchLevel(branch) == 9) "&e可激活" else "&c未激活"
                } else {
                    val spell = branch.spells.values.filter { career.getSpellLevel(it) == 1 }.toList()[0]
                    "&a已激活 &e${spell.display()}"
                }

            icon.variables {
                when (it) {
                    "state" -> listOf(state)
                    else -> listOf()
                }
            }
        }
    }

    @MenuComponent
    private val eureka = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val spell = args[2] as Spell
            val career = player.career()

            val state = if (career.getSpellLevel(spell) == 1) "&a已激活"
            else if (career.getBranchLevel(branch) == 9) "&e可激活" else "&c不可激活"

            icon.textured(spell.skull)

            icon.modifyMeta<ItemMeta> {
                this["mark", PersistentDataType.STRING] = spell.name
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(spell.display())
                    "state" -> listOf(state)
                    "description" -> spell.description()
                    else -> listOf()
                }
            }
        }

        onClick { (_, _, event, args) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val player = event.clicker
            val career = player.career()
            val name = item.itemMeta!!["mark", PersistentDataType.STRING]!!

            if (career.getSpellLevel(name) == 1) {
                BindUI.open(player, name)
            } else {
                player.sendMessage("§a生涯系统 §7>> " + career.upgradeSpell(name).second)
                open(player, args[0].toString())
            }
        }
    }


    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, args) ->
            val branch = args[0] as Branch
            CareerUI.open(event.clicker, branch.clazz.name)
        }
    }
}