package world.icebear03.starlight.tool

import world.icebear03.starlight.tool.info.BossBarCompass
import world.icebear03.starlight.tool.info.CustomTab
import world.icebear03.starlight.tool.info.DailyOnline
import world.icebear03.starlight.tool.player.AFK
import world.icebear03.starlight.tool.player.NearestPlayer
import world.icebear03.starlight.tool.player.RespawnProtection
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
    }
}