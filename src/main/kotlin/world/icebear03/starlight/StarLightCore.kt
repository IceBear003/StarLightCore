package world.icebear03.starlight

import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.expansion.setupPlayerDatabase
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.CareerManager
import world.icebear03.starlight.career.spell.entry.worker.Miner
import world.icebear03.starlight.station.StationManager
import world.icebear03.starlight.tag.TagLibrary
import world.icebear03.starlight.tool.ToolManager
import world.icebear03.starlight.tool.info.BossBarCompass
import world.icebear03.starlight.utils.CooldownFreezer
import java.io.File

object StarLightCore : Plugin() {

    override fun onEnable() {
        CareerManager.initialize()

        AutoIO.initialize()
        setupPlayerDatabase(File(getDataFolder(), "data.db"))
        onlinePlayers.forEach {
            it.loadStarLightData()
        }

        StationManager.initialize()
        ToolManager.initialize()
        CooldownFreezer.initialize()
        TagLibrary.initialize()
        info("Successfully Running StarLightCore!")
    }

    override fun onDisable() {
        BossBarCompass.clearBars()
        onlinePlayers.forEach {
            it.sendMessage("§b繁星工坊 §7>> 服务器正在重载...")
            it.kickPlayer("服务器重启")
        }
        Miner.shulkers.forEach { uid ->
            Bukkit.getEntity(uid)?.remove()
        }
    }
}