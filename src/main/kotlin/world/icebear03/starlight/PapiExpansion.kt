package world.icebear03.starlight

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import taboolib.common5.format
import taboolib.module.chat.colored
import taboolib.platform.compat.PlaceholderExpansion
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.station.mechanism.StaminaModifier
import world.icebear03.starlight.station.mechanism.StationMechanism
import world.icebear03.starlight.station.station
import world.icebear03.starlight.tag.PlayerTag
import world.icebear03.starlight.tool.info.CareerChart
import world.icebear03.starlight.tool.mechanism.AFK
import world.icebear03.starlight.tool.mechanism.NearestPlayer
import world.icebear03.starlight.utils.*

object PapiExpansion : PlaceholderExpansion {

    override val autoReload: Boolean
        get() = true

    override val identifier: String
        get() = "starlight"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null)
            return args
        val career = player.career()
        if (args.startsWith("career_shortcut_")) {
            val int = args.replace("career_shortcut_", "").toInt()
            val name = career.shortCuts[int] ?: return ""
            val spell = getSpell(name)!!

            val level = career.getSpellLevel(name)
            val cd = spell.cd(level)
            val duration = spell.duration(level)

            val cdPair = player.checkCooldownStamp(name, cd)

            if (AFK.isAFKing(player)) {
                return "$int &7- ${display(name)} &7> &c挂机-不读数"
            }

            val state = if (player.isDischarging(name)) {
                val stamp = dischargeStamp[player.uniqueId]!![name] ?: return ""
                val period = (System.currentTimeMillis() - stamp) / 1000.0
                if (duration != -1) {
                    "&e✷ &7(&b${(duration - period).format(1)}秒&7)"
                } else {
                    "&e✷ &7(&b&l∞&7)"
                }
            } else {
                when (cdPair.first) {
                    true -> "&a✔"
                    false -> "&c✘ &7(&e${cdPair.second}s&7)"
                }
            }

            return "$int &7- ${display(name)} &7> $state"
        }


        if (args.startsWith("career_chart_")) {
            val int = args.replace("career_chart_", "").toInt() - 1
            val pair = CareerChart.rank(int)
            val color = CareerChart.rankColor(int + 1)
            return "&a${pair.second} ${color.colored()}${pair.first}"
        }

        if (args.startsWith("career_resonating_")) {
            val int = args.replace("career_resonating_", "").toInt()
            val spellList = Resonate.resonateSpellMap[player.uniqueId]?.toList() ?: return ""
            if (int >= spellList.size)
                return ""
            val resonate = spellList[int]
            return resonate.first.display(resonate.second.second) + " &7来自 " + resonate.second.first
        }

        if (args.startsWith("station_halo_")) {
            val index = args.replace("station_halo_", "").toInt()
            val pairs = StationMechanism.haloMap[player.uniqueId]?.toList() ?: return ""
            if (index > pairs.size)
                return ""
            val pair = pairs[index - 1]
            return "&e${pair.first} &7> &a+${pair.second.format(1)}/秒"
        }
        return when (args) {
            "tag" -> PlayerTag.currentTag(player)?.display?.colored() ?: "&6跋涉者"
            "career_points" -> career.points.toString()
            "career_resonate" -> career.resonantBranch?.display() ?: "&7无"
            "career_resonate_tag" -> career.resonantBranch?.display() ?: "&e玩家"
            "career_online_time" -> player["career_time", PersistentDataType.INTEGER]?.secToFormattedTime() ?: "0秒"
            "nearest_player" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").first
            "nearest_distance" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").second
            "stamina" -> player.stamina().display()
            "stamina_magnification" -> "×" + (StaminaModifier.magnificationMap[player.uniqueId] ?: 1.0).format(2)
            "station_level" -> player.station().level.toRoman()
            "station_cd" -> {
                val result = player.station().checkStamp()
                if (result.first) {
                    "&a✔ &7可放置/回收"
                } else {
                    "&c✘ &7${result.second.secToFormattedTime()}"
                }
            }

            "station_state" -> {
                player.station().location?.let { "&b驻扎中" } ?: "&e未驻扎"
            }

            "daily_online_time" -> player["daily_time", PersistentDataType.INTEGER]?.secToFormattedTime() ?: "0秒"
            "is_afking" -> if (!AFK.isAFKing(player)) "&a在线" else "&e挂机中"
            "name_color" -> CareerChart.rankColor(CareerChart.chart.indexOfFirst { it.first == player.uniqueId } + 1)

            else -> args
        }
    }
}