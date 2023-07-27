package world.icebear03.starlight.career.core.`class`

import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.career.core.branch.BranchLoader
import world.icebear03.starlight.utils.YamlUpdater

object ClassLoader {
    val classes = mutableMapOf<String, Class>()

    fun initialize() {
        val config = YamlUpdater.loadAndUpdate("career/classes.yml")
        config.getKeys(false).forEach { name ->
            
            val branches = mutableMapOf<String, Branch>()
            val clazz = Class(
                name,
                config.getString("$name.skull")!!,
                config.getString("$name.color")!!,
                branches
            )

            config.getStringList("$name.branches").forEach { branchName ->
                branches[branchName] = BranchLoader.loadBranch(branchName, clazz)
            }

            classes[name] = clazz
        }
    }

    fun fromName(name: String?): Class? {
        return classes[name]
    }
}