package world.icebear03.starlight.career.core.branch

import world.icebear03.starlight.career.core.Basic
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.spell.Spell

data class Branch(
    override val name: String,
    override val skull: String,
    override val color: String,
    val clazz: Class,
    val spells: MutableMap<String, Spell>,
    val description: List<String>
) : Basic() {

    val skills = mutableMapOf<String, Spell>()
    val eurekas = mutableMapOf<String, Spell>()

    init {
        spells.forEach { (name, spell) ->
            if (spell.isEureka)
                eurekas[name] = spell
            else
                skills[name] = spell
        }
    }

    fun initSpellLevelMap(): MutableMap<Spell, Int> {
        return spells.values.associateWith { 0 }.toMutableMap()
    }

    fun spellNames(skillOrEureka: Boolean, displayed: Boolean = false): List<String> {
        val tmp = if (skillOrEureka) skills else eurekas
        return tmp.values.sortedBy { it.name }.map { if (displayed) it.name else it.display() }
    }
}
