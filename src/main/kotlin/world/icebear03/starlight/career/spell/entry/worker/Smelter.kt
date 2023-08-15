package world.icebear03.starlight.career.spell.entry.worker

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Furnace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapedRecipe
import world.icebear03.starlight.utils.getBlockAside
import world.icebear03.starlight.utils.hasBlockAside
import world.icebear03.starlight.utils.isDischarging

object Smelter {

    fun initialize() {
        addLimit(HandlerType.USE, "烧炼师" to 0, Material.BLAST_FURNACE)
        listOf(
            Material.HOPPER,
            Material.DISPENSER,
            Material.DROPPER
        ).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("烧炼师", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(
                    listOf(Material.FURNACE, Material.BLAST_FURNACE), 6
                )
            )
                return@addLowestListener false to "你不能在炉边放置物品传输方块，需要解锁 §e职业分支 ${display("烧炼师")}"
            return@addLowestListener true to null
        }
        listOf(Material.FURNACE, Material.BLAST_FURNACE).addLowestListener(HandlerType.PLACE) { event, player, _ ->
            val placeEvent = event as BlockPlaceEvent
            if (player.meetRequirement("烧炼师", 0))
                return@addLowestListener true to null
            if (placeEvent.block.location.hasBlockAside(
                    listOf(
                        Material.HOPPER,
                        Material.DISPENSER,
                        Material.DROPPER
                    ), 2
                )
            )
                return@addLowestListener false to "你不能在物品传输方块边放置这个炉子，需要解锁 §e职业分支 ${display("烧炼师")}"
            return@addLowestListener true to null
        }

        "熔金模具".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下一次获取烧炼产物时会获得额外经验"
        }

        "燃料管道".discharge { name, level ->
            location.getBlockAside(2 + 2 * level, Material.FURNACE, Material.BLAST_FURNACE).forEach { loc ->
                val block = loc.block
                val furnace = block.state as Furnace
                furnace.burnTime = maxOf(((25 + 5 * level) * 20).toShort(), furnace.burnTime)
                furnace.update()
            }
            "§a技能 ${display(name)} §7释放成功，周围熔炉将会进入持续燃烧状态"
        }

        shapedRecipe(
            NamespacedKey.minecraft("career_lava_bucket"),
            ItemStack(Material.LAVA_BUCKET),
            listOf("aaa", "aba", "aaa"),
            'a' to Material.COAL,
            'b' to Material.BUCKET
        ).addSpecialRecipe("煤液化")

        shapedRecipe(
            NamespacedKey.minecraft("career_netherite_ingot"),
            ItemStack(Material.NETHERITE_INGOT, 4),
            listOf("aba", "cbc", "ddd"),
            'a' to Material.IRON_BLOCK,
            'b' to Material.GOLD_BLOCK,
            'c' to Material.DIAMOND,
            'd' to Material.NETHERITE_INGOT
        ).addSpecialRecipe("制锭机床")
    }

    val specialFuels = mutableListOf(Material.COAL, Material.COAL_BLOCK, Material.LAVA_BUCKET, Material.BLAZE_ROD)

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun sinter(event: InventoryClickEvent) {
        event.clickedInventory ?: return

        val inv = event.inventory
        if (inv.type != InventoryType.BLAST_FURNACE && inv.type != InventoryType.FURNACE)
            return

        val slot = event.slotType
        val item = event.currentItem ?: return
        val type = item.type
        val player = event.whoClicked as Player

        if (event.clickedInventory!!.type == InventoryType.PLAYER) {
            if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (specialFuels.contains(type) && !player.meetRequirement("烧炼师", 0)) {
                    player.sendMessage("§a生涯系统 §7>> 无法使用进阶燃料，需要解锁 ${display("烧炼师")}")
                    event.isCancelled = true
                    return
                }
            }
        }

        if (slot == SlotType.FUEL) {
            val cursor = event.cursor ?: return
            val cursorType = cursor.type
            if (specialFuels.contains(cursorType) && !player.meetRequirement("烧炼师", 0)) {
                player.sendMessage("§a生涯系统 §7>> 无法使用进阶燃料，需要解锁 ${display("烧炼师")}")
                event.isCancelled = true
                return
            }
            if (event.action == InventoryAction.HOTBAR_MOVE_AND_READD) {
                val hotbar = event.hotbarButton
                player.inventory.getItem(hotbar)?.type?.let {
                    if (specialFuels.contains(type) && !player.meetRequirement("烧炼师", 0)) {
                        player.sendMessage("§a生涯系统 §7>> 无法使用进阶燃料，需要解锁 ${display("烧炼师")}")
                        event.isCancelled = true
                        return
                    }
                }
            }
        }

        if (slot == SlotType.RESULT) {
            val level = player.spellLevel("熔炉改良")
            val originAmount = item.amount
            if (level >= 0) {
                var amount = 0
                val rate = 0.05 * level

                for (i in 1..originAmount)
                    if (Math.random() <= rate)
                        amount += 1

                if (amount != 0) {
                    player.giveItem(ItemStack(type, amount))
                    player.sendMessage("§a生涯系统 §7>> §a技能 ${display("熔炉改良")} §7使得烧炼获得额外§a${amount}个§7产物")
                }
            }

            if (player.isDischarging("熔金模具")) {
                player.finish("熔金模具")
                player.giveExp(originAmount * (1 + player.spellLevel("熔金模具")))
            }

            if (player.meetRequirement("回炉重铸") && type == Material.GOLD_NUGGET) {
                player.sendMessage("§a生涯系统 §7>> §d顿悟 ${display("回炉重铸")} §7使得烧炼获得额外矿物粒")
                player.giveItem(ItemStack(Material.GOLD_NUGGET, 8))
            }
            if (player.meetRequirement("回炉重铸") && type == Material.IRON_NUGGET) {
                player.sendMessage("§a生涯系统 §7>> §d顿悟 ${display("回炉重铸")} §7使得烧炼获得额外矿物粒")
                player.giveItem(ItemStack(Material.IRON_NUGGET, 8))
            }
        }
    }
}