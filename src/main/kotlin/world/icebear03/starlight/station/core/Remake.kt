package world.icebear03.starlight.station.core

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.station.setStamina
import world.icebear03.starlight.station.station

fun Player.remakeStamina() {
    setStamina(2000.0)
    station().deleteFromWorld()
    station().level = 1
    station().stamp = System.currentTimeMillis() - 100000000

    submit {
        giveItem(station().generateItem())
    }
}