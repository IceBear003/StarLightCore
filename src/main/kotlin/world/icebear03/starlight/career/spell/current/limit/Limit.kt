package world.icebear03.starlight.career.spell.current.limit

import world.icebear03.starlight.career.display

data class Limit(
    val limit: Pair<String, Int>
) {
    val name = limit.first
    val level = limit.second

    fun string(): String {
        return display(name, level)
    }
}