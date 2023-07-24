package world.icebear03.starlight.career.mechanism.limit

import org.bukkit.entity.Player
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.loadCareerData
import world.icebear03.starlight.utils.MathUtils

fun Player.hasAbility(limit: Pair<String, Int>): Boolean {
    val data = loadCareerData(this)
    if (Branch.fromId(limit.first) != null)
        return data.getBranchLevel(limit.first) >= limit.second
    if (Skill.fromId(limit.first) != null)
        return data.getSkillLevel(limit.first) >= limit.second
    if (Eureka.fromId(limit.first) != null)
        return data.hasEureka(limit.first)
    return false
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