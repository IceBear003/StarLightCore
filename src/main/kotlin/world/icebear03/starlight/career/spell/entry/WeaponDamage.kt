package world.icebear03.starlight.career.spell.entry

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.attacker
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.finish
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spellLevel
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.getEnchants
import world.icebear03.starlight.utils.isDischarging
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
    fun damaged(event: EntityDamageByEntityEvent) {
        val player = event.entity
        if (player !is Player) {
            return
        }
        val hand = player.inventory.itemInMainHand
        val type = hand.type
        val typeString = type.toString()

        val level = player.spellLevel("以锋为御")
        if (level > 0 && (typeString.contains("_SWORD") ||
                    typeString.contains("_AXE") ||
                    type == Material.TRIDENT)
        ) {
            val mag = when (level) {
                1 -> 0.92
                2 -> 0.84
                3 -> 0.75
                else -> 1.0
            }
            event.damage *= mag
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun attack(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val damaged = event.entity
        val player = event.attacker
        if (player !is Player || damaged !is LivingEntity) {
            return
        }
        val weapon = player.inventory.itemInMainHand
        val type = weapon.type
        val typeString = type.toString()
        val usingAxe = typeString.contains("_AXE")
        val usingPickAxe = typeString.contains("_PICKAXE")
        val usingSpade = typeString.contains("_SHOVEL")
        val usingSword = typeString.contains("_SWORD")
        val usingBow = type == Material.BOW
        val usingCrossBow = type == Material.CROSSBOW
        val usingTrident = type == Material.TRIDENT

        val isRemoteAttack = damager is AbstractArrow
        val isFull = player.attackCooldown >= 0.9

        if (usingAxe)
            if (!player.meetRequirement("屠夫", 0) &&
                !player.meetRequirement("伐木工", 0) &&
                !player.meetRequirement("工具制造商", 0) &&
                !player.meetRequirement("探险家", 0) &&
                !player.meetRequirement("武器专家", 0)
            )
                event.damage *= 0.5

        if (usingSword)
            if (!player.meetRequirement("探险家", 0) &&
                !player.meetRequirement("武器专家", 0)
            )
                event.damage *= 0.5

        if (usingBow)
            if (!player.meetRequirement("探险家", 0) &&
                !player.meetRequirement("武器专家", 0)
            )
                event.damage *= 0.5

        if (usingCrossBow)
            if (!player.meetRequirement("武器专家", 0))
                event.damage *= 0.5

        if (usingTrident)
            if (!player.meetRequirement("武器专家", 0)) event.damage *= 0.5
            else event.damage += 2.0


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

        if (player.isDischarging("奇兵突袭")) {
            player.finish("奇兵突袭")
        }

        if (usingCrossBow && player.meetRequirement("穿甲箭头")) {
            val level = weapon.getEnchants()[Enchantment.PIERCING] ?: 0
            damaged.realDamage(1.0 + level)
        }

        if (usingTrident && player.meetRequirement("涌潮长戟")) {
            if (isRemoteAttack) {
                player.sendMessage("§a生涯系统 §7>> §d顿悟 ${display("涌潮长戟")} §7使得敌方获得挖掘疲劳效果")
                damaged.sendMessage("§a生涯系统 §7>> 由于攻击者激活了§d顿悟 ${display("涌潮长戟")} §7你获得短暂的挖掘疲劳")
                damaged.effect(PotionEffectType.SLOW_DIGGING, 10, 2)
            } else if (isFull)
                player.health = minOf(player.health + 2.0, player.maxHealth)
        }

        if (damager is SpectralArrow) {
            if (player.isDischarging("信号弹")) {
                player.finish("信号弹")
                val duration = when (player.spellLevel("信号弹")) {
                    1 -> 15
                    2 -> 20
                    3 -> 25
                    else -> 10
                }
                player.giveItem(ItemStack(Material.SPECTRAL_ARROW))
                player.sendMessage("§a生涯系统 §7>> §a技能 ${display("信号弹")} §7使得本次不消耗光灵箭，且敌方受到发光作用增长")
                submit(delay = 1) {
                    damaged.removePotionEffect(PotionEffectType.GLOWING)
                    damaged.effect(PotionEffectType.GLOWING, duration, 1)
                }
            }
        }
    }
}