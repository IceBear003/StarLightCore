package world.icebear03.starlight.career.gui

import org.bukkit.entity.Player
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
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.display
import world.icebear03.starlight.utils.YamlUpdater
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set
import java.util.*

@MenuComponent("Choose")
object ChooseUI {

    init {
        YamlUpdater.loadAndUpdate("career/gui/choose.yml")
    }

    @Config("career/gui/choose.yml")
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
                    "Choose\$choice"
                ) { slot, index, item, _ ->
                    inventory.setItem(slot, item(slot, index))
                }
            }

            setSlots("Choose\$choice", listOf(), player, "expression=tot")

            onClick {
                it.isCancelled = true
                if (it.rawSlot in shape) {
                    templates[it.rawSlot]?.handle(it)
                }
            }
        }
    }

    val choicesMap = mutableMapOf<UUID, List<Class>>()

    @MenuComponent
    private val choice = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val player = args[0] as Player
            val index = args[1] as Int
            val career = player.career()
            val choices = choicesMap[player.uniqueId] ?: career.chooseList()
            choicesMap[player.uniqueId] = choices

            val choice = choices[index]

            icon.textured(choice.skull)

            icon.modifyMeta<ItemMeta> {
                this["mark", PersistentDataType.STRING] = choice.name
            }

            icon.variables {
                when (it) {
                    "display" -> listOf(choice.display())
                    "branches" -> choice.branchNames(true)
                    else -> listOf()
                }
            }
        }
        onClick { (_, _, event, _) ->
            val item = event.clickEvent().currentItem ?: return@onClick
            val name = item.itemMeta!!["mark", PersistentDataType.STRING] ?: return@onClick
            val player = event.clicker
            val career = player.career()

            if (career.canChoose()) {
                career.addClass(name)
                choicesMap.remove(player.uniqueId)
                player.sendMessage("§a生涯系统 §7>> 你选择了职业 ${display(name)}")
                player.sendMessage("§a生涯系统 §7>> 加上随机抽取的两个，你现在拥有一下职业:")
                career.classes.keys.forEach {
                    player.sendMessage("               §7|—— ${it.display()}")
                }
                player.sendMessage("§a生涯系统 §7>> 打开生涯面板可查看详细信息，解锁升级更多能力")
            } else {
                player.sendMessage("§a生涯系统 §7>> 你已经选择过职业了")
            }
            player.closeInventory()
        }
    }
}