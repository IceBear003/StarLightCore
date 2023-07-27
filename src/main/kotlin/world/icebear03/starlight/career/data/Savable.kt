package world.icebear03.starlight.career.data

import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.getBranch
import world.icebear03.starlight.career.getClass
import world.icebear03.starlight.career.getSpell

data class Savable(
    val classes: Map<String, List<String>>,
    val branches: Map<String, Map<String, Int>>,
    val spells: Map<String, Int>,
    var points: Int,
    var resonantBranch: String?,
    var resonantType: String,
    val shortCuts: Map<Int, String>,
    val autoDischarges: Set<String>
) {
    fun toCareer(): Career {
        val usableClasses = mutableMapOf<Class, MutableList<Branch>>()
        val usableBranches = mutableMapOf<Branch, MutableMap<Spell, Int>>()
        val usableSpells = mutableMapOf<Spell, Int>()
        classes.forEach { (key, value) ->
            usableClasses[getClass(key)!!] = value.map { getBranch(it)!! }.toMutableList()
        }
        branches.forEach { (key, value) ->
            usableBranches[getBranch(key)!!] = value.mapKeys { getSpell(it.key)!! }.toMutableMap()
        }
        spells.forEach { (key, value) ->
            usableSpells[getSpell(key)!!] = value
        }
        return Career(
            usableClasses,
            usableBranches,
            usableSpells,
            points,
            getBranch(resonantBranch),
            Resonate.ResonateType.valueOf(resonantType),
            shortCuts.toMutableMap(),
            autoDischarges.toMutableSet()
        )
    }
}
