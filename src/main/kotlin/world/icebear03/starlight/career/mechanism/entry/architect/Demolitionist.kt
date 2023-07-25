package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.limit.LimitType

object DemolitionistPassive {
    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun igniteCreeper(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity !is Creeper)
            return
        val type = player.inventory.itemInMainHand.type
        if (type != Material.FLINT_AND_STEEL &&
            type != Material.FIRE_CHARGE
        )
            return
        if (!player.hasBranch("爆破师")) {
            event.isCancelled = true

            player.sendMessage("无法点燃苦力怕，需要解锁: ${"爆破师".display()}")
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

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun craftItem(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val type = event.recipe.result.type

        if (type == Material.TNT || type == Material.TNT_MINECART) {
            val level = player.getSkillLevel("稳定三硝基甲苯")

            val failPercent = when (level) {
                0 -> 0.5
                1 -> 0.25
                2 -> 0.1
                3 -> 1.0
                else -> 0.0
            }

            if (Math.random() <= failPercent) {
                event.isCancelled = true
                event.inventory.clear()
                player.closeInventory()

                val trace = player.rayTraceBlocks(5.0) ?: return
                val block = trace.hitBlock ?: return
                val loc = block.location
                val world = block.world
                block.type = Material.AIR
                world.createExplosion(loc, 3.0F)

                player.sendMessage("合成炸药时不小心爆炸了，升级技能 ${"稳定三硝基甲苯".display()}\" &r以规避")
            }
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
            Material.TNT,
            Material.TNT_MINECART
        ), listOf(
            LimitType.CRAFT to ("爆破师" to 0),
            LimitType.PLACE to ("爆破师" to 0),
            LimitType.USE to ("爆破师" to 0)
        )
    ),
    FIRE(
        listOf(
            Material.FLINT_AND_STEEL,
            Material.FIRE_CHARGE
        ),
        listOf(
            LimitType.CRAFT to ("爆破师" to 0),
            LimitType.USE to ("爆破师" to 0)
        )
    )
}