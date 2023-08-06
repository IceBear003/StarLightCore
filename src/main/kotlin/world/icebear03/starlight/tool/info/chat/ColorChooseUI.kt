package world.icebear03.starlight.tool.info.chat

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.function.textured
import org.serverct.parrot.parrotx.function.variable
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.compileToJexl
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.performAsOp
import world.icebear03.starlight.utils.set
import java.util.*
import kotlin.math.roundToInt

@MenuComponent("ColorChoose")
object ColorChooseUI {

    val setting = mutableMapOf<UUID, String>()

    @Config("tool/color_choose.yml")
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
                    "ColorChoose\$legacy", "ColorChoose\$hex",
                    "ColorChoose\$rainbow", "ColorChoose\$gradients",
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            setSlots("ColorChoose\$legacy", listOf(), player, "expression=tot+1")
            setSlots("ColorChoose\$hex", listOf(), player)
            setSlots("ColorChoose\$rainbow", listOf(), player)
            setSlots("ColorChoose\$gradients", listOf(), player)

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it)
                }
            }
        }
    }

    @MenuComponent
    private val legacy = MenuFunctionBuilder {
        onBuild { (_, _, _, _, _, args) ->
            val player = args[0] as Player
            val index = args[1] as Int
            val pair = when (index) {
                1 -> "&1" to "&1深蓝色"
                2 -> "&2" to "&2深绿色"
                3 -> "&3" to "&3湖蓝色"
                4 -> "&4" to "&4暗红色"
                5 -> "&5" to "&5紫色"
                6 -> "&6" to "&6橙色"
                7 -> "&7" to "&7浅灰色"
                8 -> "&8" to "&8深灰色"
                9 -> "&9" to "&9海蓝色"
                10 -> "&a" to "&a绿色"
                11 -> "&b" to "&b天青色"
                12 -> "&c" to "&c红色"
                13 -> "&d" to "&d品红色"
                14 -> "&e" to "&e黄色"
                else -> "&f" to "&f白色"
            }
            val skull = when (index) {
                1 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0YmJiYWMyNDkwM2E3ZjMxODk5NDk4ZTE4NDU2YzJhYTg3NTI5ZWExNDBmMWM2NmEyMmZkZGY4YWM2ZWZmNiJ9fX0="
                2 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0YmJiYWMyNDkwM2E3ZjMxODk5NDk4ZTE4NDU2YzJhYTg3NTI5ZWExNDBmMWM2NmEyMmZkZGY4YWM2ZWZmNiJ9fX0="
                3 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk5MTQ2ODkxYTY5ZjA2YWFiOWY5ODY1NTJmNmQwNDYwMjI2NmMwMDc1OWI2ZGI1MzJhMjg3MmZjZGExZWE0In19fQ=="
                4 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkwZDNjNTIyY2U1MzNiNjU5NzU1NmMwNmQ4MGFlYmViMjY0MGVlMGQxYmY4YzQ5ODc0MGE3NjBkOGI3YzExZCJ9fX0="
                5 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA2ODUyMTAxZDM0YTI3ZTZlODAzNGVhZjY5NWIwY2E0MmVjZGNlNjhkMWI2N2I4OGNjYjI1Mjg1MGRhYTNjNSJ9fX0="
                6 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZhZTYzYTkxZmU0OTJlOTE1OTE4ODk3OTczNmI1N2JmNTg1MDk1ODVhNGQwODk5MmE5NzZmOThjODhlY2VmMSJ9fX0="
                7 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWEyODYzMjU4ZGEwM2E0MDYyY2IwN2ZhMDIxZWEwM2RiOGJlNDI3OGM1NjJiMGEyYmY4Yjg5YjhiMDA1ZDUxNSJ9fX0="
                8 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNmZTQwYWFkOTllNDBmNDVkYmNjZjJlMmFjMDg1OWU3ZjFlYTg5MzRlMzc1ODEzYzU0YmE3ZDg1NDc4MzU3YyJ9fX0="
                9 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk5MTQ2ODkxYTY5ZjA2YWFiOWY5ODY1NTJmNmQwNDYwMjI2NmMwMDc1OWI2ZGI1MzJhMjg3MmZjZGExZWE0In19fQ=="
                10 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ3YzgxZjlkMzU2ZDIzOTAzMTA0YWFiYzE4NjkzZTI0MjEzMDU2YWE1ZTJhNmY1ODJjOTU4MDcxZWU5NTU4OCJ9fX0="
                11 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFjODg1M2JmMmMwZjRhYTc4ZGM2ZDBjY2Q1ZTZjNWU5NzY3MGJjNmYyNWQ5MzZjMWZjMmE0NGVkNjQzOTczYiJ9fX0="
                12 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkwZDNjNTIyY2U1MzNiNjU5NzU1NmMwNmQ4MGFlYmViMjY0MGVlMGQxYmY4YzQ5ODc0MGE3NjBkOGI3YzExZCJ9fX0="
                13 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI3OGVlYmYzYzE3ZGRkMzg5MmEzZDk1MjBlMzhkZWNmNzg1ZjA5ZjI2MDFkM2U0ODAwMWExYjhkNDhlYmYxZiJ9fX0="
                14 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxMzEyOWI4MjRjNGQ3YjYzMDM0YTFlZjA3NTVmNzgxODM1MzFkYWI1NDA2MmIzNGIyMjRmNGY4NTE4ZjJhZiJ9fX0="
                else -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ4NjdkOTBlZWU1ZTczY2U2MzJhZTY2NjMwZTkyMWQxZmVjOTRlZjFiMzIzZjI4ZjM5YTk4NmZiYmI4NjhiZSJ9fX0="
            }

            var state = "§a✔ §7可选择"
            if (ColoredChat.getColor(player) == pair.first)
                state = "§e✷ §7已选择"
            if (!player.hasPermission("trchat.color.legacy"))
                state = "§c✘ §7尚未获得该聊天颜色"

            ItemStack(Material.PLAYER_HEAD)
                .textured(skull)
                .modifyMeta<ItemMeta> {
                    this["color", PersistentDataType.STRING] = pair.first
                    setDisplayName(("&7原版颜色 - " + pair.second).colored())
                    lore = listOf(
                        "§8| §7点击选择该颜色作为聊天颜色",
                        "§7",
                        "§8| §7$state",
                    )
                }
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            val color = event.currentItem?.itemMeta?.get("color", PersistentDataType.STRING) ?: return@onClick
            if (player.hasPermission("trchat.color.legacy")) {
                ColoredChat.setColor(player, color)
                player.sendMessage("§b繁星工坊 §7>> 选择了${color.colored()}✷§7作为聊天颜色")
                open(player)
            }
        }
    }

    @MenuComponent
    private val hex = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val color = ColoredChat.getColor(player)

            var state = "§a✔ §7可选择"
            if (color.count { it == '#' } == 1) {
                state = "${color.colored()}✷ §7已选择"
            }
            if (!player.hasPermission("trchat.color.hex"))
                state = "§c✘ §7尚未获得该聊天颜色"

            icon.variable("state", listOf(state))
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            if (player.hasPermission("trchat.color.hex")) {
                player.sendMessage("§b繁星工坊 §7>> 请输入目标颜色对应的HEX代码:")
                player.closeInventory()
                setting[player.uniqueId] = "hex"
            }
        }
    }

    @MenuComponent
    private val rainbow = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val color = ColoredChat.getColor(player)

            var state = "§a✔ §7可选择"
            if (color.contains("<r:")) {
                val value = color.replace("<r:", "").replace(">", "").toDouble()
                state = "§e✷ §7已选择 (饱和度 §a" + (value * 100).roundToInt() + "%§7)"
            }
            if (!player.hasPermission("trchat.color.hex"))
                state = "§c✘ §7尚未获得该聊天颜色"

            icon.variable("state", listOf(state))
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            if (player.hasPermission("trchat.color.rainbow")) {
                player.sendMessage("§b繁星工坊 §7>> 请输入0-100的一个整数作为色彩饱和度:")
                player.closeInventory()
                setting[player.uniqueId] = "rainbow"
            }
        }
    }

    @MenuComponent
    private val gradients = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val color = ColoredChat.getColor(player)

            var state = "§a✔ §7可选择"
            if (!player.hasPermission("trchat.color.hex"))
                state = "§c✘ §7尚未获得该聊天颜色"
            if (color.count { it == '#' } == 2) {
                val tmp = color.replace("<g:", "").replace(">", "").replace("#", "")
                val first = "&{#${tmp.split(":")[0]}"
                val second = "&{#${tmp.split(":")[1]}"
                state = "${first.colored()}✷§7→${second.colored()}✷ §7已选择"
            }

            icon.variable("state", listOf(state))
        }
        onClick { (_, _, event, _) ->
            val player = event.clicker
            if (player.hasPermission("trchat.color.gradients")) {
                player.sendMessage("§b繁星工坊 §7>> 请输入目标渐变色对应的初末HEX代码，用逗号隔开:")
                player.closeInventory()
                setting[player.uniqueId] = "gradients"
            }
        }
    }

    @MenuComponent
    private val back = MenuFunctionBuilder {
        onClick { (_, _, event, _) ->
            event.clicker.performAsOp("sl")
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun chat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val uuid = player.uniqueId
        if (!setting.contains(uuid))
            return
        val msg = event.message
        val type = setting[uuid]
        setting.remove(uuid)
        event.isCancelled = true
        when (type) {
            "hex" -> {
                val color = "&{#$msg}"
                if (color.colored().length != color.length) {
                    ColoredChat.setColor(player, color)
                    player.sendMessage("§b繁星工坊 §7>> 选择了${color.colored()}✷§7作为聊天颜色")
                } else player.sendMessage("§b繁星工坊 §7>> 请输入正确的HEX代码")
                submit {
                    open(player)
                }
            }

            "rainbow" -> {
                try {
                    val value = msg.toInt() / 100.0
                    val color = "<r:$value>"
                    ColoredChat.setColor(player, color)
                    player.sendMessage("§b繁星工坊 §7>> 选择了§c彩§e虹§b色✷§7作为聊天颜色")
                    submit {
                        open(player)
                    }
                } catch (e: Exception) {
                    player.sendMessage("§b繁星工坊 §7>> 请输入正确的饱和度")
                }
            }

            "gradients" -> {
                try {
                    val first = msg.split(",")[0]
                    val second = msg.split(",")[1]
                    val firstColor = "&{#$first}"
                    val secondColor = "&{#$second}"
                    if (firstColor.colored().length != first.length && secondColor.colored().length != second.length) {
                        ColoredChat.setColor(player, "<g:#$first:#$second>")
                        player.sendMessage("§b繁星工坊 §7>> 选择了${firstColor.colored()}✷§7→${secondColor.colored()}✷§7作为聊天颜色")
                        submit {
                            open(player)
                        }
                    } else player.sendMessage("§b繁星工坊 §7>> 请输入正确的初末HEX代码")
                } catch (e: Exception) {
                    player.sendMessage("§b繁星工坊 §7>> 请输入正确的初末HEX代码")
                }
            }
        }
    }
}