package world.icebear03.starlight.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.format
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.addStamina
import world.icebear03.starlight.tool.mechanism.AFK
import java.util.*


val cooldownStamps = mutableMapOf<UUID, MutableMap<String, Long>>()
fun Player.addCooldownStamp(key: String) {
    cooldownStamps.getOrPut(this.uniqueId) { mutableMapOf() }[key] = System.currentTimeMillis()
}

fun Player.removeCooldownStamp(key: String) {
    cooldownStamps.getOrPut(this.uniqueId) { mutableMapOf() }.remove(key)
}

fun Player.clearCooldownStamp() {
    cooldownStamps.remove(this.uniqueId)
}

//pair#first 冷却是否结束，冷却中为false
//pair#second 冷却若未结束，离结束还剩下的时间（秒）
fun Player.checkCooldownStamp(key: String, cdInSec: Int): Pair<Boolean, Double> {
    return checkCooldownStamp(key, cdInSec.toDouble())
}

fun Player.checkCooldownStamp(key: String, cdInSec: Double): Pair<Boolean, Double> {
    val stamp = (cooldownStamps[this.uniqueId] ?: return true to 0.0)[key] ?: return true to 0.0
    val period = (System.currentTimeMillis() - stamp) / 1000.0
    val left = (cdInSec - period).format(1)
    return (period >= cdInSec) to left
}

object CooldownFreezer {

    fun initialize() {
        submit(period = 2L) {
            onlinePlayers.filter { AFK.isAFKing(it) }.forEach { player ->
                val map = cooldownStamps[player.uniqueId]?.toMutableMap() ?: return@forEach
                map.forEach { (name, stamp) ->
                    cooldownStamps[player.uniqueId]!![name] = stamp + 100
                }
            }
        }
    }
}

object CooldownData {

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun quit(event: PlayerQuitEvent) {
        val player = event.player
        val cooldown = cooldownStamps[player.uniqueId]
        cooldownStamps.remove(player.uniqueId)
        player["cooldown", PersistentDataType.STRING] = Gson().toJson(cooldown)
        player["quit_stamp", PersistentDataType.LONG] = System.currentTimeMillis()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        val string = player["cooldown", PersistentDataType.STRING] ?: run {
            cooldownStamps[player.uniqueId] = mutableMapOf()
            return
        }

        val resultType = object : TypeToken<MutableMap<String, Long>>() {}.type
        val cooldown: MutableMap<String, Long> = Gson().fromJson(string, resultType) ?: run {
            cooldownStamps[player.uniqueId] = mutableMapOf()
            return
        }

        val quitStamp = player["quit_stamp", PersistentDataType.LONG] ?: System.currentTimeMillis()
        val offlinePeriod = System.currentTimeMillis() - quitStamp
        //懒得在stamina再加了，干脆写在这里了
        player.addStamina(offlinePeriod / 1000 * 0.25)
        cooldown.toMap().forEach { (name, stamp) ->
            cooldown[name] = stamp + offlinePeriod
        }
        cooldownStamps[player.uniqueId] = cooldown
    }
}