package world.icebear03.starlight

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.expansion.setupPlayerDatabase
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.spell.DischargeHandler
import world.icebear03.starlight.career.spell.entry.architect.*
import world.icebear03.starlight.career.spell.entry.cook.BrewerPassive
import world.icebear03.starlight.career.spell.passive.limit.MaterialLimitLibrary
import world.icebear03.starlight.other.*
import world.icebear03.starlight.station.core.StationLoader
import world.icebear03.starlight.station.mechanism.StaminaEffector
import world.icebear03.starlight.station.mechanism.StaminaModifier
import world.icebear03.starlight.station.mechanism.StationCraft
import world.icebear03.starlight.station.mechanism.StationMechanism
import java.io.File

object StarLightCore : Plugin() {

    override fun onEnable() {
        ClassLoader.initialize()
        AutoIO.initialize()

        setupPlayerDatabase(File(getDataFolder(), "data.db"))
        onlinePlayers.forEach {
            it.loadStarLightData()
        }

        Resonate.initialize()
        StationLoader.initialize()
        StationMechanism.initialize()
        StaminaModifier.initialize()
        StaminaEffector.initialize()
        StationCraft.initialize()

        info("Successfully running StarLightCore!")

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
        WorldBorder.initialize()
        NearestPlayer.initialize()
        RespawnProtection.initialize()
        WorldRule.initialize()
        DarkMare.initialize()
        BossBarCompass.initialize()
        FasterNight.initialize()

        DischargeHandler.initialize()
    }

    override fun onDisable() {
        BossBarCompass.clearBars()
        onlinePlayers.forEach {
            it.sendMessage("§b繁星工坊 §7>> 服务器正在重载...")
//            it.kickPlayer("服务器重启")
        }
    }
}