package world.icebear03.starlight.tool

import world.icebear03.starlight.tool.info.*
import world.icebear03.starlight.tool.mechanism.AFK
import world.icebear03.starlight.tool.mechanism.DeathPunishment
import world.icebear03.starlight.tool.mechanism.NearestPlayer
import world.icebear03.starlight.tool.mechanism.RespawnProtection
import world.icebear03.starlight.tool.world.DarkMare
import world.icebear03.starlight.tool.world.FasterNight
import world.icebear03.starlight.tool.world.WorldBorder
import world.icebear03.starlight.tool.world.WorldRule

object ToolManager {

    fun initialize() {
        CustomTab.initialize()
        WorldBorder.initialize()
        NearestPlayer.initialize()
        RespawnProtection.initialize()
        WorldRule.initialize()
        DarkMare.initialize()
        BossBarCompass.initialize()
        FasterNight.initialize()
        AFK.initialize()
        DailyOnline.initialize()
        CareerOnline.initialize()
        DeathPunishment.initialize()
        CareerChart.initialize()
    }
}