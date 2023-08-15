package world.icebear03.starlight.career.spell.entry.scholar

import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import taboolib.platform.util.isMainhand
import taboolib.platform.util.isRightClick
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.set
import java.util.*

object Teacher {

    val saloning = mutableMapOf<UUID, Int>()

    val skillBook = ItemStack(Material.PLAYER_HEAD)
        .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjZGVlNmQwNmRmMjM0YjhlNjAzMzI4Yjk2YzU3ZjNhMzEyZTc5YWFiZmMzYmU3MmE4YjQyMTg3OGVkNjhjZiJ9fX0=")
        .modifyMeta<ItemMeta> {
            setDisplayName("§b技能之书")
            lore = listOf(
                "§8| §7右击消耗可获得§a1技能点",
                "§8| §7注意: 选择教师分支的玩家§c无法使用"
            )
            this["custom_item", PersistentDataType.STRING] = "career_skill_book"
        }

    fun initialize() {
        val tmp = listOf(Material.BOOKSHELF, Material.CHISELED_BOOKSHELF, Material.LECTERN)
        addLimit(HandlerType.PLACE, "教师" to 0, *tmp.toTypedArray())
        addLimit(HandlerType.CRAFT, "教师" to 0, *tmp.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "教师" to 0, Material.LECTERN)

        "学术沙龙".discharge { name, level ->
            saloning[uniqueId] = 4 + 3 * level
            "§a技能 ${display(name)} §7释放成功，周围所有非敌对玩家共鸣范围增加"
        }.finish { _, _ ->
            saloning.remove(uniqueId)
            null
        }

        "诲人不倦".discharge { name, _ ->
            finish(name)
            if (hasBlockAside(Material.LECTERN)) {
                giveExp(20)
                "§d顿悟 ${display(name)} §7释放成功，获得§a20点§7经验"
            } else {
                fail(name)
                "§d顿悟 ${display(name)} §7释放失败，因为周围没有讲台"
            }
        }

        "厚积薄发".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下次通过书架制作技能之书时有几率消耗更少的技能点"
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun click(event: PlayerInteractEvent) {
        if (!event.isMainhand())
            return
        if (!event.hasItem())
            return
        if (!event.isRightClick())
            return
        val player = event.player
        val item = event.item!!
        if (item.itemMeta?.get("custom_item", PersistentDataType.STRING) == "career_skill_book") {
            event.isCancelled = true
            if (player.meetRequirement("教师", 0)) {
                player.sendMessage("§a生涯系统 §7>> 教师不能使用技能之书")
                return
            }
            if (item.amount > 1) {
                item.amount -= 1
            } else {
                submit {
                    player.inventory.setItemInMainHand(null)
                }
            }
            player.career().addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 从技能之书中获得了§a1技能点")
        }
        val career = player.career()
        if (item.type == Material.BOOK && event.hasBlock() && player.isSneaking && player.meetRequirement("教师", 0)) {
            if (!career.spells.any { it.key.isEureka }) {
                player.sendMessage("§a生涯系统 §7>> 制作技能之书至少需要激活一个顿悟")
                return
            }
            val block = event.clickedBlock!!
            if (block.type == Material.LECTERN) {
                val levelCost = when (player.spellLevel("教育学", false)) {
                    1 -> 37
                    2 -> 34
                    3 -> 30
                    else -> 40
                }
                if (player.level >= levelCost && career.points >= 1) {
                    career.points -= 1
                    player.level -= levelCost
                    if (item.amount > 1) {
                        item.amount -= 1
                    } else player.inventory.setItemInMainHand(null)
                    player.giveItem(skillBook.clone())
                    player.sendMessage("§a生涯系统 §7>> 你消耗§a1技能点§7和§a${levelCost}级经验§7制作了一本技能之书")
                } else {
                    player.sendMessage("§a生涯系统 §7>> 缺少技能点或经验，无法制作技能之书")
                }
            }
            if (block.type == Material.BOOKSHELF || block.type == Material.CHISELED_BOOKSHELF) {
                val rate = if (player.isDischarging("厚积薄发")) {
                    player.finish("厚积薄发")
                    player.spellLevel("厚积薄发") * 0.1
                } else -1.0
                val cost = if (Math.random() <= rate) 1 else 2
                if (career.points >= cost) {
                    career.points -= cost
                    if (item.amount > 1) {
                        item.amount -= 1
                    } else player.inventory.setItemInMainHand(null)
                    player.giveItem(skillBook.clone())
                    player.sendMessage("§a生涯系统 §7>> 你消耗§a${cost}技能点§7制作了一本技能之书")
                } else {
                    player.sendMessage("§a生涯系统 §7>> 缺少技能点，无法制作技能之书")
                }
            }
        }
    }
}