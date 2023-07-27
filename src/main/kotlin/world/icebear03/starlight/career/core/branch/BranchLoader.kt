package world.icebear03.starlight.career.core.branch

import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellLoader
import world.icebear03.starlight.utils.YamlUpdater

object BranchLoader {
    val branches = mutableMapOf<String, Branch>()

    fun loadBranch(name: String, clazz: Class): Branch {
        val config = YamlUpdater.loadAndUpdate("career/branch/${clazz.name}/$name.yml")
        val spells = mutableMapOf<String, Spell>()
        val branch = Branch(
            name,
            config.getString("skull")!!,
            config.getString("color")!!,
            clazz,
            spells,
            config.getStringList("description")
        )

        branches[name] = branch

        var section = config.getConfigurationSection("skills")!!
        section.getKeys(false).forEach {
            SpellLoader.loadSpell(it, branch, section.getConfigurationSection(it)!!)
        }
        section = config.getConfigurationSection("eurekas")!!
        section.getKeys(false).forEach {
            SpellLoader.loadSpell(it, branch, section.getConfigurationSection(it)!!)
        }

        return branch
    }

    fun fromName(name: String?): Branch? {
        return branches[name]
    }
}