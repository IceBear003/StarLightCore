package world.icebear03.starlight.career.core.`class`

import world.icebear03.starlight.career.core.Basic
import world.icebear03.starlight.career.core.branch.Branch

data class Class(
    override val name: String,
    override val skull: String,
    override val color: String,
    val branches: MutableMap<String, Branch>
) : Basic() {

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }

    fun branchNames(displayed: Boolean = true): List<String> {
        return branches.values.sortedBy { it.name }.map { if (displayed) it.display() else it.name }
    }
}