package world.icebear03.starlight.career.internal

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

data class Skill(val id: String, val branch: Branch, private val section: ConfigurationSection) {
    val color: String = section.getString("color")!!
    val skull: String = section.getString("skull")!!
    val levels = mutableListOf<Level>()
    val type = SkillType.valueOf(section.getString("type")!!)

    inner class Level(
        val description: List<String>,
        val cooldown: Int,
        val duration: Int
    )

    init {
        section.getKeys(false).filter { it != "color" && it != "skull" }.forEach {
            levels += Level(
                section.getStringList("$it.description"),
                section.getInt("$it.cooldown", 0),
                section.getInt("$it.duration", -1)
            )
        }

        skills[id] = this
    }

    override fun toString(): String {
        return id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
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
