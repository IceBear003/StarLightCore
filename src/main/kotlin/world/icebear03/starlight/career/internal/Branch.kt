package world.icebear03.starlight.career.internal

import taboolib.module.chat.colored
import world.icebear03.starlight.utils.YamlUpdater

data class Branch(val careerClass: Class, val id: String) {

    val color: String
    val skull: String
    val description: List<String>

    val skills = mutableListOf<Skill>()
    val eurekas = mutableListOf<Eureka>()

    init {
        val config = YamlUpdater.loadAndUpdate("career/branch/${careerClass.id}/$id.yml")
        color = config.getString("color")!!
        skull = config.getString("skull")!!
        description = config.getStringList("description")

        var section = config.getConfigurationSection("skills")!!
        section.getKeys(false).forEach {
            skills += Skill(it, this, section.getConfigurationSection(it)!!)
        }

        section = config.getConfigurationSection("eurekas")!!
        section.getKeys(false).forEach {
            eurekas += Eureka(it, this, section.getConfigurationSection(it)!!)
        }

        branches[id] = this
    }

    override fun toString(): String {
        return id
    }

    fun display(): String {
        return color.colored() + id
    }

    fun initSkillMap(): MutableMap<Skill, Int> {
        return skills.associateWith { 0 }.toMutableMap()
    }

    fun skillIds(hasColor: Boolean = true): List<String> {
        return skills.toList().sortedBy { it.id }.map {
            (if (hasColor) it.color.colored() else "") + it.id
        }
    }

    fun eurekaIds(hasColor: Boolean = true): List<String> {
        return eurekas.toList().sortedBy { it.id }.map {
            (if (hasColor) it.color.colored() else "") + it.id
        }
    }

    companion object {
        val branches = mutableMapOf<String, Branch>()

        fun fromId(id: String?): Branch? {
            return branches[id]
        }
    }
}
