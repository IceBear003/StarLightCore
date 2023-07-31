package world.icebear03.starlight.tool.mechanism

import org.bukkit.event.player.PlayerItemMendEvent
import org.bukkit.inventory.meta.Damageable
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta

object WeakerMending {
    @SubscribeEvent
    fun mend(event: PlayerItemMendEvent) {
        event.isCancelled = true
        val item = event.item
        item.modifyMeta<Damageable> {
            this.damage = maxOf(this.damage - 1, 0)
        }
    }
}