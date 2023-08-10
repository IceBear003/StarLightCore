package world.icebear03.starlight.station.core

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spellLevel
import world.icebear03.starlight.utils.*
import java.util.*

data class Station(
    val ownerId: UUID,
    var level: Int,
    var location: Location?,
    var stamp: Long
) {

    var mag = 1.0

    fun cooldown(): Int {
        return when (level) {
            1 -> 1800
            2 -> 3600
            3 -> 7200
            else -> 7200
        }
    }

    fun halo(): Double {
        return when (level) {
            1 -> 3.0
            2 -> 6.0
            3 -> 12.0
            else -> 3.0
        }
    }

    fun horizontal(): Int {
        return when (level) {
            1 -> 16
            2 -> 32
            3 -> 48
            else -> 16
        }
    }

    fun vertical(): Int {
        return when (level) {
            1 -> 72
            2 -> 144
            3 -> 512
            else -> 72
        }
    }

    fun checkStamp(): Pair<Boolean, Int> {
        val period = (System.currentTimeMillis() - stamp) / 1000
        val cooldown = cooldown()
        return (period >= cooldown) to cooldown - period.toInt()
    }

    fun destroy(player: Player): String {
        if (player.uniqueId != ownerId) {
            val owner = Bukkit.getOfflinePlayer(ownerId)
            deleteFromWorld()
            if (owner.isOnline) {
                (owner as Player).sendMessage("§b繁星工坊 §7>> 你的驻扎篝火被 §e${player.name} §7破坏")
            }
            return "破坏了 §e${owner.name} §7的驻扎篝火"
        }
        deleteFromWorld()
        player.giveItem(generateItem())
        return "成功回收驻扎篝火，物品已经收回背包(或掉落)"
    }

    fun generateItem(): ItemStack {
        return ItemStack(Material.SOUL_CAMPFIRE).modifyMeta<ItemMeta> {
            setDisplayName("§6驻扎篝火")
            lore = listOf(
                "§8| §7主人: §e" + ownerId.toName(),
                "§8| §7等级: §a" + level.toRoman(),
                "§8| §7范围: §b${horizontal()}格 §7(水平)",
                "       §b${vertical()}格 §7(垂直)",
                "§8| §7冷却: §6${cooldown()}s",
                "§7",
                "§8| §7放置于地面上以提供一定范围内的体力光环效果",
                "§8| §7注意: §c回收后重放置需要较长冷却时间",
                "§8| §7具体介绍和指导请看官方Wiki"
            )
            this["station_owner_id", PersistentDataType.STRING] = ownerId.toString()
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
        if (player.world.getHighestBlockYAt(loc) > loc.y && player.world.environment == World.Environment.NORMAL) {
            return false to "驻扎篝火必须放置于地表"
        }
        if (player.meetRequirement("山海一过")) {
            return false to "§d顿悟 ${display("山海一过")} §7使得你无法放置驻扎篝火"
        }
        val replace = checkStamp()
        return if (replace.first) {

            deleteFromWorld()
            location = loc
            val block = loc.block
            val pdc = block.loadPdc()
            pdc["station_owner_id"] = ownerId.toString()
            block.savePdc(pdc)

            val level = player.spellLevel("前进营地")
            if (level > 0) {
                when (level) {
                    1 -> mag = 1.1
                    2 -> mag = 1.2
                    3 -> mag = 1.3
                }
            }

            true to "放置驻扎篝火，开始生效..."
        } else {
            false to "重放置冷却中，还有§e${replace.second.secToFormattedTime()}"
        }
    }
}