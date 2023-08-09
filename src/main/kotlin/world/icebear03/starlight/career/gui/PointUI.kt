package world.icebear03.starlight.career.gui

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
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
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career
import world.icebear03.starlight.career.display
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

    val otherWays = listOf(
        "钓鱼" to (listOf(
            "0.05%/次",
            "",
            "${display("农夫")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmRlOTE2MmY2MzBlZWMzZWMyZDVjMWE2OWY5YTc5YzFhNTExYjI3YTM2ZWZhOTg2NmMyYzBiOGFiNmJhZmExMyJ9fX0="),
        "附魔 (至少消耗2级)" to (listOf(
            "0.25%/次",
            "",
            "${display("学者")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RjOTg1YTdhNjhjNTc0ZjY4M2MwYjg1OTUyMWZlYjNmYzNkMmZmYTA1ZmEwOWRiMGJhZTQ0YjhhYzI5YjM4NSJ9fX0="),
        "挖矿" to (listOf(
            "&6远古残骸 &70.5%/次",
            "&b钻石&7/&a绿宝石 &70.1%/次",
            "&f其他 &70.01%/次",
            "",
            "${display("工人")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWVhOThlOGQ2YzU2NDJlMzJhYjU2OWRhYmY5M2NkNjkwNDYwYzQ5NDc3Y2E5YjlkYmY3MjU5YjM3OTUxZjJhZCJ9fX0="),
        "击杀" to (listOf(
            "&e动物 &70.015%/次",
            "&c怪物 &70.03%/次",
            "&dBoss &710%/次",
            "",
            "${display("战士")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBkZmM4YTM1NjNiZjk5NmY1YzFiNzRiMGIwMTViMmNjZWIyZDA0Zjk0YmJjZGFmYjIyOTlkOGE1OTc5ZmFjMSJ9fX0="),
        "放置" to (listOf(
            "0.002%/次",
            "",
            "${display("建筑师")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAxNmRlNjMwYWIyMGFmMjZiZDk1MjdkNjIyNzdiMTU1OGViZGY3MDk1OTdjMDM4ZmJjYTFlYjZhNWMwMWEzZiJ9fX0="),
        "食用" to (listOf(
            "0.02%/饱食度",
            "",
            "${display("厨师")} &7×2.0"
        ) to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI2NDIyNGUzYjg5NWRiMGYzYThlZDAzOTY3N2ZiMTJlOTU2ZWQ1ZTVhMTQ0YWJjMmZjOGVkNDE1NTFjYzJlYyJ9fX0="),
    )
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

            setSlots("Point\$other_way", otherWays, "expression=tot")
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
    private val other_way = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val way = otherWays[args[0] as Int]
            val name = way.first
            val lore = way.second.first
            val skull = way.second.second

            icon.textured(skull)

            icon.variables {
                when (it) {
                    "way" -> listOf(name)
                    "rates" -> lore
                    else -> listOf()
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
            val received =
                player["daily_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
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
        onClick { (_, _, event, _) ->
            val item = event.currentItem ?: return@onClick
            val index = item.itemMeta?.get("index", PersistentDataType.INTEGER) ?: return@onClick
            val player = event.clicker
            val reward = dailyRewards[index]
            val point = reward.second

            if (item.type == Material.YELLOW_STAINED_GLASS_PANE) {
                player.career().addPoint(point)
                var received =
                    player["daily_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
                received += index
                player["daily_rewards_received", PersistentDataType.INTEGER_ARRAY] = received
                player.sendMessage("§a生涯系统 §7>> 成功领取了§a${point}技能点")
                open(player)
            }
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
            val received =
                player["career_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
            var extra = false
            if (index == 3 && player.career().hasBranch("教师")) {
                extra = true
            }
            val state = (if (current >= need) {
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
            }) + if (extra) " &7(${display("教师")}&7额外&a+1&7)" else ""


            icon.modifyMeta<ItemMeta> {
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                this["index", PersistentDataType.INTEGER] = index
                this["extra", PersistentDataType.BOOLEAN] = extra
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
        onClick { (_, _, event, _) ->
            val item = event.currentItem ?: return@onClick
            val index = item.itemMeta?.get("index", PersistentDataType.INTEGER) ?: return@onClick
            val extra = item.itemMeta?.get("extra", PersistentDataType.BOOLEAN) ?: return@onClick
            val player = event.clicker
            val reward = careerRewards[index]
            val point = reward.second + (if (extra) 1 else 0)

            if (item.type == Material.YELLOW_STAINED_GLASS_PANE) {
                player.career().addPoint(point)
                var received =
                    player["career_rewards_received", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
                received += index
                player["career_rewards_received", PersistentDataType.INTEGER_ARRAY] = received
                player.sendMessage("§a生涯系统 §7>> 成功领取了§a${point}技能点")
                open(player)
            }
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            event.clicker.performCommand("sl")
        }
    }
}