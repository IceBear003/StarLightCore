package world.icebear03.starlight.career.spell.entry.architect

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapedRecipe
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside

object TrafficEngineer {

    val rails = listOf(
        Material.RAIL,
        Material.ACTIVATOR_RAIL,
        Material.DETECTOR_RAIL,
        Material.POWERED_RAIL
    )
    val minecarts = listOf(
        Material.MINECART,
        Material.CHEST_MINECART,
        Material.FURNACE_MINECART,
        Material.HOPPER_MINECART,
        Material.TNT_MINECART
    )
    val iceBlocks = listOf(
        Material.PACKED_ICE,
        Material.BLUE_ICE
    )
    val seaBiomes = Biome.values().filter {
        it.toString().endsWith("OCEAN")
    }

    fun initialize() {
        addLimit(HandlerType.CRAFT, "交通工程师" to 0, *rails.toTypedArray())
        addLimit(
            HandlerType.CRAFT,
            "交通工程师" to 0,
            *Material.entries.filter { it.toString().endsWith("_BOAT") }.toTypedArray()
        )
        addLimit(
            HandlerType.CRAFT,
            "交通工程师" to 0,
            *Material.entries.filter { it.toString().endsWith("_MINECART") }.toTypedArray()
        )
        addLimit(HandlerType.PLACE, "交通工程师" to 0, *rails.toTypedArray())
        addLimit(HandlerType.DROP_IF_BREAK, "交通工程师" to 0, *rails.toTypedArray())
        addLimit(HandlerType.CRAFT, "交通工程师" to 0, *minecarts.toTypedArray())
        addLimit(HandlerType.PLACE, "交通工程师" to 0, *iceBlocks.toTypedArray())

        shapedRecipe(
            NamespacedKey.minecraft("career_rail_iron"),
            ItemStack(Material.RAIL, 16),
            listOf("a a", " b ", "a a"),
            'a' to Material.IRON_INGOT,
            'b' to Material.STICK
        ).addSpecialRecipe("精炼铁轨")
        shapedRecipe(
            NamespacedKey.minecraft("career_rail_copper"),
            ItemStack(Material.RAIL, 16),
            listOf("a a", "aba", "a a"),
            'a' to Material.COPPER_INGOT,
            'b' to Material.STICK
        ).addSpecialRecipe("精炼铁轨")

        rails.addHighListener(HandlerType.PLACE) { _, player, type ->
            val percent = when (player.spellLevel("路线规划")) {
                0, -1 -> 0.0
                1 -> 0.08
                else -> 0.15
            }
            if (Math.random() <= percent) {
                player.giveItem(ItemStack(type))
                "§a技能 ${display("路线规划")} §7使得本次放置不消耗铁轨"
            } else null
        }
        iceBlocks.addHighListener(HandlerType.PLACE) { _, player, type ->
            val level = player.spellLevel("路线规划")
            if ((level >= 3 && Math.random() <= 0.1) ||
                (player.world.environment == World.Environment.NETHER && player.meetRequirement("下界开路者") && Math.random() <= 0.15)
            ) {
                player.giveItem(ItemStack(type))
                "本次放置未消耗物品"
            } else null
        }

        "追加动力".discharge { name, level ->
            val percent = 20 + 10 * level
            if (this.isInsideVehicle) {
                val vehicle = this.vehicle!!
                val now = vehicle.velocity
                vehicle.velocity = now.multiply(1 + (percent * 7) / 100.0)
            }
            "§a技能 ${display(name)} §7释放成功，载具在§a5秒§7内速度加快§e${percent}%"
        }
        "备用载具".discharge skill@{ name, level ->
            finish(name)
            if (this.hasBlockAside(Material.WATER, 2) ||
                this.hasBlockAside(iceBlocks + Material.ICE + Material.FROSTED_ICE, 2)
            ) {
                val boat = this.world.spawnEntity(this.location.add(0.0, 1.0, 0.0), EntityType.BOAT) as Boat
                boat.boatType = Boat.Type.values().random()
                return@skill "§a技能 ${display(name)} §7释放成功，船只已经召唤"
            }
            if (level >= 2 && this.hasBlockAside(rails, 2)) {
                this.world.spawnEntity(this.location, EntityType.MINECART)
                return@skill "§a技能 ${display(name)} §7释放成功，矿车已经召唤"
            }
            "§a技能 ${display(name)} §7释放成功，但是没有载具匹配当前位置"
        }
        "游弋信风".discharge { name, _ ->
            finish(name)
            if (seaBiomes.contains(this.location.block.biome)) {
                this.effect(PotionEffectType.DOLPHINS_GRACE, 45, 3)
            } else {
                this.effect(PotionEffectType.DOLPHINS_GRACE, 30, 2)
            }
            "§d顿悟 ${display(name)} §7释放成功，获得效果 §b海豚的恩惠"
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: VehicleEnterEvent) {
        val entity = event.entered

        if (entity !is Player)
            return

        try {
            if (!entity.meetRequirement("交通工程师", 0))
                return
        } catch (unload: Exception) {
            return
        }

        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed *= 1.2
        if (vehicle is Minecart)
            vehicle.maxSpeed *= 1.2
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun exit(event: VehicleExitEvent) {
        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed = 0.4
        if (vehicle is Minecart)
            vehicle.maxSpeed = 0.4
    }
}