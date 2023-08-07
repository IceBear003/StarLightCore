package world.icebear03.starlight.career.spell.entry

import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.attacker
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.utils.realDamage

object WeaponDamage {

    val giants = listOf(
        EntityType.IRON_GOLEM,
        EntityType.ELDER_GUARDIAN,
        EntityType.ENDER_DRAGON,
        EntityType.WITHER,
        EntityType.RAVAGER,
        EntityType.WARDEN
    )

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun attack(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val damaged = event.entity
        val player = event.attacker
        if (player !is Player || damaged !is LivingEntity) {
            return
        }
        val typeString = player.inventory.itemInMainHand.type.toString()
        val usingAxe = typeString.contains("_AXE")
        val usingPickAxe = typeString.contains("_PICKAXE")
        val usingSpade = typeString.contains("_SHOVEL")
        val usingSword = typeString.contains("_SWORD")
        val usingBow = typeString == "BOW"

        if (usingAxe)
            if (!player.meetRequirement("屠夫", 0) &&
                !player.meetRequirement("伐木工", 0) &&
                !player.meetRequirement("工具制造商", 0) &&
                !player.meetRequirement("探险家", 0)
            )
                event.damage *= 0.5

        if (usingSword)
            if (!player.meetRequirement("探险家", 0))
                event.damage *= 0.5

        if (usingBow)
            if (!player.meetRequirement("探险家", 0))
                event.damage *= 0.5

        if (usingPickAxe || usingSpade)
            if (!player.meetRequirement("矿工", 0) &&
                !player.meetRequirement("工具制造商", 0)
            )
                event.damage *= 0.5

        if (giants.contains(damaged.type)) {
            if (player.meetRequirement("探险家", 0)) {
                event.damage *= 1.5
            }
        }

        if (damager is Arrow && player.meetRequirement("旷野互助")) {
            if (damager.hasCustomEffect(PotionEffectType.REGENERATION) ||
                damager.hasCustomEffect(PotionEffectType.HEAL)
            ) {
                event.isCancelled = true
                damaged.realDamage(1.0)
                damaged.health = minOf(damaged.health + 6.0, damaged.maxHealth)
            }
        }
    }
}