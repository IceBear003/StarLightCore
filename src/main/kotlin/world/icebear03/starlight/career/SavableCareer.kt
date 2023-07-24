package world.icebear03.starlight.career

import world.icebear03.starlight.career.internal.*

data class SavableCareer(
    val classes: Map<String, List<String>>,
    val branches: Map<String, Pair<Map<String, Int>, String?>>,
    val skills: Map<String, Int>,
    val eurekas: List<String>,
    var points: Int,
    var resonantBranch: String?,
    var resonantType: String
) {
    fun toUsableCareer(): UsableCareer {
        val usableClasses = mutableMapOf<Class, MutableList<Branch>>()
        val usableBranches = mutableMapOf<Branch, Pair<MutableMap<Skill, Int>, Eureka?>>()
        val usableSkills = mutableMapOf<Skill, Int>()
        val usableEurekas = mutableListOf<Eureka>()
        classes.forEach { (key, value) ->
            val careerClass = Class.fromId(key) ?: return@forEach
            usableClasses[careerClass] = value.map { Branch.fromId(it)!! }.toMutableList()
        }
        for ((key, value) in branches) {
            val branch = Branch.fromId(key) ?: continue
            val skillMap = mutableMapOf<Skill, Int>()
            value.first.forEach { (id, level) ->
                val skill = Skill.fromId(id) ?: return@forEach
                skillMap[skill] = level
            }
            val eureka = Eureka.fromId(value.second)
            usableBranches[branch] = skillMap to eureka
        }
        skills.forEach { (key, value) ->
            val skill = Skill.fromId(key) ?: return@forEach
            usableSkills[skill] = value
        }
        return UsableCareer(
            usableClasses,
            usableBranches,
            usableSkills,
            eurekas.map { Eureka.fromId(it)!! }.toMutableList(),
            points,
            Branch.fromId(resonantBranch),
            ResonateType.valueOf(resonantType)
        )
    }
}
