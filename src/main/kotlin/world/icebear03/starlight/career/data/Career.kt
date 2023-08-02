package world.icebear03.starlight.career.data

import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellType
import world.icebear03.starlight.career.getBranch
import world.icebear03.starlight.career.getClass
import world.icebear03.starlight.career.getSpell

data class Career(
    val classes: MutableMap<Class, MutableList<Branch>> = mutableMapOf(),
    val branches: MutableMap<Branch, MutableMap<Spell, Int>> = mutableMapOf(),
    val spells: MutableMap<Spell, Int> = mutableMapOf(),
    var points: Int = 0,
    var resonantBranch: Branch? = null,
    var resonantType: Resonate.ResonateType = Resonate.ResonateType.FRIENDLY,
    val shortCuts: MutableMap<Int, String> = mutableMapOf(),
    val autoDischarges: MutableSet<String> = mutableSetOf()
) {
    fun toSavable(): Savable {
        val savableClasses = mutableMapOf<String, List<String>>()
        val savableBranches = mutableMapOf<String, Map<String, Int>>()
        val savableSpells = mutableMapOf<String, Int>()
        classes.forEach { (key, value) ->
            savableClasses[key.name] = value.map { it.name }
        }
        branches.forEach { (key, value) ->
            savableBranches[key.name] = value.mapKeys { it.key.name }
        }
        spells.forEach { (key, value) ->
            savableSpells[key.name] = value
        }
        return Savable(
            savableClasses,
            savableBranches,
            savableSpells,
            points,
            resonantBranch?.name,
            resonantType.toString(),
            shortCuts,
            autoDischarges
        )
    }

    fun remake(): Career {
        classes.clear()
        branches.clear()
        spells.clear()
        shortCuts.clear()
        autoDischarges.clear()
        points = 0
        resonantBranch = null
        resonantType = Resonate.ResonateType.FRIENDLY

        val talentA = ClassLoader.classes.values.random()
        var talentB = talentA
        while (talentA == talentB) {
            talentB = ClassLoader.classes.values.random()
        }
        classes[talentA] = mutableListOf()
        classes[talentB] = mutableListOf()

        return this
    }

    //---------------------------------职业相关-----------------------------
    fun getClasses(): List<Class> {
        return classes.keys.toList()
    }

    fun hasClass(name: String?): Boolean {
        return hasClass(getClass(name))
    }

    fun hasClass(clazz: Class?): Boolean {
        return classes.containsKey(clazz ?: return false)
    }

    fun canChoose(): Boolean {
        return classes.size < 3
    }

    fun chooseList(): List<Class> {
        val list = ClassLoader.classes.values.toMutableList()
        list.removeAll(classes.keys)
        list.remove(list.random())
        return list
    }

    fun addClass(name: String) {
        addClass(getClass(name) ?: return)
    }

    fun addClass(clazz: Class) {
        classes[clazz] = mutableListOf()
    }
    //--------------------------------------------------------------------

    //---------------------------------分支相关-----------------------------
    fun getBranches(): List<Branch> {
        return branches.keys.toList()
    }

    fun hasBranch(name: String?): Boolean {
        return hasBranch(getBranch(name))
    }

    fun hasBranch(branch: Branch?): Boolean {
        return branches.containsKey(branch ?: return false)
    }

    fun hasOtherBranchesInClass(branch: Branch): Boolean {
        branch.clazz.branches.forEach { (_, br) ->
            if (branches.containsKey(br) && br != branch)
                return true
        }
        return false
    }

    fun getBranchLevel(name: String?): Int {
        return getBranchLevel(getBranch(name))
    }

    fun getBranchLevel(branch: Branch?): Int {
        branch ?: return -1
        if (!hasBranch(branch))
            return -1
        var total = 0
        branches[branch]!!.forEach { (_, level) ->
            total += level
        }
        return total
    }

    fun unlockBranch(name: String): Pair<Boolean, String> {
        return unlockBranch(getBranch(name))
    }

    fun unlockBranch(branch: Branch?): Pair<Boolean, String> {
        branch ?: return false to "§e分支§7不存在"
        if (branches.size >= 6)
            return false to "解锁的§e职业分支§7数量已达到上限"
        if (branches.containsKey(branch))
            return false to "已解锁该§e职业分支"
        if (points <= 0)
            return false to "技能点不足"
        val clazz = branch.clazz
        if (!hasClass(clazz))
            return false to "该§e分支§7对应的职业未解锁"

        takePoint(1)
        classes[clazz]!! += branch
        branches[branch] = branch.initSpellLevelMap()
        spells += branch.initSpellLevelMap()
        return true to "成功解锁§e职业分支§7 ${branch.display()}"
    }
    //--------------------------------------------------------------------

    //--------------------------------SPELL相关----------------------------
    fun getSpellLevel(name: String?): Int {
        return this.getSpellLevel(getSpell(name))
    }

    fun getSpellLevel(spell: Spell?): Int {
        return spells[spell ?: return -1] ?: -1
    }

    fun upgradeSpell(name: String): Pair<Boolean, String> {
        return upgradeSpell(getSpell(name))
    }

    val specialEurekas = listOf("主世界的建造者", "以厨为师", "鞠躬尽瘁")

    fun upgradeSpell(spell: Spell?): Pair<Boolean, String> {
        spell ?: return false to "§a技能§7/§d顿悟§7不存在"
        val level = getSpellLevel(spell)
        val branch = spell.branch
        val branchLevel = getBranchLevel(branch)

        if (branchLevel < 0)
            return false to "${spell.prefix()}§7对应的§e职业分支§7未解锁"
        if (points <= 0)
            return false to "技能点不足"

        if (spell.isEureka) {
            if (branchLevel <= 8)
                return false to "需要§e职业分支§7的三个§a技能§7达到满级"
            if (branchLevel == 10)
                return false to "该§e职业分支§7已激活§d顿悟"

            takePoint(1)
            branches[branch]!![spell] = 1
            spells[spell] = 1

            if (specialEurekas.contains(spell.name) && hasOtherBranchesInClass(branch)) {
                points += 3
                return true to "成功激活§d顿悟§7 ${spell.display()} §7额外获得了 §a3技能点"
            }

            return true to "成功激活§d顿悟§7 ${spell.display()}"
        }

        if (level >= 3)
            return false to "§a技能§7等级已达到上限"

        points -= 1
        branches[branch]!![spell] = level + 1
        spells[spell] = level + 1

        return true to "成功升级§a技能§7 ${spell.display()}"
    }
    //--------------------------------------------------------------------

    //-----------------------------技能点相关-------------------------------
    fun takePoint(amount: Int) {
        points = maxOf(0, points - amount)
    }

    fun addPoint(amount: Int) {
        points += amount
    }

    fun setPoint(amount: Int) {
        points = maxOf(0, amount)
    }
    //--------------------------------------------------------------------

    //-----------------------------快捷键相关-------------------------------
    fun addShortCut(name: String, key: Int): Pair<Boolean, String> {
        return addShortCut(getSpell(name), key)
    }

    fun addShortCut(spell: Spell?, key: Int): Pair<Boolean, String> {
        spell ?: return false to "§a技能§7/§d顿悟§7不存在"
        if (spell.type == SpellType.PASSIVE)
            return false to "被动${spell.prefix()}§7无法绑定至键盘"
        if (getSpellLevel(spell) < 1)
            return false to "请先${spell.prefix(true)}"

        shortCuts[key] = spell.name
        return true to "成功绑定${spell.prefix()} ${spell.display()} §7至键盘§e按键$key"
    }
    //--------------------------------------------------------------------

    //-----------------------------自动释放相关------------------------------
    fun switchAutoDischarge(name: String): Pair<Boolean, String> {
        return switchAutoDischarge(getSpell(name))
    }

    fun switchAutoDischarge(spell: Spell?): Pair<Boolean, String> {
        spell ?: return false to "§a技能§7/§d顿悟§7不存在"
        if (spell.type == SpellType.PASSIVE)
            return false to "被动${spell.prefix()}§7无法设置为自动释放"
        if (getSpellLevel(spell) < 1)
            return false to "请先${spell.prefix(true)}"
        if (spell.isEureka)
            return false to "§d顿悟§7不可以设置为自动释放"

        return if (autoDischarges.contains(spell.name)) {
            autoDischarges.remove(spell.name)
            true to "${spell.prefix()} ${spell.display()} §7被设置为不再自动释放"
        } else {
            autoDischarges.add(spell.name)
            true to "${spell.prefix()} ${spell.display()} §7被设置为自动释放"
        }
    }
    //--------------------------------------------------------------------

    //-------------------------------遗忘相关-------------------------------
    fun forget(name: String?): Pair<Boolean, String> {
        return forget(getBranch(name))
    }

    fun forget(branch: Branch?): Pair<Boolean, String> {
        branch ?: return false to "§e职业分支§7不存在"
        val level = getBranchLevel(branch)
        if (level < 0)
            return false to "该§e职业分支§7未解锁"
        if (points < level * 2 + 2) {
            return false to "技能点不足"
        }
        takePoint(level * 2 + 2)
        classes[branch.clazz]!!.remove(branch)
        branches.remove(branch)
        branch.spells.forEach { (_, spell) ->
            spells.remove(spell)
            shortCuts.filterValues { spell.name == it }.forEach { (level, _) ->
                shortCuts.remove(level)
            }
        }
        if (resonantBranch == branch)
            resonantBranch = null
        return true to "成功遗忘§e职业分支§7 ${branch.display()}"
    }
    //--------------------------------------------------------------------
}