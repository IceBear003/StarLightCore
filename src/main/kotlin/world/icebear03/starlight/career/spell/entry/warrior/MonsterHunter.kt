package world.icebear03.starlight.career.spell.entry.warrior

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.discharge
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.finish
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.utils.effect

object MonsterHunter {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.filter { it.meetRequirement("怪物猎人", 0) }.forEach { player ->
                if (player.health <= 10.0)
                    player.effect(PotionEffectType.INCREASE_DAMAGE, 2, 1)

                if (player.meetRequirement("暗夜行者")) {
                    if (player.world.time in 0..12500) {
                        if (player.health >= 28.0)
                            player.health = 28.0
                        player.maxHealth = 28.0
                        player.effect(PotionEffectType.REGENERATION, 3, 2)
                    } else {
                        player.maxHealth = 40.0
                    }
                }
            }
        }

        "浴血强攻".discharge { name, spellLevel ->
            finish(name)
            var level = if (spellLevel == 3) 2 else 1
            val duration = when (spellLevel) {
                2 -> 12
                3 -> 15
                else -> 10
            }
            if (health <= 20) {
                level += if (level == 2) 2 else 1
            }
            effect(PotionEffectType.INCREASE_DAMAGE, duration, level)
            "§a技能 ${display(name)} §7释放成功，获得效果 §c力量"
        }

        "我即梦魇".discharge { name, level ->
            val range = 8.0 + 8 * level
            val duration = 20 + 10 * level
            getNearbyEntities(range, range, range).filterIsInstance<Monster>().forEach { monster ->
                monster.effect(PotionEffectType.GLOWING, duration, 1)
                monster.effect(PotionEffectType.SLOW, duration, 1)
            }
            "§a技能 ${display(name)} §7释放成功，使附近怪物高亮且缓慢，一定时间内对怪物伤害增加"
        }

        "圣洁泉源".discharge { name, _ ->
            finish(name)
            getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<Player>().forEach {
                it.removePotionEffect(PotionEffectType.GLOWING)
                it.removePotionEffect(PotionEffectType.SLOW)
                it.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                it.removePotionEffect(PotionEffectType.BAD_OMEN)
                it.removePotionEffect(PotionEffectType.BLINDNESS)
                it.removePotionEffect(PotionEffectType.CONFUSION)
                it.removePotionEffect(PotionEffectType.DARKNESS)
                it.removePotionEffect(PotionEffectType.HUNGER)
                it.removePotionEffect(PotionEffectType.LEVITATION)
                it.removePotionEffect(PotionEffectType.POISON)
                it.removePotionEffect(PotionEffectType.UNLUCK)
                it.removePotionEffect(PotionEffectType.WEAKNESS)
                it.removePotionEffect(PotionEffectType.WITHER)
                it.sendMessage("§a生涯系统 §7>> §e${this.name} §7清除了你的所有debuff")
            }
            "§d顿悟 ${display(name)} §7释放成功，使附近玩家Debuff清除"
        }
    }

    @SubscribeEvent
    fun kill(event: EntityDeathEvent) {
        val entity = event.entity
        val killer = entity.killer ?: return
        if (killer.meetRequirement("斩首")) {
            val pair = when (entity.type) {
                EntityType.SKELETON -> Material.SKELETON_SKULL to 0.02
                EntityType.WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL to 0.05
                EntityType.ZOMBIE -> Material.ZOMBIE_HEAD to 0.02
                EntityType.CREEPER -> Material.CREEPER_HEAD to 0.02
                EntityType.PIGLIN -> Material.PIGLIN_HEAD to 0.01
                EntityType.ENDER_DRAGON -> Material.DRAGON_HEAD to 0.05
                else -> Material.AIR to -1.0
            }
            if (Math.random() <= pair.second) {
                entity.world.dropItemNaturally(entity.location, ItemStack(pair.first))
                killer.sendMessage("§a生涯系统 §7>> §d顿悟 ${display("斩首")} §7使得本次击杀掉落了怪物头颅")
            }
        }
    }
}