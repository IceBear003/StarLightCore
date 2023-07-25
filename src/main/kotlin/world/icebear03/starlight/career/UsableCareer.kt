package world.icebear03.starlight.career

import org.bukkit.entity.Player
import world.icebear03.starlight.career.internal.*
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.loadCareerData

data class UsableCareer(
    val classes: MutableMap<Class, MutableList<Branch>> = mutableMapOf(),
    val branches: MutableMap<Branch, Pair<MutableMap<Skill, Int>, Eureka?>> = mutableMapOf(),
    val skills: MutableMap<Skill, Int> = mutableMapOf(),
    val eurekas: MutableList<Eureka> = mutableListOf(),
    var points: Int = 0,
    var resonantBranch: Branch? = null,
    var resonantType: ResonateType = ResonateType.FRIENDLY,
    val shortCuts: MutableMap<Int, String> = mutableMapOf()
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
            resonantType.toString(),
            shortCuts
        )
    }

    //玩家死亡时
    //新进入服务器时
    //TODO 死亡惩罚
    fun remake(): UsableCareer {
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
        return attemptToUnlockBranch(Branch.fromId(branchId) ?: return false to "§e分支§7不存在")
    }

    fun attemptToUnlockBranch(branch: Branch): Pair<Boolean, String> {
        if (branches.size >= 6)
            return false to "解锁的§e职业分支§7数量已达到上限"
        if (branches.containsKey(branch))
            return false to "已解锁该§e职业分支"
        if (points <= 0)
            return false to "技能点不足"
        val classNeeded = branch.careerClass
        if (!hasClass(classNeeded))
            return false to "该§e分支§7对应的职业未解锁"

        takePoint(1)
        classes[classNeeded]!! += branch
        branches[branch] = branch.initSkillMap() to Eureka.fromId("null")
        skills += branch.initSkillMap()
        return true to "成功解锁§e职业分支§7 ${branch.display()}"
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
        return attemptToUpgradeSkill(Skill.fromId(skillId) ?: return false to "§a技能§7不存在")
    }

    fun attemptToUpgradeSkill(skill: Skill): Pair<Boolean, String> {
        val level = getSkillLevel(skill)
        val branch = skill.branch
        if (level >= 3)
            return false to "§a技能§7等级已达到上限"
        if (getBranchLevel(branch) < 0)
            return false to "§a技能§7对应的§e职业分支§7未解锁"
        if (points <= 0)
            return false to "技能点不足"

        points -= 1
        branches[branch]!!.first[skill] = level + 1
        skills[skill] = level + 1
        return true to "成功升级§a技能§7 ${skill.display()}"
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
        return attemptToEureka(Eureka.fromId(eurekaId) ?: return false to "§d顿悟§7不存在")
    }

    fun attemptToEureka(eureka: Eureka): Pair<Boolean, String> {
        val branch = eureka.branch
        if (getBranchLevel(branch) < 9) {
            return false to "需要§e职业分支§7的三个§a技能§7达到满级"
        }
        if (points <= 0)
            return false to "技能点不足"
        if (branches[branch]!!.second != null)
            return false to "该§e职业分支§7已激活§d顿悟"

        points -= 1
        val pair = branches[branch]!!
        val newPair = pair.first to eureka
        branches[branch] = newPair
        eurekas += eureka

        //请注意这里是角色技能
        if (eureka.id == "主世界的创造者") {
            var flag = false
            eureka.branch.careerClass.branches.forEach {
                if (hasBranch(it) && it != eureka.branch)
                    flag = true
            }
            if (flag) {
                addPoint(3)
                return true to "成功激活§d顿悟§7 ${eureka.display()} 并获得§e3技能点"
            }
        }

        return true to "成功激活§d顿悟§7 ${eureka.display()}"
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

    //-----------------------------快捷键相关-------------------------------
    fun attemptToAddSkillToShortCut(skillId: String, key: Int): Pair<Boolean, String> {
        return attemptToAddSkillToShortCut(Skill.fromId(skillId) ?: return false to "§a技能§7不存在", key)
    }

    fun attemptToAddSkillToShortCut(skill: Skill, key: Int): Pair<Boolean, String> {
        if (skill.type == SkillType.PASSIVE)
            return false to "§a被动技能§7无法绑定至键盘"
        if (getSkillLevel(skill) < 1)
            return false to "请先升级该§a技能"

        shortCuts[key] = skill.id

        return true to "成功绑定§a技能§7 ${skill.display()} §7至键盘§e按键$key"
    }

    fun attemptToAddEurekaToShortCut(eurekaId: String, key: Int): Pair<Boolean, String> {
        return attemptToAddEurekaToShortCut(Eureka.fromId(eurekaId) ?: return false to "§a技能§7不存在", key)
    }

    fun attemptToAddEurekaToShortCut(eureka: Eureka, key: Int): Pair<Boolean, String> {
        if (eureka.type == SkillType.PASSIVE)
            return false to "§d被动顿悟§7无法绑定至键盘"
        if (!hasEureka(eureka))
            return false to "请先激活该§d顿悟"

        shortCuts[key] = eureka.id

        return true to "成功绑定§d顿悟§7 ${eureka.display()} §7至键盘§e按键$key"
    }
    //--------------------------------------------------------------------
}

fun Player.hasBranch(branchId: String, level: Int = 0): Boolean {
    return loadCareerData(this).getBranchLevel(branchId) >= level
}

fun Player.hasSkill(skillId: String, level: Int = 0): Boolean {
    return loadCareerData(this).getSkillLevel(skillId) >= level ||
            Resonate.getSkillResonatedLevel(this, skillId) >= level
}

fun Player.hasEureka(eurekaId: String): Boolean {
    return loadCareerData(this).hasEureka(eurekaId)
}

fun Player.getBranchLevel(branchId: String): Int {
    return loadCareerData(this).getBranchLevel(branchId)
}

fun Player.getSkillLevel(skillId: String): Int {
    return maxOf(
        loadCareerData(this).getSkillLevel(skillId),
        Resonate.getSkillResonatedLevel(this, skillId)
    )
}