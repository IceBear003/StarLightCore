package world.icebear03.starlight.career.core.`class`

import world.icebear03.starlight.career.core.Basic
import world.icebear03.starlight.career.core.branch.Branch

data class Class(
    override val name: String,
    override val skull: String,
    override val color: String,
    val branches: MutableMap<String, Branch>
) : Basic() {

    fun branchNames(displayed: Boolean = true): List<String> {
        return branches.values.sortedBy { it.name }.map { if (displayed) it.display() else it.name }
    }
}