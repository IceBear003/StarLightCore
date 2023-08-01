package world.icebear03.starlight.career.spell.entry.scholar

import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.discharge
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.finish
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.hasBlockAside
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
        }

    fun initialize() {
        val tmp = listOf(Material.BOOKSHELF, Material.LECTERN)
        addLimit(HandlerType.PLACE, "教师" to 0, *tmp.toTypedArray())
        addLimit(HandlerType.CRAFT, "教师" to 0, *tmp.toTypedArray())

        "学术沙龙".discharge { name, level ->
            saloning[uniqueId] = 4 + 3 * level
            "${display(name)} §7释放成功，周围所有非敌对玩家共鸣范围增加"
        }.finish { _, _ ->
            saloning.remove(uniqueId)
            null
        }

        "诲人不倦".discharge { name, _ ->
            finish(name)
            if (hasBlockAside(Material.LECTERN)) {
                giveExp(20)
                "${display(name)} §7释放成功，获得§a20点§7经验"
            } else {
                "${display(name)} §7释放失败，因为周围没有讲台"
            }
        }
    }

    @SubscribeEvent
    fun click(event: PlayerInteractEvent) {
        //TODO 点书架等
    }
}