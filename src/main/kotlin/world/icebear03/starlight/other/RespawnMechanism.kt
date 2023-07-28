package world.icebear03.starlight.other

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.utils.effect

object RespawnProtection {

    fun initialize() {
        submit(period = 100L) {
            onlinePlayers.forEach {
                if (!isInProtection(it))
                    return@forEach

                val deathLoc = it.lastDeathLocation ?: return@forEach
                val loc = it.location
                val vector = Vector(
                    deathLoc.x - loc.x,
                    0.0,
                    deathLoc.z - loc.z
                )

                val direction = it.eyeLocation.direction
                direction.y = 0.0

                if (vector.angle(direction) <= 0.8) {
                    it.effect(PotionEffectType.SPEED, 8, 1)
                }
            }
        }
    }

    @SubscribeEvent
    fun respawn(event: PlayerRespawnEvent) {
        val player = event.player
        submit(delay = 2) {
            player.effect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1)
            val randomLoc = player.world.randomLocation()
            if (randomLoc.block.isLiquid) {
                player.world.spawnEntity(randomLoc, EntityType.BOAT)
            }
            player.teleport(randomLoc)
            player.sendMessage("§b繁星工坊 §7>> 这一觉睡了好久...我这是到哪了?")
            player.sendMessage("§b繁星工坊 §7>> 本次重生保护，持续时间§e10分钟")
            player.sendMessage("               §7|—— 获得持续的§b20%免伤")
            player.sendMessage("               §7|—— 体力值损耗§b减慢80%")
            player.sendMessage("               §7|—— 跑向死亡箱时获得§b速度§7效果")
        }
    }

    fun isInProtection(player: Player): Boolean {
        val pdc = player.persistentDataContainer
        val lastDeathStamp = pdc.get(DeathStamp.deathStampKey, PersistentDataType.LONG) ?: return false
        return System.currentTimeMillis() - lastDeathStamp < 360 * 1000
    }
}