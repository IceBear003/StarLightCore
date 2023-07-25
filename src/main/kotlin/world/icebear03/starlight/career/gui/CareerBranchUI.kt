package world.icebear03.starlight.career.gui

import org.bukkit.Material
import org.bukkit.NamespacedKey
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
import taboolib.module.kether.isInt
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.nextChat
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.loadCareerData
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.toRoman

@MenuComponent("CareerBranch")
object CareerBranchUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/career_branch.yml")
    }

    @Config("career/gui/career_branch.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player, branchId: String) {
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
                    "CareerBranch\$branch",
                    "CareerBranch\$mine",
                    "CareerBranch\$skill",
                    "CareerBranch\$level",
                    "CareerBranch\$eureka_guide",
                    "CareerBranch\$eureka",
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            val data = loadCareerData(player)
            val demonstrating = Branch.fromId(branchId)!!

            setSlots("CareerBranch\$branch", listOf(), player, demonstrating)
            setSlots("CareerBranch\$mine", listOf(), player)

            val skills = demonstrating.skills.toList().sortedBy { it.id }
            setSlots("CareerBranch\$skill", skills, player, "element")
            setSlots("CareerBranch\$level", skills, player, "element=tot-tot/3*3", "expression=tot/3+1")

            val eurekas = demonstrating.eurekas.toList().sortedBy { it.id }
            setSlots("CareerBranch\$eureka_guide", listOf(), player, demonstrating)
            setSlots("CareerBranch\$eureka", eurekas, player, demonstrating, "element")

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
    private val branch = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val data = loadCareerData(player)

            val level = data.getBranchLevel(branch)
            val state =
                if (level < 0) {
                    "&c未解锁"
                } else {
                    "&a已解锁 &e${level}级"
                }

            icon.textured(branch.skull)

            icon.variables {
                when (it) {
                    "display" -> listOf(branch.display())
                    "state" -> listOf(state)
                    "description" -> branch.description
                    "skills" -> branch.skillIds()
                    "eurekas" -> branch.eurekaIds()
                    else -> listOf()
                }
            }
        }
    }

    @MenuComponent
    private val mine = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val data = loadCareerData(player)
            icon.variable("amount", listOf("${data.points}"))
        }
    }

    @MenuComponent
    private val skill = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val skill = args[1] as Skill
            val data = loadCareerData(player)

            val level = data.getSkillLevel(skill)
            val state =
                if (level < 0) {
                    "&c未解锁分支"
                } else {
                    "&a已解锁 &e${data.getSkillLevel(skill)}级"
                }

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, skill.id)
            }

            icon.textured(skill.skull)

            icon.variables {
                when (it) {
                    "display" -> listOf(skill.display())
                    "level" -> listOf(state)
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, _) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val data = loadCareerData(player)
            val id = item.itemMeta!!.persistentDataContainer.get(CareerMenuUI.mark, PersistentDataType.STRING)!!

            player.closeInventory()
            player.sendMessage("§a生涯系统 §7>> 请输入§a1-9§7中的一个数字，将这个技能绑定到§e对应的按键")
            player.nextChat {
                if (!it.isInt()) {
                    player.sendMessage("§a生涯系统 §7>> 你输入的不是数字")
                }
                if (it.toInt() !in 1..9) {
                    player.sendMessage("§a生涯系统 §7>> 你输入的不是§a1-9§7中的数字")
                }
                player.sendMessage("§a生涯系统 §7>> " + data.attemptToAddSkillToShortCut(id, it.toInt()).second)
            }
        }
    }

    @MenuComponent
    private val level = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val skill = args[1] as Skill
            val slotLevel = args[2] as Int
            val data = loadCareerData(player)

            val level = data.getSkillLevel(skill)
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
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, "${skill.id}=$slotLevel")
            }

            icon.amount = slotLevel

            icon.variables {
                when (it) {
                    "display" -> listOf(skill.display())
                    "roman" -> listOf(slotLevel.toRoman())
                    "state" -> listOf(state)
                    "description" -> skill.level(slotLevel).description
                    else -> listOf()
                }
            }
        }

        onClick { (_, _, event, args) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val data = loadCareerData(player)
            val string = item.itemMeta!!.persistentDataContainer.get(CareerMenuUI.mark, PersistentDataType.STRING)!!
            val id = string.split("=")[0]
            val level = string.split("=")[1].toInt()

            if (data.getSkillLevel(id) >= level || data.getSkillLevel(id) < level - 1) {
                return@onClick
            }

            val result = data.attemptToUpgradeSkill(id)
            player.sendMessage("§a生涯系统 §7>> " + result.second)
            if (result.first)
                open(player, args[0].toString())
        }
    }

    @MenuComponent
    private val eureka_guide = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val branch = args[1] as Branch
            val data = loadCareerData(player)

            val state =
                if (data.getBranchLevel(branch) < 10) {
                    if (data.getBranchLevel(branch) == 9) {
                        "&e可激活"
                    } else {
                        "&c未激活"
                    }
                } else {
                    val eureka = data.eurekas.filter { branch.eurekas.contains(it) }[0]
                    "&a已激活 &e${eureka.display()}"
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
            val eureka = args[2] as Eureka
            val data = loadCareerData(player)

            val state =
                if (data.hasEureka(eureka)) {
                    "&a已激活"
                } else {
                    if (data.getBranchLevel(branch.id) == 9) {
                        "&e可激活"
                    } else {
                        "&c不可激活"
                    }
                }

            icon.textured(eureka.skull)

            icon.modifyMeta<ItemMeta> {
                this.persistentDataContainer.set(mark, PersistentDataType.STRING, eureka.id)
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(eureka.display())
                    "state" -> listOf(state)
                    "description" -> eureka.description
                    else -> listOf()
                }
            }
        }

        onClick { (_, _, event, args) ->
            val item = event.virtualEvent().clickItem
            val player = event.clicker
            val data = loadCareerData(player)
            val id = item.itemMeta!!.persistentDataContainer.get(CareerMenuUI.mark, PersistentDataType.STRING)!!

            if (data.hasEureka(id)) {
                player.closeInventory()
                player.sendMessage("§a生涯系统 §7>> 请输入§a1-9§7中的一个数字，将这个顿悟绑定到§e对应的按键")
                player.nextChat {
                    if (!it.isInt()) {
                        player.sendMessage("§a生涯系统 §7>> 你输入的不是数字")
                    }
                    if (it.toInt() !in 1..9) {
                        player.sendMessage("§a生涯系统 §7>> 你输入的不是§a1-9§7中的数字")
                    }
                    player.sendMessage("§a生涯系统 §7>> " + data.attemptToAddEurekaToShortCut(id, it.toInt()).second)
                }
            } else {
                val result = data.attemptToEureka(id)
                player.sendMessage("§a生涯系统 §7>> " + result.second)
                if (result.first)
                    open(player, args[0].toString())
            }
        }
    }


    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, args) ->
            val branch = args[0] as Branch
            CareerMenuUI.open(event.clicker, branch.careerClass.id)
        }
    }
}