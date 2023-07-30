package world.icebear03.starlight.other

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import java.util.*

object AFK {

    val afkPlayers = mutableListOf<UUID>()
    val checking = mutableListOf<UUID>()

    fun initialize() {
        submit(period = 20) {
            onlinePlayers.forEach { player ->
                if (QTEProvider.isQTEing(player))
                    return@forEach
                if (isAFKing(player) || checking.contains(player.uniqueId))
                    return@forEach
                if (Math.random() <= 0.001) {
                    checking += player.uniqueId
                    player.closeInventory()
                    QTEProvider.sendQTE(
                        player,
                        QTEProvider.QTEDifficulty.HARD,
                        QTEProvider.QTEType.THREE_TIMES,
                        {
                            checking -= player.uniqueId
                            if (it != QTEProvider.QTEResult.ACCEPTED) {
                                sendTitle("", "§7检测到挂机，重新进入服务器以刷新状态", 0, 86400 * 20, 0)
                                afkPlayers += uniqueId
                            }
                        },
                        "§b挂机检测"
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        afkPlayers -= player.uniqueId
        player.sendTitle("", "§7", 0, 1, 0)
    }

    fun isAFKing(player: Player): Boolean {
        return afkPlayers.contains(player.uniqueId)
    }
}