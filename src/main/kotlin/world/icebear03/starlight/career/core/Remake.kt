package world.icebear03.starlight.career.core

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.career.spell.old.clearCooldownStamp
import world.icebear03.starlight.career.spell.old.clearDischargeStamp

fun Player.remakeCareer() {
    career().remake()
    clearCooldownStamp()
    clearDischargeStamp()
}