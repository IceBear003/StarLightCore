package world.icebear03.starlight.utils

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.career.meetRequirement
import java.util.*

val dischargeStamp = mutableMapOf<UUID, MutableMap<String, Long>>()

fun Player.addDischargeStamp(name: String) {
    dischargeStamp.getOrPut(this.uniqueId) { mutableMapOf() }[name] = System.currentTimeMillis()
}

fun Player.removeDischargeStamp(name: String) {
    dischargeStamp.getOrPut(this.uniqueId) { mutableMapOf() }.remove(name)
}

fun Player.clearDischargeStamp() {
    dischargeStamp.remove(uniqueId)
}

fun Player.isDischarging(name: String): Boolean {
    if (!this.meetRequirement(name, 1))
        return false

    val stamp = (dischargeStamp[this.uniqueId] ?: return false)[name] ?: return false
    val period = (System.currentTimeMillis() - stamp) / 1000.0

    val spell = getSpell(name)
    if (spell != null) {
        val level = career().getSpellLevel(spell)
        val duration = spell.duration(level)
        if (duration == -1) {
            return true
        }
        return duration >= period
    }

    return false
}
