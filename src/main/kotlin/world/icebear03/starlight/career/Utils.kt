package world.icebear03.starlight.career

import org.bukkit.entity.Player
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.branch.BranchLoader
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellLoader

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

fun Player.hasClass(name: String?): Boolean {
    return career().hasClass(name)
}

fun Player.hasBranch(name: String?): Boolean {
    return career().hasBranch(name)
}

fun Player.branchLevel(name: String?): Int {
    return career().getBranchLevel(name)
}

fun Player.spellLevel(name: String?, includeResonate: Boolean = true): Int {
    return maxOf(
        career().getSpellLevel(name),
        if (includeResonate) Resonate.getSkillResonatedLevel(this, name) else -1
    )
}

fun Player.reachSpell(name: String?, level: Int, includeResonate: Boolean = true): Boolean {
    return spellLevel(name, includeResonate) >= level
}

fun Player.forget(name: String): Pair<Boolean, String> {
    return career().forget(name)
}