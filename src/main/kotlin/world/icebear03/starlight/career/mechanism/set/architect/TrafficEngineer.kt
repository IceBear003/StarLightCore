package world.icebear03.starlight.career.mechanism.set.architect

import org.bukkit.Material
import org.bukkit.entity.Boat
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.mechanism.limit.LimitType

object TrafficEngineerPassive {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: VehicleEnterEvent) {
        val entity = event.entered

        if (entity !is Player)
            return

        if (!entity.hasBranch("交通工程师"))
            return

        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed = vehicle.maxSpeed * 1.2
        if (vehicle is Minecart)
            vehicle.maxSpeed = vehicle.maxSpeed * 1.2
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: VehicleExitEvent) {
        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed = 0.4
        if (vehicle is Minecart)
            vehicle.maxSpeed = 0.4
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val type = item.type
        val player = event.player

        val level = player.getSkillLevel("路线规划")

        if (TrafficEngineerSet.RAILS.types.contains(type)) {
            val percent = when (level) {
                0, -1 -> 0.0
                1 -> 0.08
                else -> 0.15
            }
            if (Math.random() <= percent) {
                player.giveItem(ItemStack(type))
                player.sendMessage("本次放置未消耗物品")
            }
        }

        if (TrafficEngineerSet.ICE_BLOCKS.types.contains(type)) {
            if (level >= 3 && Math.random() <= 0.1) {
                player.giveItem(ItemStack(type))
                player.sendMessage("本次放置未消耗物品")
            }
        }
    }
}

enum class TrafficEngineerSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    RAILS(
        listOf(
            Material.RAIL,
            Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL,
            Material.POWERED_RAIL
        ), listOf(
            LimitType.CRAFT to ("堡垒工程师" to 0),
            LimitType.PLACE to ("堡垒工程师" to 0),
            LimitType.DROP_IF_BREAK to ("堡垒工程师" to 0)
        )
    ),
    MINECARTS(
        listOf(
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER_MINECART,
            Material.TNT_MINECART
        ), listOf(
            LimitType.CRAFT to ("堡垒工程师" to 0)
        )
    ),
    ICE_BLOCKS(
        listOf(
            Material.PACKED_ICE,
            Material.BLUE_ICE
        ),
        listOf(LimitType.PLACE to ("堡垒工程师" to 0))
    )
}