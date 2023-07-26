package world.icebear03.starlight

import org.bukkit.entity.Player
import taboolib.common5.format
import taboolib.platform.compat.PlaceholderExpansion
import taboolib.platform.compat.replacePlaceholder
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.career.mechanism.discharge.checkCooldownStamp
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.other.NearestPlayer
import world.icebear03.starlight.utils.secToFormattedTime
import world.icebear03.starlight.utils.secondLived

object PapiExpansion : PlaceholderExpansion {

    override val identifier: String
        get() = "starlight"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null)
            return args
        val data = loadCareerData(player)
        if (args.startsWith("career_shortcut_")) {
            val int = args.replace("career_shortcut_", "").toInt()
            val string = data.shortCuts[int] ?: return "&7$int - 未绑定&a技能&7/&d顿悟"

            var level: Int
            var cd = 0
            var duration = -1
            Skill.fromId(string)?.let {
                level = data.getSkillLevel(it)
                cd = it.level(level).cooldown
                duration = it.level(level).duration
            }
            Eureka.fromId(string)?.let {
                cd = it.cooldown
                duration = it.duration
            }

            val cdPair = player.checkCooldownStamp(string, cd)

            val state = when (cdPair.first) {
                true -> "&a✔"
                false -> {
                    if (player.isDischarging(string, false)) {
                        "&e✷ &7(&b${(duration - (cd - cdPair.second)).format(1)}秒&7)"
                    } else {
                        "&c✘ &7(&e${cdPair.second}s&7)"
                    }
                }
            }

            return "$int &7- ${string.display()} &7> $state"
        }
        return when (args) {
            "tag" -> "%deluxetags_tag%".replacePlaceholder(player).ifEmpty { "&6跋涉者" }
            "career_points" -> data.points.toString()
            "career_resonate" -> data.resonantBranch?.display() ?: "&7无"
            "career_resonate_tag" -> data.resonantBranch?.display() ?: "&e玩家"
            "period_alive" -> player.secondLived().secToFormattedTime()
            "nearest_player" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").first
            "nearest_distance" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").second
            else -> args
        }
    }
}