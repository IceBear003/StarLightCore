package world.icebear03.starlight.career.core

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.tool.mechanism.DeathPunishment
import world.icebear03.starlight.utils.clearCooldownStamp
import world.icebear03.starlight.utils.clearDischargeStamp

fun Player.remakeCareer() {
    val isPunished = DeathPunishment.isPunished(this)
    if (isPunished)
        sendMessage("§a生涯系统 §7>> 由于您本小时内死亡次数过多，为防止刷职业，短时间内重生最多获得两个职业")
    career().remake(isPunished)
    clearCooldownStamp()
    clearDischargeStamp()
}