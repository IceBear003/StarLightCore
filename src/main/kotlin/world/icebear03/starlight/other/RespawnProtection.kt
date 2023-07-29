package world.icebear03.starlight.other

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.secondLived

object RespawnProtection {

    lateinit var world: World

    fun initialize() {
        world = Bukkit.getWorld("world")!!
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        if (!player.hasPlayedBefore()) {
            submit(delay = 1) {
                player.effect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1)
                val randomLoc = WorldBorder.randomLocation(world)
                player.teleport(randomLoc.clone().add(0.0, 1.5, 0.0))
                submit(delay = 5) {
                    if (randomLoc.block.isLiquid) {
                        world.spawnEntity(randomLoc.add(0.0, 1.0, 0.0), EntityType.BOAT)
                    }
                }
                player.sendMessage("§b繁星工坊 §7>> 这一觉睡了好久...我这是到哪了?")
                player.sendMessage("§b繁星工坊 §7>> 进入新玩家保护模式，持续时间§e10分钟")
                player.sendMessage("               §7|—— 获得持续的§b20%免伤")
                player.sendMessage("               §7|—— 体力值损耗§b减慢80%")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun respawn(event: PlayerRespawnEvent) {
        val player = event.player

        val randomLoc = WorldBorder.randomLocation(world)
        event.respawnLocation = randomLoc.clone().add(0.0, 1.5, 0.0)

        submit(delay = 1) {
            player.effect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1)
            player.sendMessage("§b繁星工坊 §7>> 这一觉睡了好久...我这是到哪了?")
            player.sendMessage("§b繁星工坊 §7>> 本次重生保护，持续时间§e10分钟")
            player.sendMessage("               §7|—— 获得持续的§b20%免伤")
            player.sendMessage("               §7|—— 体力值损耗§b减慢80%")
            player.sendMessage("               §7|—— 跑向死亡箱时获得§b速度§7效果")
            if (randomLoc.block.isLiquid) {
                world.spawnEntity(randomLoc.add(0.0, 1.0, 0.0), EntityType.BOAT)
            }
        }
    }

    fun isInProtection(player: Player, time: Int = 600): Boolean {
        return player.secondLived() < time
    }
}