package world.icebear03.starlight.career

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import world.icebear03.starlight.career
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.branch.BranchLoader
import world.icebear03.starlight.career.core.`class`.Class
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.core.spell.Spell
import world.icebear03.starlight.career.core.spell.SpellLoader
import world.icebear03.starlight.career.spell.discharge.DischargeHandler
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.career.spell.handler.internal.Limit

fun getClass(name: String?): Class? {
    return ClassLoader.fromName(name)
}

fun getBranch(name: String?): Branch? {
    return BranchLoader.fromName(name)
}

fun getSpell(name: String?): Spell? {
    return SpellLoader.fromName(name)
}

fun display(name: String, level: Int? = null): String {
    val basic = getClass(name) ?: getBranch(name) ?: getSpell(name) ?: return name
    return basic.display(level)
}

//包含共鸣，技能必须调用这个！
fun Player.spellLevel(name: String?, includeResonate: Boolean = true): Int {
    return maxOf(
        career().getSpellLevel(name),
        if (includeResonate) Resonate.getSpellResonatedLevel(this, name) else -1
    )
}

fun Player.branchLevel(name: String?): Int {
    return career().getBranchLevel(name)
}

fun Player.forget(name: String): Pair<Boolean, String> {
    return career().forget(name)
}

fun Player.meetRequirement(name: String?, level: Int = 1, includeResonate: Boolean = true): Boolean {
    return spellLevel(name, includeResonate) >= level ||
            branchLevel(name) >= level
}

fun Player.meetRequirement(limit: Limit, includeResonate: Boolean = true): Boolean {
    return meetRequirement(limit.name, limit.level, includeResonate)
}


fun Player.meetRequirements(limits: List<Pair<String, Int>>?): Boolean {
    if (limits == null)
        return true
    if (limits.isEmpty())
        return true
    var result = false
    val list = mutableListOf<String>()
    limits.forEach { (name, level) ->
        if (this.meetRequirement(name, level))
            result = true
        else {
            list += display(name, level)
        }
    }
    return result
}

fun addLimit(handlerType: HandlerType, limit: Pair<String, Int>, vararg types: Material) {
    EventHandler.addLimit(handlerType, limit, *types)
}

fun String.discharge(function: Player.(name: String, level: Int) -> String?): String {
    DischargeHandler.dischargeMap[this] = function
    return this
}

fun String.finish(function: Player.(name: String, level: Int) -> String?) {
    DischargeHandler.finishMap[this] = function
}

fun Material.addLowestListener(
    handlerType: HandlerType,
    function: (event: Event, player: Player, type: Material) -> Pair<Boolean, String?>
) {
    listOf(this).addLowestListener(handlerType, function)
}

fun Material.addHighListener(
    handlerType: HandlerType,
    function: (event: Event, player: Player, type: Material) -> String?
) {
    listOf(this).addHighListener(handlerType, function)
}

fun List<Material>.addLowestListener(
    handlerType: HandlerType,
    function: (event: Event, player: Player, type: Material) -> Pair<Boolean, String?>
) {
    EventHandler.addLowestListener(handlerType, function, *this.toTypedArray())
}

fun List<Material>.addHighListener(
    handlerType: HandlerType,
    function: (event: Event, player: Player, type: Material) -> String?
) {
    EventHandler.addHighListener(handlerType, function, *this.toTypedArray())
}

fun Player.finish(name: String) {
    DischargeHandler.finish(this, name)
}