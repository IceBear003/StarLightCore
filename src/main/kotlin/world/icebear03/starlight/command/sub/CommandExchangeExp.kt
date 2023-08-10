package world.icebear03.starlight.command.sub

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem


val commandExchangeExp = subCommand {
    execute<Player> { sender, _, _ ->
        val level = sender.level
        if (level >= 1) {
            val exp = getExpPerLevelInVanilla(level)
            val amount = exp / 14
            if (amount >= 1) {
                sender.level -= 1
                sender.giveItem(ItemStack(Material.EXPERIENCE_BOTTLE, amount))
                sender.sendMessage("§b繁星工坊 §7>> 消耗1经验等级兑换为${amount}个经验瓶")
            } else {
                sender.sendMessage("§b繁星工坊 §7>> 经验值过低，无法转换为经验瓶")
            }
        }
    }
}

fun getExpPerLevelInVanilla(level: Int): Int {
    return if (level <= 15) {
        2 * level + 7
    } else if (level <= 30) {
        5 * level - 38
    } else {
        9 * level - 158
    }
}