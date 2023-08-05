package world.icebear03.starlight.command.sub

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.tag.PlayerTag
import world.icebear03.starlight.tag.TagLibrary
import world.icebear03.starlight.tag.TagUI


val commandTag = subCommand {
    execute<Player> { sender, _, _ ->
        TagUI.open(sender)
    }
    dynamic("cmd") {
        suggestionUncheck<Player> { _, _ -> listOf("add", "set", "remove", "reload") }
        execute<Player> { sender, ctx, _ ->
            if (!sender.isOp)
                return@execute
            if (ctx["cmd"] == "reload") {
                TagLibrary.reloadAllTags()
                sender.sendMessage("§b繁星工坊 §7>> 已载入所有称号")
            }
        }
        dynamic("player") {
            suggestionUncheck<Player> { _, _ -> onlinePlayers.map { it.name } }
            dynamic("tag") {
                suggestionUncheck<Player> { _, _ -> TagLibrary.tags.keys.toList() }
                execute<CommandSender> { sender, ctx, _ ->
                    if (!sender.isOp)
                        return@execute

                    val player = Bukkit.getPlayer(ctx["player"])!!
                    when (ctx["cmd"]) {
                        "add" -> {
                            PlayerTag.addTag(player, ctx["tag"])
                            sender.sendMessage("§b繁星工坊 §7>> 已尝试添加称号 ${ctx["tag"]} 给玩家 ${ctx["player"]}")
                        }

                        "set" -> {
                            PlayerTag.setTag(player, ctx["tag"])
                            sender.sendMessage("§b繁星工坊 §7>> 已尝试设置称号 ${ctx["tag"]} 给玩家 ${ctx["player"]}")
                        }

                        "remove" -> {
                            PlayerTag.removeTag(player, ctx["tag"])
                            sender.sendMessage("§b繁星工坊 §7>> 已尝试删除称号 ${ctx["tag"]} 给玩家 ${ctx["player"]}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}