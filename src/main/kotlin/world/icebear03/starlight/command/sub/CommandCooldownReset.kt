package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import world.icebear03.starlight.utils.clearCooldownStamp
import world.icebear03.starlight.utils.clearDischargeStamp


val commandCooldownReset = subCommand {
    execute<Player> { sender, _, _ ->
        sender.clearCooldownStamp()
        sender.clearDischargeStamp()
        sender.sendMessage("§b繁星工坊 §7>> §a技能§7/§d顿悟§7冷却已清除")
    }
}