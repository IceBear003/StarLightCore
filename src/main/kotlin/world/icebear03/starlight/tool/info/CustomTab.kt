package world.icebear03.starlight.tool.info

import taboolib.common.platform.function.submit
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.onlinePlayers

object CustomTab {
    fun initialize() {
        submit(period = 10L) {
            onlinePlayers.forEach { player ->
                player.playerListHeader = "§7       欢迎来到 §bStarLight§3繁星工坊       \n\n" +
                        "§a玩家列表  §7(§e%server_online% §7/ 50)\n".replacePlaceholder(player)
                player.playerListFooter = "\n" +
                        "§7QQ群: §e747909129\n" +
                        "§7       进群可查看服务器游玩指导       "
            }
        }
    }
}