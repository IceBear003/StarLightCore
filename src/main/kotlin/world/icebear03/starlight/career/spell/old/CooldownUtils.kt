package world.icebear03.starlight.career.spell.old

import org.bukkit.entity.Player
import taboolib.common5.format
import java.util.*


val cooldownStamps = mutableMapOf<UUID, MutableMap<String, Long>>()
fun Player.addCooldownStamp(key: String) {
    cooldownStamps.getOrPut(this.uniqueId) { mutableMapOf() }[key] = System.currentTimeMillis()
}

fun Player.clearCooldownStamp() {
    cooldownStamps.remove(this.uniqueId)
}

//pair#first 冷却是否结束，冷却中为false
//pair#second 冷却若未结束，离结束还剩下的时间（秒）
fun Player.checkCooldownStamp(key: String, cdInSec: Int): Pair<Boolean, Double> {
    val stamp = (cooldownStamps[this.uniqueId] ?: return true to 0.0)[key] ?: return true to 0.0
    val period = (System.currentTimeMillis() - stamp) / 1000.0
    val left = (cdInSec - period).format(1)
    return (period >= cdInSec) to left
}