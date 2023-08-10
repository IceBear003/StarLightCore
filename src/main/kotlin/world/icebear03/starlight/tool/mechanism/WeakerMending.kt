package world.icebear03.starlight.tool.mechanism

import org.bukkit.enchantments.Enchantment
import org.bukkit.event.player.PlayerItemMendEvent
import org.bukkit.inventory.meta.Damageable
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta

object WeakerMending {
    @SubscribeEvent
    fun mend(event: PlayerItemMendEvent) {
        event.isCancelled = true
        val item = event.item
        val level = item.getEnchantmentLevel(Enchantment.MENDING)
        if (level > 0)
            item.modifyMeta<Damageable> {
                this.damage = maxOf(this.damage - level, 0)
            }
        else event.isCancelled = true
    }
}