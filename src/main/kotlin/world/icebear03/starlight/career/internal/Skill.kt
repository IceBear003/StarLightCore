package world.icebear03.starlight.career.internal

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

data class Skill(val id: String, val branch: Branch, private val section: ConfigurationSection) {
    val color: String = section.getString("color")!!
    val skull: String = section.getString("skull")!!
    val levels = mutableListOf<Level>()

    inner class Level(
        val description: List<String>,
        val type: SkillType,
        val cooldown: Int
    )

    init {
        section.getKeys(false).filter { it != "color" && it != "skull" }.forEach {
            levels += Level(
                section.getStringList("$it.description"),
                SkillType.valueOf(section.getString("$it.type")!!),
                section.getInt("$it.cooldown", 0)
            )
        }

        skills[id] = this
    }

    override fun toString(): String {
        return id
    }

    fun display(): String {
        return color.colored() + id
    }

    fun level(level: Int): Level {
        return levels[level - 1]
    }

    companion object {
        val skills = mutableMapOf<String, Skill>()

        fun fromId(id: String?): Skill? {
            return skills[id]
        }
    }
}
