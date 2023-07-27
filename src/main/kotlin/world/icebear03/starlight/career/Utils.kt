package world.icebear03.starlight.career

import org.bukkit.entity.Player
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.branch.BranchLoader
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellLoader
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.loadCareerData

fun getClass(name: String?): Class? {
    return ClassLoader.fromName(name)
}

fun getBranch(name: String?): Branch? {
    return BranchLoader.fromName(name)
}

fun getSpell(name: String?): Spell? {
    return SpellLoader.fromName(name)
}

fun display(name: String): String {
    val basic = getClass(name) ?: getBranch(name) ?: getSpell(name) ?: return name
    return basic.display()
}

fun Player.hasBranch(name: String?): Boolean {
    return loadCareerData(this).hasBranch(name)
}

fun Player.branchLevel(name: String?): Int {
    return loadCareerData(this).getBranchLevel(name)
}

fun Player.spellLevel(name: String?, includeResonate: Boolean = true): Int {
    return maxOf(
        loadCareerData(this).getSpellLevel(name),
        if (includeResonate) Resonate.getSkillResonatedLevel(this, name)
        else -1
    )
}

fun Player.reachSpell(name: String?, level: Int, includeResonate: Boolean = true): Boolean {
    return spellLevel(name, includeResonate) >= level
}