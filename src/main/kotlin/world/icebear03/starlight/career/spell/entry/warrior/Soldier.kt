package world.icebear03.starlight.career.spell.entry.warrior

import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.isMainhand
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.discharge
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.finish
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.isDischarging

object Soldier {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.filter { it.meetRequirement("士兵", 0) }.forEach { player ->
                if (player.health <= 10.0)
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, 2, 1)
            }
        }

        "掩体机动".discharge { name, spellLevel ->
            finish(name)
            val duration = when (spellLevel) {
                2 -> 12
                3 -> 15
                else -> 10
            }
            var level = if (spellLevel == 3) 2 else 1
            if (health <= 20) level += 4 - spellLevel
            effect(PotionEffectType.DAMAGE_RESISTANCE, duration, level)
            "§a技能 ${display(name)} §7释放成功，一段时间内受到伤害降低"
        }

        "决战冲锋".discharge { name, level ->
            effect(PotionEffectType.SPEED, 5, if (level >= 3) 4 else 3)
            "§a技能 ${display(name)} §7释放成功，立即获得短暂迅捷效果，且接下来一段时间内伤害增加"
        }

        "无畏突刺".discharge { name, level ->
            "§d ${display(name)} §7释放成功，一段时间内每次攻击都将会伴随位移"
        }
    }

    @SubscribeEvent
    fun click(event: PlayerInteractEvent) {
        val player = event.player
        if (!player.isDischarging("无畏突刺"))
            return
        if (player.attackCooldown <= 0.5)
            return
        if (!event.isMainhand())
            return
        val type = event.item?.type ?: return
        val typeString = type.toString()
        if (typeString.endsWith("_SWORD") || typeString.endsWith("_AXE")) {
            player.velocity = player.eyeLocation.direction.normalize().multiply(1.5)
        }
    }

    @SubscribeEvent
    fun click(event: PlayerInteractEntityEvent) {
        val player = event.player
        if (!player.isDischarging("无畏突刺"))
            return
        if (!event.isMainhand())
            return
        val type = player.inventory.itemInMainHand.type
        val typeString = type.toString()
        if (typeString.endsWith("_SWORD") || typeString.endsWith("_AXE")) {
            player.velocity = player.eyeLocation.direction.normalize().multiply(1.5)
        }
    }
}