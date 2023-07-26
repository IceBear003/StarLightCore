package world.icebear03.starlight.command.sub

import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem
import world.icebear03.starlight.stamina.BeaconMechanism
import world.icebear03.starlight.stamina.getBeacon


val commandBeacon = subCommand {
    dynamic("forceMode") {
        suggestionUncheck<Player> { _, _ -> listOf("forceMode") }
        execute<Player> { sender, _, _ ->
            val beacon = sender.getBeacon()
            beacon.location?.let {
                it.block.type = Material.AIR
            }
            beacon.location = null
            beacon.lastRecycle = null
            sender.giveItem(BeaconMechanism.generateItem(beacon))
            sender.sendMessage("§a生涯系统 §7>> 驻扎信标已经发送至背包")
        }
    }
    execute<Player> { sender, _, _ ->
        val beacon = sender.getBeacon()
        val result = beacon.canRecycle()
        if (result.first) {
            sender.giveItem(BeaconMechanism.generateItem(beacon))
            sender.sendMessage("§a生涯系统 §7>> 驻扎信标已经发送至背包")
        } else {
            sender.sendMessage("§a生涯系统 §7>> 驻扎信标被破坏，需要§a${result.second}§7后才能重新获取")
        }
    }
}