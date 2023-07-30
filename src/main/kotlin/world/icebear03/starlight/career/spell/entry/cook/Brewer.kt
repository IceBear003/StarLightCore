package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.handler.limit.LimitType
import world.icebear03.starlight.utils.recipe.CraftResult
import world.icebear03.starlight.utils.recipe.registerLowest

object Brewer {
    fun initialize() {
        addLimit(LimitType.USE, "药剂师" to 0, Material.BREWING_STAND)

        Material.SUSPICIOUS_STEW.registerLowest { player, _ ->
            if (!player.meetRequirement("药剂师", 0) && Math.random() <= 0.5) {
                CraftResult.FAIL to "§a生涯系统 §7>> 合成谜之炖菜失败，解锁 §e职业分支 §7${display("药剂师")} §7可以提高成功率"
            } else CraftResult.ALLOW to null
        }
    }

    class Passive {

    }
}