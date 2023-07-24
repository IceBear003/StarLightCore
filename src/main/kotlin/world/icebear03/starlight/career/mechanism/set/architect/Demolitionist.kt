package world.icebear03.starlight.career.mechanism.set.architect

import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.mechanism.displayLimit
import world.icebear03.starlight.career.mechanism.limit.LimitType

object DemolitionistPassive {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun igniteCreeper(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity !is Creeper)
            return
        if (player.inventory.itemInMainHand.type != Material.FLINT_AND_STEEL)
            return
        if (!player.hasBranch("爆破师")) {
            event.isCancelled = true

            player.sendMessage("无法点燃苦力怕，需要解锁: ${displayLimit("爆破师" to 0)}")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damagedByExplosion(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity !is Player)
            return

        if (event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION &&
            event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
        )
            return

        if (entity.hasBranch("爆破师")) {
            event.damage = event.damage * 0.8
        }
    }
}

enum class DemolitionistSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    ENDER_CRYSTAL(
        listOf(
            Material.END_CRYSTAL
        ), listOf(
            LimitType.USE to ("爆破师" to 0)
        )
    ),
    TNT(
        listOf(
            Material.TNT
        ), listOf(LimitType.IGNITE to ("爆破师" to 0))
    ),
    FIREBALL(
        listOf(
            Material.FIRE_CHARGE
        ),
        listOf(
            LimitType.CRAFT to ("爆破师" to 0),
            LimitType.USE to ("爆破师" to 0)
        )
    )
}