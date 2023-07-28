package world.icebear03.starlight.station.core

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.utils.*
import java.util.*

data class Station(
    val ownerId: UUID,
    var level: Int,
    var location: Location?,
    var stamp: Long
) {

    fun checkStamp(): Pair<Boolean, Int> {
        val period = (System.currentTimeMillis() - stamp) / 1000
        return when (level) {
            1 -> (period >= 3600) to 3600 - period.toInt()
            2 -> (period >= 43200) to 43200 - period.toInt()
            3 -> (period >= 86400) to 86400 - period.toInt()
            else -> false to 0
        }
    }

    fun destroy(player: Player): Pair<Boolean, String> {
        if (player.uniqueId != ownerId) {
            val owner = Bukkit.getOfflinePlayer(ownerId)
            deleteFromWorld()
            if (owner.isOnline) {
                (owner as Player).sendMessage("§b繁星工坊 §7>> 你的驻扎篝火被 §e${player.name} §7破坏")
            }
            return true to "破坏了 §e${owner.name} §7的驻扎篝火"
        }
        val recycle = checkStamp()
        return if (recycle.first) {
            deleteFromWorld()
            player.giveItem(generateItem())
            true to "成功回收驻扎篝火，物品已经收回背包(或掉落)"
        } else {
            false to "回收冷却中，还有§e${recycle.second.secToFormattedTime()}"
        }
    }

    fun generateItem(): ItemStack {
        return ItemStack(Material.CAMPFIRE).modifyMeta<ItemMeta> {
            setDisplayName("§6驻扎篝火")
            lore = listOf(
                "§8| §7主人: §e" + ownerId.toName(),
                "§8| §7等级: §a" + level.toRoman(),
                "§7",
                "§8| §7放置于地面上以提供一定范围内的体力光环效果",
                "§8| §7注意: §c回收或重放置需要较长冷却时间",
                "§8| §7具体介绍和指导请看官方Wiki"
            )
            set("station_owner_id", PersistentDataType.STRING, ownerId.toString())
        }
    }

    fun deleteFromWorld() {
        location?.let { loc ->
            val block = loc.block
            block.type = Material.AIR
            val pdc = block.loadPdc()
            pdc.clear()
            block.savePdc(pdc)
        }

        location = null
        stamp = System.currentTimeMillis()
    }

    fun place(player: Player, loc: Location): Pair<Boolean, String> {
        if (player.uniqueId != ownerId) {
            return false to "无法放置不属于自己的驻扎篝火"
        }
        if (player.world.getHighestBlockYAt(loc) > loc.y) {
            return false to "驻扎篝火必须放置于地表"
        }
        val replace = checkStamp()
        return if (replace.first) {

            deleteFromWorld()
            location = loc
            val block = loc.block
            val pdc = block.loadPdc()
            pdc["station_owner_id"] = ownerId.toString()
            block.savePdc(pdc)

            true to "放置驻扎篝火，开始生效..."
        } else {
            false to "重放置冷却中，还有§e${replace.second.secToFormattedTime()}"
        }
    }
}