package world.icebear03.starlight.career.point

import org.bukkit.entity.Player
import world.icebear03.starlight.career

object Fish {
    fun fish(player: Player) {
        val career = player.career()
        val rate = if (career.hasClass("农夫")) 0.001 else 0.0005
        if (Math.random() <= rate) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你钓鱼时偶然获得了§a1技能点")
        }
    }
}