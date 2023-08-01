package world.icebear03.starlight.career.gui

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
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
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.secToFormattedTime
import world.icebear03.starlight.utils.set

@MenuComponent("Point")
object PointUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/point.yml")
    }

    @Config("career/gui/point.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    val dailyRewards = listOf(
        30 * 60 to 1,
        60 * 60 to 1,
        120 * 60 to 1,
        240 * 60 to 1,
    )
    val dailyIndex = mapOf(
        0 to 3,
        1 to 2,
        2 to 1,
        3 to 0
    )
    val careerRewards = listOf(
        10 * 60 to 1,
        30 * 60 to 1,
        60 * 60 to 1,
        120 * 60 to 1,
        240 * 60 to 2,
        12 * 60 * 60 to 2,
        24 * 60 * 60 to 2,
        36 * 60 * 60 to 2,
        48 * 60 * 60 to 3,
        96 * 60 * 60 to 3,
        8 * 24 * 60 * 60 to 3,
        16 * 24 * 60 * 60 to 3
    )
    val careerIndex = mapOf(
        0 to 11,
        1 to 10,
        2 to 0,
        3 to 9,
        4 to 1,
        5 to 8,
        6 to 2,
        7 to 7,
        8 to 3,
        9 to 6,
        10 to 5,
        11 to 4,
    )

    fun open(player: Player) {
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
                    "Point\$other_way",
                    "Point\$daily_time",
                    "Point\$career_time"
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            setSlots("Point\$daily_time", dailyRewards, player, "expression=tot")
            setSlots("Point\$career_time", careerRewards, player, "expression=tot")

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it)
                }
            }
        }
    }

    @MenuComponent
    private val daily_time = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val index = dailyIndex[args[1] as Int]!!
            val reward = dailyRewards[index]
            val need = reward.first
            val point = reward.second

            val current = player["daily_time", PersistentDataType.INTEGER]!!
            val received = player["daily_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
            val state = if (current >= need) {
                if (received.contains(index)) {
                    icon.type = Material.GREEN_STAINED_GLASS_PANE
                    "&a已领取"
                } else {
                    icon.type = Material.YELLOW_STAINED_GLASS_PANE
                    icon.addUnsafeEnchantment(Enchantment.MENDING, 1)
                    "&e可领取"
                }
            } else {
                icon.type = Material.RED_STAINED_GLASS_PANE
                "&c不可领取"
            }


            icon.modifyMeta<ItemMeta> {
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                this["index", PersistentDataType.INTEGER] = index
            }

            icon.variables {
                when (it) {
                    "reward" -> listOf("&a${point}技能点")
                    "state" -> listOf(state)
                    "time" -> listOf(need.secToFormattedTime())
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, args) ->

        }
    }

    @MenuComponent
    private val career_time = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val index = careerIndex[args[1] as Int]!!
            val reward = careerRewards[index]
            val need = reward.first
            val point = reward.second

            val current = player["career_time", PersistentDataType.INTEGER]!!
            val received = player["career_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
            val state = if (current >= need) {
                if (received.contains(index)) {
                    icon.type = Material.GREEN_STAINED_GLASS_PANE
                    "&a已领取"
                } else {
                    icon.type = Material.YELLOW_STAINED_GLASS_PANE
                    icon.addUnsafeEnchantment(Enchantment.MENDING, 1)
                    "&e可领取"
                }
            } else {
                icon.type = Material.RED_STAINED_GLASS_PANE
                "&c不可领取"
            }


            icon.modifyMeta<ItemMeta> {
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                this["index", PersistentDataType.INTEGER] = index
            }

            icon.variables {
                when (it) {
                    "reward" -> listOf("&a${point}技能点")
                    "state" -> listOf(state)
                    "time" -> listOf(need.secToFormattedTime())
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, args) ->

        }
    }

    @MenuComponent
    private val other_way = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            icon
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            event.clicker.performCommand("bs 主菜单")
        }
    }
}