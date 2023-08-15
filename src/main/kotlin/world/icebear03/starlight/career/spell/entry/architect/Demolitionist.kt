package world.icebear03.starlight.career.spell.entry.architect

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Arrow
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.entity.TippedArrow
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.addSpecialRecipe
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.recipe.shapedRecipe
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.shootPrimedTNT
import java.util.*

object Demolitionist {
    fun initialize() {
        val basic = "爆破师" to 0
        addLimit(HandlerType.USE, basic, Material.END_CRYSTAL)
        addLimit(HandlerType.PLACE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(HandlerType.USE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(HandlerType.CRAFT, basic, Material.FIRE_CHARGE)
        addLimit(HandlerType.USE, basic, Material.FIRE_CHARGE)

        "前沿爆破线".discharge { name, level ->
            finish(name)
            val amount = 2 + level
            val eye = this.eyeLocation.direction.multiply(3)
            for (i in 1..amount) {
                val velocity = eye.clone().add(
                    Vector(
                        Math.random() * 0.5,
                        Math.random() * 0.5, Math.random() * 0.5
                    )
                )
                this.location.shootPrimedTNT(velocity)
            }
            "§a技能 ${display(name)} §7释放成功，发射出§e${amount}个§7点燃的TNT"
        }
        "手摇TNT火炮".discharge { name, _ ->
            "§d顿悟 ${display(name)} §7释放成功，接下来§e30秒§7内箭矢变为TNT"
        }
        "气浪行者".discharge { name, level ->
            val duration = 2 + level
            val percent = 35 + 15 * level
            "§a技能 ${display(name)} §7释放成功，接下来§e${duration}秒§7内受到爆炸伤害减少§e${percent}%"
        }

        shapedRecipe(
            NamespacedKey.minecraft("career_crystal_special"),
            ItemStack(Material.END_CRYSTAL),
            listOf("aba", "aba", "aba"),
            'a' to Material.GLASS,
            'b' to Material.TNT
        ).addSpecialRecipe("末影硝酸甘油")

        listOf(Material.TNT, Material.TNT_MINECART).addHighListener(HandlerType.CRAFT) { _, player, type ->
            if (player.meetRequirement("精炼炸药") && Math.random() <= 0.2) {
                player.giveItem(ItemStack(type))
                "§d顿悟 ${display("精炼炸药")} §7使得本次合成炸药时获得额外产物"
            } else null
        }

        listOf(Material.TNT, Material.TNT_MINECART).addLowestListener(HandlerType.CRAFT) skill@{ _, player, _ ->
            val failPercent = when (player.spellLevel("稳定三硝基甲苯")) {
                1 -> 0.25
                2 -> 0.1
                3 -> 0.0
                else -> 0.5
            }

            if (Math.random() <= failPercent) {
                if (Math.random() <= failPercent) {
                    val trace = player.rayTraceBlocks(5.0)
                    val block = trace?.hitBlock ?: player.location.block
                    val loc = block.location
                    val world = block.world
                    block.type = Material.AIR
                    world.createExplosion(loc, 3.0F)

                    false to "§a生涯系统 §7>> 炸药合成时爆炸了，升级技能 ${display("稳定三硝基甲苯")} §7以规避"
                } else {
                    false to "§a生涯系统 §7>> 炸药合成失败，升级技能 ${display("稳定三硝基甲苯")} §7以提升成功率"
                }
            } else {
                true to null
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun igniteBlock(event: BlockIgniteEvent) {
        val player = event.player ?: return
        if (!player.meetRequirement("爆破师", 0)) {
            if (Math.random() >= 0.2) {
                event.isCancelled = true
                player.setCooldown(Material.FLINT_AND_STEEL, 5)
                player.setCooldown(Material.FIRE_CHARGE, 5)
                submit {
                    player.sendMessage("§a生涯系统 §7>> 使用打火石失败，请解锁§e职业分支 ${display("爆破师")} §7以提高成功率")
                }
            }
        }
    }

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
        if (!player.meetRequirement("爆破师", 0)) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法点燃苦力怕，需要解锁 ${display("爆破师")}")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun arrowSpawn(event: EntitySpawnEvent) {
        val arrow = event.entity
        if (arrow !is Arrow)
            return
        val shooter = arrow.shooter
        if (shooter !is Player)
            return

        if (arrow is TippedArrow)
            return

        if (arrow.isShotFromCrossbow)
            return

        if (shooter.isDischarging("手摇TNT火炮"))
            submit {
                arrow.location.shootPrimedTNT(arrow.velocity)
                arrow.remove()
            }
    }

    val tnts = mutableListOf<UUID>()

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun tntExplode(event: EntityExplodeEvent) {
        val uuid = event.entity.uniqueId
        if (tnts.contains(uuid)) {
            event.blockList().clear()
            tnts.remove(uuid)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damaged(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is Player)
            return

        if (!event.cause.toString().contains("EXPLOSION"))
            return

        if (entity.meetRequirement("爆破师", 0)) {
            event.damage *= 0.8
        }

        if (entity.isDischarging("气浪行者")) {
            val percent = 35 + 15 * entity.spellLevel("气浪行者")
            event.damage *= (1 - percent / 100.0)
        }
    }
}