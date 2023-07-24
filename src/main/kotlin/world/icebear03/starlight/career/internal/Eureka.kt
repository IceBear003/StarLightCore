package world.icebear03.starlight.career.internal

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

data class Eureka(val id: String, val branch: Branch, private val section: ConfigurationSection) {
    val color: String = section.getString("color")!!
    val skull: String = section.getString("skull")!!

    val description: List<String> = section.getStringList("description")
    val type: SkillType = SkillType.valueOf(section.getString("type")!!)
    val cooldown: Int = section.getInt("cooldown", 0)

    init {
        eurekas[id] = this
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

    companion object {
        val eurekas = mutableMapOf<String, Eureka>()

        fun fromId(id: String?): Eureka? {
            return eurekas[id]
        }
    }
}
