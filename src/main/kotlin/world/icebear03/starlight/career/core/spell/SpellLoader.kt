package world.icebear03.starlight.career.core.spell

import taboolib.library.configuration.ConfigurationSection
import world.icebear03.starlight.career.core.branch.Branch

object SpellLoader {
    val spells = mutableMapOf<String, Spell>()

    fun loadSpell(name: String, branch: Branch, section: ConfigurationSection): Spell {
        val descriptions = mutableListOf<List<String>>()
        var isEureka = true
        if (section.contains("description.1")) {
            isEureka = false
            for (i in 1..3) {
                descriptions += section.getStringList("description.$i")
            }
        } else {
            descriptions += section.getStringList("description")
        }

        val spell = Spell(
            name,
            section.getString("skull")!!,
            section.getString("color")!!,
            branch,
            section.getString("cooldown", "-1")!!,
            section.getString("duration", "-1")!!,
            isEureka,
            SpellType.valueOf(section.getString("type")!!),
            descriptions
        )

        spells[name] = spell

        return spell
    }

    fun fromName(name: String?): Spell? {
        return spells[name]
    }
}