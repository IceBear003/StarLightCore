package world.icebear03.starlight.command.sub

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.loadStaminaData


val commandStamina = subCommand {
    dynamic("mode") {
        suggestionUncheck<Player> { _, _ -> listOf("add", "set", "take") }
        dynamic("amount") {
            suggestionUncheck<Player> { _, _ -> listOf("数量(0-1800的整数)") }
            execute<Player> { sender, ctx, _ ->
                sender.sendMessage(handleCareerPointCommand(sender, ctx["mode"], ctx["amount"].toInt()))
            }
            dynamic("player") {
                suggestionUncheck<Player> { _, _ -> onlinePlayers.map { it.name } }

                execute<CommandSender> { sender, ctx, _ ->
                    sender.sendMessage(
                        handleCareerPointCommand(
                            Bukkit.getPlayer(ctx["player"]),
                            ctx["mode"],
                            ctx["amount"].toInt()
                        )
                    )
                }
            }
        }
    }
}

fun handleStaminaCommand(player: Player?, mode: String, amount: Int): String {
    if (player == null)
        return "§a生涯系统 §7>> 玩家不存在"
    val data = loadStaminaData(player)
    when (mode) {
        "add" -> data.addStamina(amount.toDouble())
        "set" -> data.setStamina(amount.toDouble())
        "take" -> data.takeStamina(amount.toDouble())
        else -> {
            return "§a生涯系统 §7>> 模式不正确, 只能填写 add/set/take"
        }
    }
    return "§a生涯系统 §7>> 操作成功"
}