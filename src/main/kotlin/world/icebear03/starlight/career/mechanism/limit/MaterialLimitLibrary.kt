package world.icebear03.starlight.career.mechanism.limit

import org.bukkit.Material

object MaterialLimitLibrary {

    //type-分支/技能/顿悟ID with 等级(顿悟不看等级)
    val craftMaps = mutableMapOf<Material, List<Pair<String, Int>>>()
    val placeMaps = mutableMapOf<Material, List<Pair<String, Int>>>()
    val breakMaps = mutableMapOf<Material, List<Pair<String, Int>>>()
    val useMaps = mutableMapOf<Material, List<Pair<String, Int>>>()
    val sinterMaps = mutableMapOf<Material, List<Pair<String, Int>>>()
    val igniteMaps = mutableMapOf<Material, List<Pair<String, Int>>>()

    fun initialize() {
    }

    fun install(limits: List<LimitType>, types: List<Material>, needed: String, level: Int) {
        limits.forEach {

        }
    }
}