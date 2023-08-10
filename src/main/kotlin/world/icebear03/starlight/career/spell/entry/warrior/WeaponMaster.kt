package world.icebear03.starlight.career.spell.entry.warrior

import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.SmithItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.getEnchants

object WeaponMaster {

    val diamondItems = listOf(
        Material.DIAMOND_HELMET,
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_BOOTS,
        Material.DIAMOND_SWORD,
        Material.DIAMOND_AXE
    )
    val netheriteItems = listOf(
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS,
        Material.NETHERITE_SWORD,
        Material.NETHERITE_AXE
    )

    fun initialize() {
        addLimit(HandlerType.USE, "武器专家" to 0, Material.GRINDSTONE)
        "奇兵突袭".discharge { name, level ->
            val duration = 15 + 15 * level
            effect(PotionEffectType.INVISIBILITY, duration, 1)
            effect(PotionEffectType.INCREASE_DAMAGE, duration, 1 + level)
            "§a技能 ${display(name)} §7释放成功，获得一段时间的效果增益，若发动攻击则会提前结束"
        }.finish { _, _ ->
            removePotionEffect(PotionEffectType.INVISIBILITY)
            removePotionEffect(PotionEffectType.INCREASE_DAMAGE)
            null
        }

        addLimit(
            HandlerType.CRAFT,
            "武器专家" to 0,
            *(diamondItems + Material.TURTLE_HELMET).toTypedArray()
        )
    }

    @SubscribeEvent
    fun shoot(event: EntityShootBowEvent) {
        val player = event.entity
        if (player !is Player)
            return
        val crossbow = event.bow ?: return
        if (crossbow.type != Material.CROSSBOW)
            return

        if (event.projectile !is Arrow)
            return

        if (player.meetRequirement("武器专家", 0)) {
            val arrow = event.projectile as Arrow
            if (arrow is SpectralArrow || arrow is TippedArrow)
                return

            if (Math.random() <= 0.25) {
                arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                player.giveItem(ItemStack(Material.ARROW))
                player.sendMessage("§a生涯系统 §7>> §e分支 ${display("武器专家")} §7使得本次使用弩射箭时不消耗箭")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun smith(event: SmithItemEvent) {
        val inv = event.inventory
        val item = inv.getItem(2) ?: return
        val type = item.type
        val player = event.whoClicked as Player
        if (diamondItems.contains(type) && !player.meetRequirement("武器专家", 0)) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法锻造钻石武器/防具，需要解锁 ${display("武器专家")}")
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun combine(event: PrepareAnvilEvent) {
        val inv = event.inventory
        val second = inv.getItem(1)

        val player = event.viewers[0] as Player

        val enchants = second.getEnchants()
        if (enchants.isEmpty() && second != null) {
            if (!player.meetRequirement("武器专家", 0)) {
                event.result = null
                player.sendMessage("§a生涯系统 §7>> 需要 ${display("武器专家")} §7才能在铁砧中修复物品")
            }
        }

        if (second == null) {
            if (!player.meetRequirement("武器专家", 0)) {
                event.result = null
                player.sendMessage("§a生涯系统 §7>> 需要 ${display("武器专家")} §7才能在铁砧中重命名物品")
            }
        }
    }
}