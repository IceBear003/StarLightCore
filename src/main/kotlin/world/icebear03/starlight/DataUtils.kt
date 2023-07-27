package world.icebear03.starlight

import com.google.gson.Gson
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.expansion.getDataContainer
import taboolib.expansion.releaseDataContainer
import taboolib.expansion.setupDataContainer
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.data.Career
import world.icebear03.starlight.career.data.Savable
import java.util.*

object AutoIO {

    init {
        submit(delay = 100L, period = 100L) {
            onlinePlayers.forEach { save(it) }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: PlayerLoginEvent) {
        event.player.loadStarLightData()
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun event(event: PlayerQuitEvent) {
        save(event.player)
    }

    fun save(player: Player) {
        player.saveStarLightData()
        //仅在退出时release
        player.releaseDataContainer()
    }
}

val careerMap = mutableMapOf<UUID, Career>()

fun Player.career(): Career {
    return careerMap[uniqueId]!!
}

fun Player.saveStarLightData() {
    getDataContainer()["career"] = Gson().toJson(career())
}

//仅在进入服务器或者热重载后调用
fun Player.loadStarLightData() {
    setupDataContainer()
    val data = getDataContainer()
    careerMap[uniqueId] = data["career"]?.let { Gson().fromJson(it, Savable::class.java).toCareer() } ?: Career().remake()
    Resonate.resonateMap[uniqueId] = mutableMapOf()
}