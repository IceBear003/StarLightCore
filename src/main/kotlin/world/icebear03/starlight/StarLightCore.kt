package world.icebear03.starlight

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.expansion.setupPlayerDatabase
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.internal.Class
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.career.mechanism.entry.architect.*
import world.icebear03.starlight.career.mechanism.entry.cook.BrewerPassive
import world.icebear03.starlight.career.mechanism.passive.limit.MaterialLimitLibrary
import world.icebear03.starlight.other.CustomTab
import java.io.File

object StarLightCore : Plugin() {

    override fun onEnable() {

        Class.initialize()

//        setupPlayerDatabase(Config.config.getConfigurationSection("database")!!)
        setupPlayerDatabase(File(getDataFolder(), "data.db"))

        info("Successfully running StarLightCore!")

        Resonate.initialize()

        MaterialLimitLibrary.initialize()

        FortressEngineerActive.initialize()
        FortressEngineerPassive.initialize()
        StructuralEngineerActive.initialize()
        StructuralEngineerPassive.initialize()
        DemolitionistActive.initialize()
        DemolitionistPassive.initialize()
        TrafficEngineerActive.initialize()
        TrafficEngineerPassive.initialize()
        BrewerPassive.initialize()

        CustomTab.initialize()
    }

    override fun onDisable() {
        onlinePlayers.forEach { it.kickPlayer("核心插件重载") }
    }
}