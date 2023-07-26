package world.icebear03.starlight.stamina

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getLocation
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.toBukkitLocation
import world.icebear03.starlight.utils.toRoman
import java.io.File
import java.util.*

object BeaconMechanism {
    val beacons = mutableMapOf<UUID, Beacon>()
    val byLocations = mutableMapOf<Location, Beacon>()

    val beaconKey = NamespacedKey.minecraft("beacon")

    fun initialize() {
        val directory = File(getDataFolder().absolutePath + "/stamina/beacons/")
        if (!directory.exists())
            directory.mkdir()
        directory.listFiles()?.forEach { file ->
            val config = Configuration.loadFromFile(file)
            val ownerId = UUID.fromString(file.name.replace(".yml", ""))
            val location = config.getLocation("location")?.toBukkitLocation()
            val lastRecycle = if (config.contains("last_recycle")) config.getLong("last_recycle") else null
            val level = config.getInt("level")
            val beacon = Beacon(ownerId, location, lastRecycle, level)
            beacons[ownerId] = beacon
            location?.let { byLocations[it] = beacon }
        }

        submit(period = 20L) {
            beacons.forEach { (_, beacon) ->
                beacon.location?.let { loc ->
                    val level = beacon.level
                    val range = when (level) {
                        2 -> 48
                        3 -> 144
                        else -> 16
                    }
                    val haloToOwner = when (level) {
                        2 -> 3.0
                        3 -> 6.0
                        else -> 12.0
                    }
                    val halo = when (level) {
                        2 -> 1.5
                        3 -> 3.0
                        else -> 6.0
                    }
                    loc.world!!.players.forEach { player ->

                        val playerLoc = player.location
                        val distance = playerLoc.distance(loc)
                        if (distance <= range) {
                            if (beacon.isBelongedTo(player))
                                player.addStamina(haloToOwner)
                            else
                                player.addStamina(halo)
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun place(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val block = event.block
        val beacon = fromItem(item) ?: return
        val player = event.player
        if (!beacon.isBelongedTo(player)) {
            event.isCancelled = true
            player.sendMessage("§b繁星工坊 §7>> 无法放置不属于自己的驻扎信标")
            return
        }
        beacon.location = block.location
        beacon.lastRecycle = System.currentTimeMillis()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun breakBlock(event: BlockBreakEvent) {
        val block = event.block
        val beacon = fromBlock(block) ?: return
        val player = event.player
        event.isDropItems = false
        beacon.location = null
        beacon.lastRecycle = System.currentTimeMillis()

        if (!beacon.isBelongedTo(player)) {
            player.sendMessage("§b繁星工坊 §7>> 破坏不属于自己的驻扎信标时无法回收")
            val owner = Bukkit.getOfflinePlayer(beacon.ownerId)
            if (owner.isOnline) {
                (owner as Player).sendMessage("§b繁星工坊 §7>> 驻扎信标被§e${player.name}破坏，请重新合成")
            }
            beacon.isAbandoned = true
            return
        } else {
            player.giveItem(generateItem(beacon))
            player.sendMessage("§b繁星工坊 §7>> 回收驻扎信标成功")
        }
    }

    fun fromBlock(block: Block): Beacon? {
        return byLocations[block.location]
    }

    fun fromItem(item: ItemStack): Beacon? {
        val string = item.itemMeta?.persistentDataContainer?.get(beaconKey, PersistentDataType.STRING) ?: return null
        val ownerId = UUID.fromString(string)
        return beacons[ownerId]
    }

    fun generateItem(beacon: Beacon): ItemStack {
        return ItemStack(Material.BEACON).modifyMeta<ItemMeta> {
            persistentDataContainer[beaconKey, PersistentDataType.STRING] = beacon.ownerId.toString()
            setDisplayName("§b驻扎信标")
            lore = listOf("§8| §7等级: §e${beacon.level.toRoman()}")
            lore = listOf("§8| §7被放置后，范围内玩家的体力得到回复")
            lore = listOf("§8| §7具体机制与数据见官方文档")
        }
    }
}