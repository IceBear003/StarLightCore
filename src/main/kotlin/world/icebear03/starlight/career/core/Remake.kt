package world.icebear03.starlight.career.core

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.utils.clearCooldownStamp
import world.icebear03.starlight.utils.clearDischargeStamp

fun Player.remakeCareer() {
    career().remake()
    clearCooldownStamp()
    clearDischargeStamp()
}