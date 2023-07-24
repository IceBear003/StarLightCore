package world.icebear03.starlight.career

import org.bukkit.entity.Player
import world.icebear03.starlight.career.internal.*
import world.icebear03.starlight.loadCareerData

data class UsableCareer(
    val classes: MutableMap<Class, MutableList<Branch>> = mutableMapOf(),
    val branches: MutableMap<Branch, Pair<MutableMap<Skill, Int>, Eureka?>> = mutableMapOf(),
    val skills: MutableMap<Skill, Int> = mutableMapOf(),
    val eurekas: MutableList<Eureka> = mutableListOf(),
    var points: Int = 0,
    var resonantBranch: Branch? = null,
    var resonantType: ResonateType = ResonateType.FRIENDLY
) {
    fun toSavableCareer(): SavableCareer {
        val savableClasses = mutableMapOf<String, List<String>>()
        val savableBranches = mutableMapOf<String, Pair<Map<String, Int>, String?>>()
        val savableSkills = mutableMapOf<String, Int>()
        classes.forEach { (key, value) ->
            savableClasses[key.id] = value.map { it.id }
        }
        branches.forEach { (key, value) ->
            val savableSkillMap = mutableMapOf<String, Int>()
            value.first.forEach { (skill, level) ->
                savableSkillMap[skill.id] = level
            }
            val savableEureka = value.second?.id
            val pair = savableSkillMap to savableEureka
            savableBranches[key.id] = pair
        }
        skills.forEach { (key, value) ->
            savableSkills[key.id] = value
        }
        return SavableCareer(
            savableClasses,
            savableBranches,
            savableSkills,
            eurekas.map { it.id },
            points,
            resonantBranch?.id,
            resonantType.toString()
        )
    }

    //玩家死亡时
    //新进入服务器时
    //TODO 死亡惩罚
    fun remake(): UsableCareer {
        println("awaaaa")
        classes.clear()
        branches.clear()
        skills.clear()
        eurekas.clear()

        points = 0
        resonantBranch = null

        val talentA = Class.classes.values.random()
//        var talentB = talentA
//        while (talentA == talentB) {
//            talentB = Class.classes.values.random()
//        }
        classes[talentA] = mutableListOf()
//        classes[talentB] = mutableListOf()

        resonantType = ResonateType.FRIENDLY
        return this
    }

    //-----------------------职业部分-------------------------
    fun getClasses(): List<Class> {
        return classes.keys.toList()
    }

    fun hasClass(classId: String): Boolean {
        return hasClass(Class.fromId(classId) ?: return false)
    }

    fun hasClass(careerClass: Class): Boolean {
        return classes.containsKey(careerClass)
    }

    fun canChoose(): Boolean {
        return classes.size < 3
    }

    fun chooseList(): List<Class> {
        val list = Class.classes.values.toMutableList()
        list.removeAll(classes.keys)
        list.remove(list.random())
        return list
    }

    fun addClass(classId: String) {
        addClass(Class.fromId(classId) ?: return)
    }

    fun addClass(careerClass: Class) {
        classes[careerClass] = mutableListOf()
    }
    //--------------------------------------------------------------------

    //---------------------------------分支相关-----------------------------
    fun getBranches(): List<Branch> {
        return branches.keys.toList()
    }

    fun hasBranch(branchId: String): Boolean {
        return hasBranch(Branch.fromId(branchId) ?: return false)
    }

    fun hasBranch(branch: Branch): Boolean {
        return branches.containsKey(branch)
    }

    fun getBranchLevel(branchId: String): Int {
        return getBranchLevel(Branch.fromId(branchId) ?: return -1)
    }

    fun getBranchLevel(branch: Branch): Int {
        var level = 0
        val pair = branches[branch] ?: return -1
        pair.first.forEach {
            level += it.value
        }
        if (pair.second != null)
            level += 1
        return level
    }

    fun attemptToUnlockBranch(branchId: String): Pair<Boolean, String> {
        return attemptToUnlockBranch(Branch.fromId(branchId) ?: return false to "分支不存在")
    }

    fun attemptToUnlockBranch(branch: Branch): Pair<Boolean, String> {
        if (branches.size >= 6)
            return false to "解锁的职业分支数量已达到上限"
        if (branches.containsKey(branch))
            return false to "该职业分支已解锁"
        if (points <= 0)
            return false to "技能点不足"
        val classNeeded = branch.careerClass
        if (!hasClass(classNeeded))
            return false to "该分支对应的职业未解锁"

        takePoint(1)
        classes[classNeeded]!! += branch
        branches[branch] = branch.initSkillMap() to Eureka.fromId("null")
        skills += branch.initSkillMap()
        return true to "成功解锁职业分支 ${branch.display()}"
    }
    //--------------------------------------------------------------------

    //--------------------------------技能相关------------------------------
    fun getSkillLevel(skillId: String): Int {
        return getSkillLevel(Skill.fromId(skillId) ?: return -1)
    }

    fun getSkillLevel(skill: Skill): Int {
        return skills[skill] ?: -1
    }

    fun getSkillsInBranch(branchId: String): Map<Skill, Int> {
        return getSkillsInBranch(Branch.fromId(branchId) ?: return mapOf())
    }

    fun getSkillsInBranch(branch: Branch): Map<Skill, Int> {
        return skills.filter { it.key.branch == branch }
    }

    fun getSkillsInTheSameBranch(skillId: String): Map<Skill, Int> {
        return getSkillsInTheSameBranch(Skill.fromId(skillId) ?: return mapOf())
    }

    fun getSkillsInTheSameBranch(skill: Skill): Map<Skill, Int> {
        return skills.filter { it.key.branch == skill.branch }
    }

    fun attemptToUpgradeSkill(skillId: String): Pair<Boolean, String> {
        return attemptToUpgradeSkill(Skill.fromId(skillId) ?: return false to "技能不存在")
    }

    fun attemptToUpgradeSkill(skill: Skill): Pair<Boolean, String> {
        val level = getSkillLevel(skill)
        val branch = skill.branch
        if (level >= 3)
            return false to "该技能等级已达到上限"
        if (getBranchLevel(branch) < 0)
            return false to "该技能对应的职业分支未解锁"
        if (points <= 0)
            return false to "技能点不足"

        points -= 1
        branches[branch]!!.first[skill] = level + 1
        skills[skill] = level + 1
        return true to "成功升级技能 ${skill.display()}"
    }
    //--------------------------------------------------------------------

    //-----------------------------顿悟相关---------------------------------
    fun hasEureka(eurekaId: String): Boolean {
        return hasEureka(Eureka.fromId(eurekaId) ?: return false)
    }

    fun hasEureka(eureka: Eureka): Boolean {
        return eurekas.contains(eureka)
    }


    fun attemptToEureka(eurekaId: String): Pair<Boolean, String> {
        return attemptToEureka(Eureka.fromId(eurekaId) ?: return false to "顿悟不存在")
    }

    fun attemptToEureka(eureka: Eureka): Pair<Boolean, String> {
        val branch = eureka.branch
        if (getBranchLevel(branch) < 9) {
            return false to "该职业分支的三个技能未满级"
        }
        if (points <= 0)
            return false to "技能点不足"
        if (branches[branch]!!.second != null)
            return false to "该职业分支已经激活顿悟"

        points -= 1
        val pair = branches[branch]!!
        val newPair = pair.first to eureka
        branches[branch] = newPair
        eurekas += eureka
        return true to "成功激活顿悟 ${eureka.display()}"
    }
    //--------------------------------------------------------------------

    //-----------------------------技能点相关-------------------------------
    fun takePoint(amount: Int) {
        points -= amount
    }

    fun addPoint(amount: Int) {
        points += amount
    }

    fun setPoint(amount: Int) {
        points = amount
    }
    //--------------------------------------------------------------------
}

fun Player.hasBranch(branchId: String, level: Int = 0): Boolean {
    return loadCareerData(this).getBranchLevel(branchId) >= level
}

fun Player.hasSkill(skillId: String, level: Int = 0): Boolean {
    return loadCareerData(this).getSkillLevel(skillId) >= level
}

fun Player.hasEureka(eurekaId: String): Boolean {
    return loadCareerData(this).hasEureka(eurekaId)
}

fun Player.getBranchLevel(branchId: String): Int {
    return loadCareerData(this).getBranchLevel(branchId)
}

fun Player.getSkillLevel(skillId: String): Int {
    return loadCareerData(this).getSkillLevel(skillId)
}