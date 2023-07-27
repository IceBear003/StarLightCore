package world.icebear03.starlight.career.spell

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.career.meetRequirement
import java.util.*

val dischargeStamp = mutableMapOf<UUID, MutableMap<String, Long>>()

fun Player.addDischargeStamp(id: String) {
    dischargeStamp.getOrPut(this.uniqueId) { mutableMapOf() }[id] = System.currentTimeMillis()
}

fun Player.removeDischargeStamp(id: String) {
    dischargeStamp.getOrPut(this.uniqueId) { mutableMapOf() }.remove(id)
}

fun Player.isDischarging(name: String, removeIfConsumable: Boolean = true): Boolean {
    if (!this.meetRequirement(name, 1))
        return false

    val stamp = (dischargeStamp[this.uniqueId] ?: return false)[name] ?: return false
    val period = (System.currentTimeMillis() - stamp) / 1000.0

    val spell = getSpell(name)
    if (spell != null) {
        val level = career().getSpellLevel(spell)
        val duration = spell.duration(level)
        if (duration == -1) {
            if (removeIfConsumable)
                this.removeDischargeStamp(name)
            return true
        }
        return duration >= period
    }

    return false
}

fun String.defineDischarge(function: Player.(id: String, level: Int) -> String?) {
    DischargeHandler.dischargeMap[this] = function
}

fun String.defineFinish(function: Player.(id: String, level: Int) -> Unit) {
    DischargeHandler.finishMap[this] = function
}