package world.icebear03.starlight.career.spell.discharge

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.actionBar
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendActionBar
import world.icebear03.starlight.career
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.utils.*

object DischargeHandler {
    val dischargeMap = mutableMapOf<String, Player.(id: String, level: Int) -> String?>()
    val finishMap = mutableMapOf<String, Player.(id: String, level: Int) -> String?>()

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                val career = player.career()
                career.autoDischarges.forEach spells@{ name ->
                    val msg = discharge(player, name, true)
                    player.actionBar("§7自动释放 > $msg")
                }
            }
        }
    }

    fun discharge(player: Player, name: String, isAuto: Boolean = false): String? {
        if (player.isDischarging(name))
            return "${display(name)} §7已经处于释放状态"

        val career = player.career()
        val spell = getSpell(name) ?: return "§a技能§7/§d顿悟§7不存在"

        val level = career.getSpellLevel(spell)
        if (level <= 0)
            return "请先升级${spell.prefix()}§7 ${spell.display()}"

        val cd = player.checkCooldownStamp(name, spell.cd(level))
        val duration = spell.duration(level)
        if (!cd.first) {
            return "无法释放 ${spell.display()} §7还需等待 §e${cd.second}秒"
        }

        if (duration != -1) {
            submit(delay = 20L * duration) {
                //注意玩家是否还在线，或者以及重生
                finish(player, name)
                if (career.getSpellLevel(spell) > 0) {
                    if (isAuto) {
                        player.sendMessage("§a生涯系统 §7>> ${spell.prefix()} §7${spell.display()} §7已经结束")
                    } else {
                        player.sendActionBar("§7自动释放 > ${spell.prefix()} §7${spell.display()} §7已经结束")
                    }
                }
            }
        }

        player.addDischargeStamp(name)
        return dischargeMap[name]?.invoke(player, name, level)
    }

    fun finish(player: Player, name: String) {
        if (!player.isDischarging(name))
            return

        player.removeDischargeStamp(name)
        player.addCooldownStamp(name)

        val career = player.career()
        val spell = getSpell(name) ?: return

        val level = career.getSpellLevel(spell)
        if (level <= 0)
            return

        val msg = finishMap[name]?.invoke(player, name, level) ?: return
        player.sendMessage("§a生涯系统 §7>> $msg")
    }
}