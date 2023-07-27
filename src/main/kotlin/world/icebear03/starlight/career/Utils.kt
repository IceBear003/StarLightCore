package world.icebear03.starlight.career

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