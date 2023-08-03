package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Campfire
import org.bukkit.entity.Animals
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.kill
import world.icebear03.starlight.career.*
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.getBlockAside

object Butcher {

    val terrestrialAnimals = listOf(
        EntityType.BAT,
        EntityType.CHICKEN,
        EntityType.COW,
        EntityType.DONKEY,
        EntityType.HORSE,
        EntityType.MUSHROOM_COW,
        EntityType.MULE,
        EntityType.PARROT,
        EntityType.PIG,
        EntityType.RABBIT,
        EntityType.SHEEP,
        EntityType.SKELETON_HORSE,
        EntityType.TURTLE,
        EntityType.GOAT,
        EntityType.CAT,
        EntityType.FROG,
        EntityType.BEE,
        EntityType.FOX,
        EntityType.HOGLIN
    )

    val cookedMeat = listOf(
        Material.COOKED_PORKCHOP,
        Material.COOKED_BEEF,
        Material.COOKED_CHICKEN,
        Material.COOKED_MUTTON,
        Material.COOKED_RABBIT
    )

    val meatAnimal = mapOf(
        EntityType.MUSHROOM_COW to (Material.BEEF to Material.COOKED_BEEF),
        EntityType.RABBIT to (Material.RABBIT to Material.COOKED_RABBIT),
        EntityType.PIG to (Material.PORKCHOP to Material.COOKED_PORKCHOP),
        EntityType.HOGLIN to (Material.ROTTEN_FLESH to Material.ROTTEN_FLESH),
        EntityType.COW to (Material.BEEF to Material.COOKED_BEEF),
        EntityType.SHEEP to (Material.MUTTON to Material.COOKED_MUTTON),
        EntityType.CHICKEN to (Material.CHICKEN to Material.COOKED_CHICKEN)
    )

    fun initialize() {
        "困兽天敌".discharge { name, level ->
            val range = 4.0 + 4 * level
            val duration = 10 * level
            getNearbyEntities(range, range, range).filter { terrestrialAnimals.contains(it.type) }.forEach {
                val creature = it as LivingEntity
                creature.effect(PotionEffectType.GLOWING, duration, 1)
                creature.effect(PotionEffectType.SLOW, duration, level)
            }
            finish(name)
            "§a技能 ${display(name)} §7释放成功，高亮并减速附近陆生生物"
        }

        "烤肉专家".discharge { name, level ->
            val max = if (level == 3) 2 else 1
            var amount = 0

            location.getBlockAside(3, Material.CAMPFIRE, Material.SOUL_CAMPFIRE).forEach { loc ->
                if (amount >= max)
                    return@forEach

                val block = loc.block
                val state = block.state
                var flag = false
                if (state is Campfire) {
                    for (index in 0 until state.size) {
                        state.getItem(index) ?: continue
                        state.setCookTimeTotal(index, 1)
                        flag = true
                    }
                }
                if (flag) {
                    state.update()
                    block.world.spawnParticle(Particle.LAVA, loc.add(0.0, 0.5, 0.0), 3)
                    amount += 1
                }
            }

            finish(name)
            "§a技能 ${display(name)} §7释放成功，使得附近§a${amount}个§7营火立即完成烹饪"
        }

        "追猎".discharge spell@{ name, _ ->
            finish(name)
            val trace =
                world.rayTraceEntities(location.add(0.0, 1.5, 0.0), eyeLocation.direction.normalize(), 50.0) { it.uniqueId != uniqueId }
                    ?: return@spell "§d顿悟 ${display(name)} §7释放成功，但是并没有锁定一个生物"
            val entity = trace.hitEntity
            if (entity !is LivingEntity)
                return@spell "§d顿悟 ${display(name)} §7释放成功，但是并没有锁定一个生物"
            entity.effect(PotionEffectType.GLOWING, 60, 2)
            entity.effect(PotionEffectType.WEAKNESS, 60, 2)
            submit(delay = 55 * 20) {
                if (entity.isDead && meetRequirement("追猎")) {
                    giveExp(25)
                    sendMessage("§a生涯系统 §7>> 击杀 ${display(name)} §7锁定的生物，经验值§a+25")
                }
            }
            "§d顿悟 ${display(name)} §7释放成功，已锁定生物，若在60s内完成击杀可获得§b额外经验"
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun attack(event: EntityDamageByEntityEvent) {
        val player = event.damager
        val entity = event.entity
        val type = entity.type
        if (player !is Player || entity !is LivingEntity) {
            return
        }
        val isMeatAnimal = meatAnimal.containsKey(type)
        val world = entity.world
        val loc = entity.location
        if (player.meetRequirement("放血") && player.fallDistance > 0 && !player.isOnGround && isMeatAnimal) {
            entity.effect(PotionEffectType.POISON, 5, 1)
            submit(delay = 100) {
                entity.getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<LivingEntity>().filter {
                    meatAnimal.containsKey(it.type) && it.health <= it.maxHealth * 0.5
                }.forEach { creature ->
                    world.dropItemNaturally(creature.location, ItemStack(meatAnimal[creature.type]!!.first))
                    creature.kill()
                }
            }
        }
        val usingAxe = player.inventory.itemInMainHand.type.toString().contains("_AXE")
        if (player.meetRequirement("血腥屠宰者") && usingAxe) {
            event.damage = event.damage * 1.25
            val equip = entity.equipment ?: return
            if (equip.armorContents.isEmpty()) {
                event.damage += 6
            }
        }

        if (!player.meetRequirement("屠夫", 0) && usingAxe)
            event.damage *= 0.5

        val isBurning = entity.fireTicks > 0

        submit(delay = 1L) {
            if (!entity.isDead)
                return@submit

            if (usingAxe && entity is Animals && player.meetRequirement("屠夫", 0)) {
                world.dropItemNaturally(loc, ItemStack(Material.BONE))
            }

            if (player.meetRequirement("庖丁遗风") && meatAnimal.containsKey(type)) {
                val level = player.spellLevel("庖丁遗风")
                val meat = ItemStack(meatAnimal[type]!!.first)
                val cooked = ItemStack(meatAnimal[type]!!.second)
                world.dropItemNaturally(loc, meat)

                if (level == 2 && Math.random() <= 0.25 && !isBurning) {
                    world.dropItemNaturally(loc, meat)
                }
                if (level == 3 && Math.random() <= 0.5) {
                    world.dropItemNaturally(loc, meat)
                    if (isBurning && Math.random() <= 0.25) {
                        world.dropItemNaturally(loc, cooked)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun eat(event: FoodLevelChangeEvent) {
        val item = event.item ?: return
        val type = item.type
        val player = event.entity as Player
        if (player.meetRequirement("屠夫", 0) && cookedMeat.contains(type)) {
            event.foodLevel += 4
        }
    }
}