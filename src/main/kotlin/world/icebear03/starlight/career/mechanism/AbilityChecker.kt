package world.icebear03.starlight.career.mechanism

import org.bukkit.entity.Player
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.hasEureka
import world.icebear03.starlight.career.hasSkill
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.utils.MathUtils

fun Player.hasAbility(limit: Pair<String, Int>): Boolean {
    val string = limit.first
    val level = limit.second
    return this.hasBranch(string, level) ||
            this.hasSkill(string, level) ||
            this.hasEureka(string)
}


fun displayLimit(limit: Pair<String, Int>): String {
    val level = MathUtils.numToRoman(limit.second, false)

    val branch = Branch.fromId(limit.first)
    if (branch != null)
        return "${branch.display()} $level"
    val skill = Skill.fromId(limit.first)
    if (skill != null)
        return "${skill.display()} $level"
    val eureka = Eureka.fromId(limit.first)
    if (eureka != null)
        return "${eureka.display()} $level"
    return "${limit.first} $level"
}

fun Player.checkAbility(limits: MutableList<Pair<String, Int>>?): Pair<Boolean, List<String>> {
    if (limits == null)
        return true to listOf()
    if (limits.isEmpty())
        return true to listOf()
    var result = false
    val list = mutableListOf<String>()
    limits.forEach {
        if (this.hasAbility(it))
            result = true
        else {
            list += displayLimit(it)
        }
    }
    return result to list
}