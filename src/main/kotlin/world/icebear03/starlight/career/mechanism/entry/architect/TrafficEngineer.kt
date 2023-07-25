package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.hasEureka
import world.icebear03.starlight.career.mechanism.discharge.defineDischarge
import world.icebear03.starlight.career.mechanism.discharge.defineFinish
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.limit.LimitType
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.hasBlockAside

object TrafficEngineerActive {

    val seaBiomes = Biome.values().filter {
        it.toString().endsWith("OCEAN")
    }

    fun initialize() {
        "追加动力".defineDischarge { id, level ->
            val percent = 20 + 10 * level

            if (this.isInsideVehicle) {
                val vehicle = this.vehicle!!
                if (vehicle is Boat)
                    vehicle.maxSpeed = vehicle.maxSpeed * (percent / 100.0)
                if (vehicle is Minecart)
                    vehicle.maxSpeed = vehicle.maxSpeed * (percent / 100.0)
            }

            "§a技能 ${id.display()} §7释放成功，载具在§a5秒§7内速度加快§e${percent}%"
        }
        "追加动力".defineFinish { _, level ->
            val percent = 20 + 10 * level

            if (this.isInsideVehicle) {
                val vehicle = this.vehicle!!
                if (vehicle is Boat)
                    vehicle.maxSpeed = vehicle.maxSpeed / (percent / 100.0)
                if (vehicle is Minecart)
                    vehicle.maxSpeed = vehicle.maxSpeed / (percent / 100.0)
            }
        }
        "备用载具".defineDischarge skill@{ id, _ ->
            if (this.isInWater) {
                this.world.spawnEntity(this.location, EntityType.BOAT)
                return@skill "§a技能 ${id.display()} §7释放成功，船只已经召唤"
            }
            if (this.hasBlockAside(TrafficEngineerSet.RAILS.types, 1)) {
                this.world.spawnEntity(this.location, EntityType.MINECART)
                return@skill "§a技能 ${id.display()} §7释放成功，矿车已经召唤"
            }
            "§a技能 ${id.display()} §7释放成功，但是没有载具匹配当前位置"
        }
        "游弋信风".defineDischarge { id, _ ->
            if (seaBiomes.contains(this.location.block.biome)) {
                this.effect(PotionEffectType.DOLPHINS_GRACE, 45, 3)
            } else {
                this.effect(PotionEffectType.DOLPHINS_GRACE, 30, 2)
            }
            "§d顿悟 ${id.display()} §7释放成功，获得效果 §b海豚的恩惠"
        }
    }
}

object TrafficEngineerPassive {

    val specialRecipes = mutableListOf<NamespacedKey>()

    fun initialize() {
        val keyA = NamespacedKey.minecraft("rail_special_a")
        val recipeA = ShapedRecipe(keyA, ItemStack(Material.RAIL, 16))
        recipeA.shape("a a", " s ", "a a")
        recipeA.setIngredient('a', Material.IRON_INGOT)
        recipeA.setIngredient('s', Material.STICK)
        Bukkit.removeRecipe(keyA)
        Bukkit.addRecipe(recipeA)

        val keyB = NamespacedKey.minecraft("rail_special_b")
        val recipeB = ShapedRecipe(keyB, ItemStack(Material.RAIL, 16))
        recipeB.shape("a a", "asa", "a a")
        recipeB.setIngredient('a', Material.COPPER_INGOT)
        recipeB.setIngredient('s', Material.STICK)
        Bukkit.removeRecipe(keyB)
        Bukkit.addRecipe(recipeB)

        specialRecipes += keyA
        specialRecipes += keyB
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val recipe = event.recipe
        if (recipe !is ShapedRecipe)
            return

        val key = recipe.key
        if (specialRecipes.contains(key)) {
            if (!player.hasEureka("精炼钢轨")) {
                event.isCancelled = true
                player.closeInventory()
                player.sendMessage("§a生涯系统 §7>> 必须激活§d顿悟 " + "精炼钢轨".display() + " §7才可以使用此特殊合成")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: VehicleEnterEvent) {
        val entity = event.entered

        if (entity !is Player)
            return

        if (!entity.hasBranch("交通工程师"))
            return

        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed = vehicle.maxSpeed * 1.2
        if (vehicle is Minecart)
            vehicle.maxSpeed = vehicle.maxSpeed * 1.2
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: VehicleExitEvent) {
        val vehicle = event.vehicle

        if (vehicle is Boat)
            vehicle.maxSpeed = 0.4
        if (vehicle is Minecart)
            vehicle.maxSpeed = 0.4
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun event(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val type = item.type
        val player = event.player

        val level = player.getSkillLevel("路线规划")

        if (TrafficEngineerSet.RAILS.types.contains(type)) {
            val percent = when (level) {
                0, -1 -> 0.0
                1 -> 0.08
                else -> 0.15
            }
            if (Math.random() <= percent) {
                player.giveItem(ItemStack(type))
                player.sendMessage("§a生涯系统 §7>> 本次放置未消耗物品")
            }
        }

        if (TrafficEngineerSet.ICE_BLOCKS.types.contains(type)) {
            if ((level >= 3 && Math.random() <= 0.1) ||
                (player.world.environment == World.Environment.NETHER && player.hasEureka("下界开路者") && Math.random() <= 0.15)
            ) {
                player.giveItem(ItemStack(type))
                player.sendMessage("§a生涯系统 §7>> 本次放置未消耗物品")
            }
        }
    }
}

enum class TrafficEngineerSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    RAILS(
        listOf(
            Material.RAIL,
            Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL,
            Material.POWERED_RAIL
        ), listOf(
            LimitType.CRAFT to ("交通工程师" to 0),
            LimitType.PLACE to ("交通工程师" to 0),
            LimitType.DROP_IF_BREAK to ("交通工程师" to 0)
        )
    ),
    MINECARTS(
        listOf(
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER_MINECART,
            Material.TNT_MINECART
        ), listOf(
            LimitType.CRAFT to ("交通工程师" to 0)
        )
    ),
    ICE_BLOCKS(
        listOf(
            Material.PACKED_ICE,
            Material.BLUE_ICE
        ),
        listOf(LimitType.PLACE to ("交通工程师" to 0))
    )
}