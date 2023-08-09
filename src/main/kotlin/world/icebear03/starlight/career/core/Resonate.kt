package world.icebear03.starlight.career.core

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellType
import world.icebear03.starlight.career.getBranch
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.career.spell.entry.scholar.Teacher
import world.icebear03.starlight.tag.PlayerTag
import java.util.*

object Resonate {

    val resonateSpellMap = mutableMapOf<UUID, Map<Spell, Pair<String, Int>>>()
    val resonateBranchMap = mutableMapOf<UUID, List<Branch>>()

    fun initialize() {
        submit(delay = 20L, period = 20L) {
            onlinePlayers.forEach {
                val result = searchResonate(it)
                resonateSpellMap[it.uniqueId] = result.first
                resonateBranchMap[it.uniqueId] = result.second

                if (result.second.size >= 3) {
                    if (!PlayerTag.addTag(it, "余音绕梁")) {
                        it.sendMessage("§b繁星工坊 §7>> 这么多人的共鸣，结合起来也是一番华美的乐章呢……(获得称号${"&{#ae7cff}余音绕梁".colored()}§7)")
                    }
                }
            }
        }
    }

    fun searchResonate(player: Player): Pair<Map<Spell, Pair<String, Int>>, List<Branch>> {
        val career = player.career()
        val spellResonate = mutableMapOf<Spell, Pair<String, Int>>()
        val branchResonate = mutableSetOf<Branch>()
        player.world.players.forEach { other ->
            if (other.uniqueId == player.uniqueId)
                return@forEach
//            if (otherData.resonantType != ResonateType.ALL)
//                return@forEach
            val otherCareer = other.career()
            val branch = otherCareer.resonantBranch ?: return@forEach
            val distance = other.location.distance(player.location)

            otherCareer.spells.filterKeys { it.branch == branch }.forEach spells@{ (spell, level) ->
                if (spell.type != SpellType.PASSIVE || spell.isEureka)
                    return@spells
                val levelShared = if (otherCareer.getBranchLevel(branch) == 10) level else level - 1

                var range = if (otherCareer.getBranchLevel(branch) == 10) 32 else 8 * level
                if (other.meetRequirement("触类旁通")) {
                    range += 3 * otherCareer.branches.size
                }
                Teacher.saloning.forEach teachers@{ (uuid, extraRange) ->
                    val teacher = Bukkit.getPlayer(uuid) ?: return@teachers
                    if (!teacher.isOnline) return@teachers
                    if (teacher.world.name != other.world.name) return@teachers
                    if (teacher.location.distance(other.location) <= 4.0)
                        range += extraRange
                }

                if (distance <= range) {
                    branchResonate += branch
                    spellResonate[spell] = other.name to maxOf(levelShared, spellResonate[spell]?.second ?: 0)
                }
            }
        }
        return spellResonate.filter { (spell, pair) -> career.getSpellLevel(spell) < pair.second } to
                branchResonate.filter { branch -> !career.hasBranch(branch) }

    }

    fun resonate(player: Player, name: String?): Pair<Boolean, String> {
        return resonate(player, getBranch(name))
    }

    fun resonate(player: Player, branch: Branch?): Pair<Boolean, String> {
        branch ?: return false to "§e职业分支§7不存在"
        val career = player.career()
        if (career.getBranchLevel(branch) == -1) {
            return false to "该§e职业分支§7未解锁"
        }
        career.resonantBranch = branch
        return true to "已经选择该§e职业分支§7作为§e共鸣分支"
    }

    fun getSpellResonatedLevel(player: Player, name: String?): Int {
        return getSpellResonatedLevel(player, getSpell(name))
    }

    fun getSpellResonatedLevel(player: Player, spell: Spell?): Int {
        spell ?: return -1
        val map = resonateSpellMap[player.uniqueId] ?: return -1
        return (map[spell] ?: return -1).second
    }

    fun getBranchResonatedLevel(player: Player, name: String?): Int {
        return getBranchResonatedLevel(player, getBranch(name))
    }

    fun getBranchResonatedLevel(player: Player, branch: Branch?): Int {
        branch ?: return -1
        val list = resonateBranchMap[player.uniqueId] ?: return -1
        return if (list.contains(branch)) 0 else -1
    }


    enum class ResonateType(val displayName: String) {
        ALL("所有"),
        FRIENDLY("非敌对"),
        INTERNAL("同盟"),
        PRIVATE("孤独")
    }
}