package world.icebear03.starlight.career.spell.entry.scholar

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapedRecipe
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.getEnchants
import world.icebear03.starlight.utils.isDischarging
import kotlin.math.roundToInt

object Enchanter {

    fun initialize() {
        addLimit(HandlerType.PLACE, "附魔师" to 0, Material.ENCHANTING_TABLE)
        addLimit(HandlerType.USE, "附魔师" to 0, Material.GRINDSTONE)

        "魔力虹吸".discharge { name, _ ->
            "${display(name)} §7释放成功，下次砂轮卸魔时会获得额外增益"
        }

        shapedRecipe(
            NamespacedKey.minecraft("career_experience_bottle"),
            ItemStack(Material.EXPERIENCE_BOTTLE),
            listOf("aaa", "aba", "aaa"),
            'a' to Material.LAPIS_LAZULI,
            'b' to Material.GLASS_BOTTLE
        ).addSpecialRecipe("青金术师")

        "附魔领域展开".discharge { name, _ ->
            openEnchanting(null, true)
            finish(name)
            null
        }

        "禁术褫夺".discharge { name, _ ->
            val trace =
                world.rayTraceEntities(location.add(0.0, 1.5, 0.0), eyeLocation.direction.normalize(), 6.0) { it.uniqueId != uniqueId }
            val entity = trace?.hitEntity
            if (entity !is Player)
                return@discharge "${display(name)} §7释放成功，但是没有选中一个玩家"
            entity.sendMessage("§a生涯系统 §7>> 你被 §e$name §7使用 ${display(name)} §7选中，请注意不要在§c无抗火条件§7下被点燃")

            entity.effect(PotionEffectType.WEAKNESS, 20, 1)
            var tot = 0
            submit(period = 2L) {
                tot++
                if (tot >= 200) {
                    cancel()
                    finish(name)
                    return@submit
                }
                if (!isDischarging(name) || !entity.isOnline || entity.isDead) {
                    cancel()
                    return@submit
                }

                if (!entity.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) && entity.fireTicks > 0) {
                    val item = entity.inventory.armorContents.filter { it.itemMeta is Damageable }.random()
                    item.enchantments.toMap().forEach { item.removeEnchantment(it.key) }
                    entity.sendMessage("§a生涯系统 §7>> 你被 §e${this@discharge.name} §7使用 ${display(name)} §7卸除了一件装备的附魔")
                    sendMessage("§a生涯系统 §7>> ${display(name)} §7标记中的玩家被点燃，触发附魔卸除")
                    finish(name)
                }
            }

            "${display(name)} §7释放成功，期间内将标记玩家在§c无抗火条件§7下点燃即可卸除其一件盔甲上的§b所有附魔"
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun combine(event: PrepareAnvilEvent) {
        val inv = event.inventory
        val second = inv.getItem(1) ?: return

        val player = event.viewers[0] as Player

        val enchants = second.getEnchants()
        if (enchants.isNotEmpty()) {
            if (!player.meetRequirement("附魔师", 0)) {
                event.result = null
                player.sendMessage("§a生涯系统 §7>> 需要 ${display("附魔师")} §7才能在铁砧中合并附魔物品")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun enchantLowest(event: EnchantItemEvent) {
        val minLevel = event.expLevelCost
        val player = event.enchanter
        if (minLevel >= 16 && !player.meetRequirement("附魔师", 0)) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 需要 ${display("附魔师")} §7才能进行§a>15级§7附魔")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun enchantHigh(event: EnchantItemEvent) {
        val player = event.enchanter

        val cost = event.whichButton() + 1
        var spellLevel = player.spellLevel("注魔宝典")
        if (spellLevel > 0 && Math.random() <= spellLevel * 0.2) {
            player.giveExpLevels(cost)
            player.sendMessage("§a生涯系统 §7>> ${display("注魔宝典", spellLevel)} §7使得本次附魔不消耗经验等级")
        }
        spellLevel = player.spellLevel("咒术工程")
        if (spellLevel > 0 && Math.random() <= spellLevel * 0.1 + 0.1) {
            player.giveItem(ItemStack(Material.LAPIS_LAZULI, minOf(spellLevel, cost)))
            player.sendMessage("§a生涯系统 §7>> ${display("咒术工程", spellLevel)} §7使得从本次附魔中额外获得了青金石")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun grindstone(event: InventoryClickEvent) {
        val inv = event.clickedInventory ?: return
        if (inv !is GrindstoneInventory)
            return
        val player = event.whoClicked as Player
        if (event.slot == 2) {
            val item = event.currentItem ?: return
            val first = inv.getItem(0)
            val second = inv.getItem(1)
            val hasEnchant = first.getEnchants().isNotEmpty() || second.getEnchants().isNotEmpty()
            if (hasEnchant && player.isDischarging("魔力虹吸")) {
                val spellLevel = player.spellLevel("魔力虹吸")
                player.giveExp(5 * spellLevel)
                if (Math.random() <= 0.1 * spellLevel) {
                    val durability = item.type.maxDurability
                    item.modifyMeta<ItemMeta> {
                        if (this is Damageable) {
                            damage = maxOf(0, (damage + 0.2 * durability).roundToInt())
                        }
                    }
                    player.sendMessage("§a生涯系统 §7>> ${display("魔力虹吸", spellLevel)} §7使得本次卸魔获得额外经验和耐久")
                } else {
                    player.sendMessage("§a生涯系统 §7>> ${display("魔力虹吸", spellLevel)} §7使得本次卸魔获得额外经验")
                }
                player.finish("魔力虹吸")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun levelUp(event: PlayerLevelChangeEvent) {
        if (event.newLevel > event.oldLevel) {
            event.player.giveExp(10 * (event.newLevel - event.oldLevel))
        }
    }
}