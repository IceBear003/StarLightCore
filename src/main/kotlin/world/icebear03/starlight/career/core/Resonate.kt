package world.icebear03.starlight.career.core

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellType
import world.icebear03.starlight.career.getBranch
import world.icebear03.starlight.career.getSpell
import java.util.*

object Resonate {

    val resonateMap = mutableMapOf<UUID, Map<Spell, Pair<String, Int>>>()

    fun initialize() {
        submit(delay = 20L, period = 100L) {
            onlinePlayers.forEach {
                resonateMap[it.uniqueId] = searchResonate(it)
            }
        }
    }

    fun searchResonate(player: Player): Map<Spell, Pair<String, Int>> {
        val career = player.career()
        val resonate = mutableMapOf<Spell, Pair<String, Int>>()
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
                if (distance <= 8 * level) {
                    resonate[spell] = other.name to maxOf(levelShared, resonate[spell]?.second ?: 0)
                }
            }
        }
        return resonate.filter { (spell, pair) -> career.getSpellLevel(spell) < pair.second }
    }

    fun resonate(player: Player, name: String?): Pair<Boolean, String> {
        return resonate(player, getBranch(name))
    }

    fun resonate(player: Player, branch: Branch?): Pair<Boolean, String> {
        branch ?: return false to "§e职业分支§7不存在"
        val data = player.career()
        if (data.getBranchLevel(branch) == -1) {
            return false to "该§e职业分支§7未解锁"
        }
        data.resonantBranch = branch
        return true to "已经选择该§e职业分支§7作为§e共鸣分支"
    }

    fun setResonateType(player: Player, type: ResonateType) {
        val data = player.career()
        data.resonantType = type
    }

    fun getSpellResonatedLevel(player: Player, name: String?): Int {
        return getSpellResonatedLevel(player, getSpell(name))
    }

    fun getSpellResonatedLevel(player: Player, spell: Spell?): Int {
        spell ?: return -1
        val map = resonateMap[player.uniqueId] ?: return -1
        return (map[spell] ?: return -1).second
    }

    enum class ResonateType(val displayName: String) {
        ALL("所有"),
        FRIENDLY("非敌对"),
        INTERNAL("同盟"),
        PRIVATE("孤独")
    }
}