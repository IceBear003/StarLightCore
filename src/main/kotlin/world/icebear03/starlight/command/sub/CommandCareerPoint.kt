package world.icebear03.starlight.command.sub

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.loadCareerData


val commandCareerPoint = subCommand {
    dynamic("mode") {
        suggestionUncheck<Player> { _, _ -> listOf("add", "set", "take") }
        dynamic("amount") {
            suggestionUncheck<Player> { _, _ -> listOf("数量(整数)") }
            execute<Player> { sender, ctx, _ ->
                sender.sendMessage(handle(sender, ctx["mode"], ctx["amount"].toInt()))
            }
            dynamic("player") {
                suggestionUncheck<Player> { _, _ -> onlinePlayers.map { it.name } }

                execute<CommandSender> { sender, ctx, _ ->
                    sender.sendMessage(handle(Bukkit.getPlayer(ctx["player"]), ctx["mode"], ctx["amount"].toInt()))
                }
            }
        }
    }
}

fun handle(player: Player?, mode: String, amount: Int): String {
    if (player == null)
        return "玩家不存在"
    val data = loadCareerData(player)
    when (mode) {
        "add" -> data.addPoint(amount)
        "set" -> data.setPoint(amount)
        "take" -> data.takePoint(amount)
        else -> {
            return "模式不正确"
        }
    }
    return "操作成功"
}