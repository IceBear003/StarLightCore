package world.icebear03.starlight.other

import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.StorageMinecart
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import world.icebear03.starlight.career
import world.icebear03.starlight.utils.has
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

object DeathChest {

    val ownerKey = NamespacedKey.minecraft("death_chest_owner")
    val expKey = NamespacedKey.minecraft("death_chest_exp")
    val pointKey = NamespacedKey.minecraft("death_chest_point")

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        val loc = player.location
        val items = mutableListOf<ItemStack>()
        val inv = player.inventory
        for (slot in 0 until inv.size) {
            val item = player.inventory.getItem(slot) ?: continue
            val meta = item.itemMeta ?: continue
            if (!meta.has("station_owner_id", PersistentDataType.STRING))
                items += item
        }

        val minecart = player.world.spawnEntity(loc, EntityType.MINECART_CHEST) as StorageMinecart

        var point = player.career().points
        player.career().branches.keys.forEach {
            point += player.career().getBranchLevel(it)
        }
        if (point <= 3) point = 0

        submit {
            items.shuffle()
            items.forEach {
                minecart.inventory.addItem(it)
            }
            minecart.isGlowing = true
            minecart.isCustomNameVisible = true
            minecart.customName = "§e${player.name}的遗物 §r关闭时自动消失"
            minecart.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, player.name)
            minecart.persistentDataContainer.set(expKey, PersistentDataType.INTEGER, getTotalExp(player.level))
            minecart.persistentDataContainer.set(pointKey, PersistentDataType.INTEGER, ceil(point * 0.25).roundToInt())
            minecart.isInvulnerable = true
            minecart.setGravity(false)
        }

        event.keepInventory = true
        event.keepLevel = true

        player.sendMessage("§b繁星工坊 §7>> 死亡掉落物品已被收集在原地的容器中 坐标 x:§e${loc.blockX} §7y:§e${loc.blockY} §7z:§e${loc.blockZ}")

        submit(delay = 3L) {
            if (player.isDead) {
                player.spigot().respawn()

                player.level = 0
                player.exp = 0f
                player.inventory.clear()
            }
        }
    }

    val opening = mutableListOf<UUID>()

    @SubscribeEvent
    fun close(event: InventoryCloseEvent) {
        if (!opening.contains(event.player.uniqueId))
            return
        opening.remove(event.player.uniqueId)

        val inv = event.inventory
        val minecart = inv.holder
        if (minecart !is StorageMinecart)
            return
        minecart.remove()
    }

    @SubscribeEvent
    fun open(event: InventoryOpenEvent) {
        val inv = event.inventory
        val minecart = inv.holder
        if (minecart !is StorageMinecart)
            return

        val pdc = minecart.persistentDataContainer
        if (!pdc.has(ownerKey, PersistentDataType.STRING))
            return

        val player = event.player as Player

        opening += player.uniqueId

        val exp = maxOf(0, pdc.get(expKey, PersistentDataType.INTEGER)!!)
        minecart.persistentDataContainer.set(expKey, PersistentDataType.INTEGER, 0)
        player.giveExp(exp)

        val point = maxOf(0, pdc.get(pointKey, PersistentDataType.INTEGER)!!)
        minecart.persistentDataContainer.set(pointKey, PersistentDataType.INTEGER, 0)
        if (point != 0) {
            player.career().addPoint(point)
            player.sendMessage("§b繁星工坊 §7>> 你从死亡箱中找到了 §a${point}技能点")
        }
    }


    fun getExpPerLevelInVanilla(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    fun getTotalExp(level: Int): Int {
        var exp = 0
        for (i in 0 until level) {
            exp += getExpPerLevelInVanilla(i)
        }
        return exp
    }
}