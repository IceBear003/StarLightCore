package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.attacker
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object Interact {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickBlock(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return
        if (!event.action.toString().contains("RIGHT"))
            return
        val type = event.clickedBlock!!.type
        val player = event.player
        handle(event, type, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickItem(event: PlayerInteractEvent) {
        if (!event.hasItem())
            return
        val type = event.item!!.type
        val player = event.player
        handle(event, type, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun damagebBlock(event: BlockDamageEvent) {
        val player = event.player
        handle(event, event.itemInHand.type, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun breakBlock(event: BlockBreakEvent) {
        val player = event.player
        handle(event, player.inventory.itemInMainHand.type, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun attack(event: EntityDamageByEntityEvent) {
        val player = event.attacker ?: return
        if (player !is Player)
            return
        handle(event, player.inventory.itemInMainHand.type, player)
    }

    fun handle(event: Cancellable, type: Material, player: Player) {
        val useResult = EventHandler.checkLimit(HandlerType.USE, player, type)
        if (!useResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法使用此物品/方块，需要解锁以下其中之一: ")
            useResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event as Event, type, HandlerType.USE, player)
    }
}