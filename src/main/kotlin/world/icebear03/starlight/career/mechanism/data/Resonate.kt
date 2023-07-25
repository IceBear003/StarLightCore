package world.icebear03.starlight.career.mechanism.data

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.career.internal.ResonateType
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.career.internal.SkillType
import world.icebear03.starlight.loadCareerData
import java.util.*

object Resonate {

    val resonating = mutableMapOf<UUID, Map<Skill, Pair<String, Int>>>()

    fun initialize() {
        submit(delay = 20L, period = 100L) {
            onlinePlayers.forEach {
                resonating[it.uniqueId] = generateResonated(it)
            }
        }
    }

    fun generateResonated(player: Player): Map<Skill, Pair<String, Int>> {
        val data = loadCareerData(player)
        val resonatedMap = mutableMapOf<Skill, Pair<String, Int>>()
        player.getNearbyEntities(36.0, 36.0, 36.0).filterIsInstance<Player>().forEach { other ->
            val otherData = loadCareerData(other)
//            if (otherData.resonantType != ResonateType.ALL)
//                return@forEach
            val branch = otherData.resonantBranch ?: return@forEach
            val otherMap = otherData.getSkillsInBranch(branch).toMutableMap()
            val distance = other.location.distance(player.location)
            for ((skill, level) in otherMap) {
                if (skill.type != SkillType.PASSIVE)
                    continue
                val shareLevel = if (otherData.getBranchLevel(branch) == 10) level else level - 1
                if (distance < level * 8) {
                    if (resonatedMap.containsKey(skill)) {
                        resonatedMap[skill] = other.name to maxOf(shareLevel, resonatedMap[skill]!!.second)
                    } else {
                        resonatedMap[skill] = other.name to shareLevel
                    }
                }
            }
        }
        return resonatedMap.filter {
            data.getSkillLevel(it.key) < it.value.second
        }
    }

    fun chooseResonate(player: Player, branchId: String): Pair<Boolean, String> {
        return chooseResonate(player, Branch.fromId(branchId) ?: return false to "分支不存在")
    }

    fun chooseResonate(player: Player, branch: Branch): Pair<Boolean, String> {
        val data = loadCareerData(player)
        if (data.getBranchLevel(branch) == -1) {
            return false to "该职业分支未解锁"
        }
        data.resonantBranch = branch
        return true to "已经选择该职业分支作为共鸣分支"
    }

    fun clearResonate(player: Player) {
        val data = loadCareerData(player)
        data.resonantBranch = null
    }

    fun setResonateType(player: Player, type: ResonateType) {
        val data = loadCareerData(player)
        data.resonantType = type
    }

    fun getSkillResonatedLevel(player: Player, skillId: String): Int {
        return getSkillResonatedLevel(player, Skill.fromId(skillId) ?: return -1)
    }

    fun getSkillResonatedLevel(player: Player, skill: Skill): Int {
        val map = resonating[player.uniqueId] ?: return -1
        return (map[skill] ?: return -1).second
    }
}