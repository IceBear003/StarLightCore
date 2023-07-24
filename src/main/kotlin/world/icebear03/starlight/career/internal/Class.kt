package world.icebear03.starlight.career.internal

import taboolib.module.chat.colored
import world.icebear03.starlight.utils.YamlUpdater

data class Class(
    val id: String,
    val color: String,
    val skull: String,
    val branches: List<Branch>
) {
    override fun toString(): String {
        return id
    }

    fun display(): String {
        return color.colored() + id
    }

    fun branchIds(hasColor: Boolean = true): List<String> {
        return branches.toList().sortedBy { it.id }.map { (if (hasColor) it.color.colored() else "") + it.id }
    }

    companion object {

        val classes = mutableMapOf<String, Class>()

        fun initialize() {
            val config = YamlUpdater.loadAndUpdate("career/classes.yml")
            config.getKeys(false).forEach { careerId ->
                val section = config.getConfigurationSection(careerId)!!
                val branches = mutableListOf<Branch>()
                val careerClass = Class(
                    careerId,
                    section.getString("color")!!,
                    section.getString("skull")!!,
                    branches
                )
                section.getStringList("branches").forEach { branchId ->
                    branches += Branch(careerClass, branchId)
                }
                classes[careerId] = careerClass
            }
        }

        fun fromId(id: String?): Class? {
            return classes[id]
        }
    }
}