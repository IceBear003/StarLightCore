package world.icebear03.starlight.career.spell.entry.farmer

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.Animals
import org.bukkit.entity.Cow
import org.bukkit.entity.Goat
import org.bukkit.entity.Sheep
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.SheepDyeWoolEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.countItem
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.takeItem
import kotlin.math.roundToInt

object Rancher {

    fun initialize() {
        "饲料调配".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下一次喂养幼年动物时会立刻使其长大"
        }
        "牧原欢歌".discharge { name, level ->
            finish(name)
            val item = inventory.itemInMainHand
            val type = item.type
            val range = 4.0 + level * 4
            if (type == Material.SHEARS) {
                getNearbyEntities(range, range, range).filterIsInstance<Sheep>().forEach { sheep ->
                    val event = PlayerShearEntityEvent(this, sheep, item, EquipmentSlot.HAND)
                    Bukkit.getPluginManager().callEvent(event)
                    sheep.isSheared = true
                    sheep.world.dropItemNaturally(
                        sheep.location,
                        ItemStack(Material.WHITE_WOOL, (1 + 3 * Math.random()).roundToInt())
                    )
                }
                return@discharge "§a技能 ${display(name)} §7释放成功，自动为周围的羊剪毛"
            }
            if (type == Material.BUCKET) {
                val flag = getNearbyEntities(range, range, range).any { it is Goat || it is Cow }
                if (flag) {
                    val amount = inventory.countItem { it.type == Material.BUCKET }
                    if (takeItem(amount) { it.type == Material.BUCKET }) {
                        giveItem(ItemStack(Material.MILK_BUCKET, amount))
                    }
                    return@discharge "§a技能 ${display(name)} §7释放成功，自动为周围的牛挤奶"
                } else
                    return@discharge "§a技能 ${display(name)} §7释放成功，但是周围貌似可以没用挤奶的生物"
            }

            "§a技能 ${display(name)} §7释放成功，但是手上未持剪刀或空桶"
        }

        addLimit(HandlerType.PLACE, "现代化牧业" to 1, Material.DISPENSER)

        Material.SPAWNER.addHighListener(HandlerType.BREAK) { event, player, _ ->
            val breakEvent = event as BlockBreakEvent
            if (player.meetRequirement("克隆技术")) {
                val block = breakEvent.block
                val spawner = block.state as CreatureSpawner
                val type = spawner.spawnedType!!
                val amount = 3 + Math.random() * 3
                val egg = ItemStack(Material.valueOf(type.toString() + "_SPAWN_EGG"), amount.roundToInt())
                block.world.dropItem(block.location, egg)
                "${display("克隆技术")} §7使得刷怪笼掉落了额外的刷怪蛋"
            } else null
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun drink(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.MILK_BUCKET) {
            val player = event.player
            val biome = player.location.block.biome
            if (biome.toString().contains("PLAIN") && player.meetRequirement("游牧习俗")) {
                player.health = minOf(player.health + 8.0, player.maxHealth)
                player.sendMessage("§a生涯系统 §7>> ${display("游牧习俗")} §7使得本次喝牛奶获得额外增益")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun shear(event: PlayerShearEntityEvent) {
        val player = event.player
        if (!player.meetRequirement("牧场主", 0)) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法剪羊毛，需要解锁 ${display("牧场主")}")
        } else player.giveExp(1)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun spawner(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return
        val block = event.clickedBlock!!
        val player = event.player
        if (block.type == Material.SPAWNER) {
            if (!player.isOp) {
                event.isCancelled = true
                player.sendMessage("§b繁星工坊 §7>> 服务器禁止交互刷怪笼")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun feed(event: PlayerInteractEntityEvent) {
        val animal = event.rightClicked
        val player = event.player
        if (animal !is Animals)
            return
        val main = player.inventory.itemInMainHand
        val off = player.inventory.itemInOffHand

        if (main.type == Material.BUCKET || off.type == Material.BUCKET) {
            if (animal is Goat || animal is Cow) {
                if (!player.meetRequirement("牧场主", 0)) {
                    event.isCancelled = true
                    player.sendMessage("§a生涯系统 §7>> 无法挤奶，需要解锁 ${display("牧场主")}")
                } else player.giveExp(1)
            }
        }

        if (animal.isBreedItem(main.type) || animal.isBreedItem(off.type)) {
            if (!player.meetRequirement("牧场主", 0)) {
                event.isCancelled = true
                player.sendMessage("§a生涯系统 §7>> 无法喂养/繁殖，需要解锁 ${display("牧场主")}")
            } else player.giveExp(1)

            val level = player.spellLevel("动物育种")
            val rate = if (level == 3) 0.02 else 0.005 * level
            if (Math.random() <= rate) {
                val egg = ItemStack(
                    Material.valueOf(animal.type.toString() + "_SPAWN_EGG")
                )
                player.giveItem(egg)
                player.sendMessage("§a生涯系统 §7>> §a技能 ${display("动物育种")} §7使得本次喂养时获得刷怪蛋")
            }

            if (player.isDischarging("饲料调配") && !animal.isAdult) {
                player.finish("饲料调配")
                animal.setAdult()
                player.sendMessage("§a生涯系统 §7>> §a技能 ${display("动物育种")} §7使得本次喂养时动物立刻长大")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun dye(event: SheepDyeWoolEvent) {
        val player = event.player ?: return
        if (!player.meetRequirement("牧场主", 0)) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法染色动物，需要解锁 ${display("牧场主")}")
        } else player.giveExp(1)
    }
}