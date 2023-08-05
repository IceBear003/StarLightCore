package world.icebear03.starlight.tool.mechanism

import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.core.remakeCareer
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.station.core.remakeStamina
import world.icebear03.starlight.tool.world.WorldBorder
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.set

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        if (!player.hasPlayedBefore()) {
            respawn(player, RespawnType.FIRST_JOIN)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun respawn(event: PlayerRespawnEvent) {
        val player = event.player
        val ticksLived = player.getStatistic(Statistic.TIME_SINCE_DEATH)
        respawn(player, if (ticksLived <= 200) RespawnType.DEATH else RespawnType.ENDER_PORTAL, event)
    }

    fun respawn(player: Player, respawnType: RespawnType, event: PlayerRespawnEvent? = null) {
        player["respawn_stamp", PersistentDataType.LONG] = System.currentTimeMillis()
        if (respawnType != RespawnType.ENDER_PORTAL)
            player.sendMessage("§b繁星工坊 §7>> 这一觉睡了好久...我这是到哪了?")
        when (respawnType) {
            RespawnType.DEATH -> player.sendMessage("§b繁星工坊 §7>> 本次重生保护，持续时间§e10分钟")
            RespawnType.FIRST_JOIN -> player.sendMessage("§b繁星工坊 §7>> 进入新玩家保护模式，持续时间§e10分钟")
            RespawnType.ENDER_PORTAL -> player.sendMessage("§b繁星工坊 §7>> 进入相位转移保护模式，持续时间§e10分钟")
        }
        player.sendMessage("               §7|—— 获得持续的§b20%免伤")
        player.sendMessage("               §7|—— 体力值损耗§b减慢80%")
        if (respawnType == RespawnType.DEATH)
            player.sendMessage("               §7|—— 跑向死亡箱时获得§b速度§7效果")

        val randomLoc = WorldBorder.randomLocation(world)
        if (respawnType != RespawnType.FIRST_JOIN)
            event!!.respawnLocation = randomLoc.clone().add(0.0, 3.0, 0.0)

        if (respawnType != RespawnType.ENDER_PORTAL) {
            player.remakeStamina()
            player.remakeCareer()
        } else {
            player.addStamina(400.0)
        }

        submit(delay = 1) {
            player.effect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1)

            if (respawnType == RespawnType.FIRST_JOIN)
                player.teleport(randomLoc.clone().add(0.0, 3.0, 0.0))
            if (randomLoc.block.isLiquid)
                world.spawnEntity(randomLoc.clone().add(0.0, 1.0, 0.0), EntityType.BOAT)

            val attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) ?: return@submit
            attribute.baseValue = 40.0
            player.health = player.maxHealth
        }
    }


    fun isInProtection(player: Player, time: Int = 600): Boolean {
        val current = System.currentTimeMillis()
        return current - (player["respawn_stamp", PersistentDataType.LONG] ?: 0) < time * 1000
    }

    enum class RespawnType {
        DEATH,
        FIRST_JOIN,
        ENDER_PORTAL
    }
}