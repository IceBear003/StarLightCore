package world.icebear03.starlight.career.mechanism.data

import org.bukkit.entity.Player
import world.icebear03.starlight.career.internal.Branch
import world.icebear03.starlight.loadCareerData

object Forget {

    fun attemptToForget(player: Player, branchId: String): Pair<Boolean, String> {
        return attemptToForget(player, Branch.fromId(branchId) ?: return false to "分支不存在")
    }

    fun attemptToForget(player: Player, branch: Branch): Pair<Boolean, String> {
        val data = loadCareerData(player)
        val level = data.getBranchLevel(branch)
        if (level < 0)
            return false to "该技能分支未解锁，无法遗忘"
        val point = data.points
        if (point < level * 2 + 2) {
            return false to "技能点不足"
        }
        data.takePoint(level * 2 + 2)
        data.classes[branch.careerClass]!!.remove(branch)
        data.branches.remove(branch)
        branch.skills.forEach {
            data.skills.remove(it)
        }
        branch.eurekas.forEach {
            data.eurekas.remove(it)
        }
        if (data.resonantBranch == branch)
            data.resonantBranch = null
        return true to "成功遗忘技能分支 ${branch.display()}"
    }
}