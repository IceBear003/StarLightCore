package world.icebear03.starlight

import com.google.gson.Gson
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
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
import world.icebear03.starlight.station.data.Stamina
import java.util.*

object AutoIO {

    fun initialize() {
        submit(delay = 100L, period = 100L) {
            onlinePlayers.forEach { it.saveStarLightData() }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        event.player.loadStarLightData()
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun quit(event: PlayerQuitEvent) {
        val player = event.player
        player.saveStarLightData()
        player.releaseDataContainer()
    }
}

val careerMap = mutableMapOf<UUID, Career>()
val staminaMap = mutableMapOf<UUID, Stamina>()

fun Player.career(): Career {
    return careerMap[uniqueId]!!
}

fun Player.stamina(): Stamina {
    return staminaMap[uniqueId]!!
}

fun Player.saveStarLightData() {
    getDataContainer()["career"] = Gson().toJson(career().toSavable())
    getDataContainer()["stamina"] = Gson().toJson(stamina())
}

//仅在进入服务器或者热重载后调用
fun Player.loadStarLightData() {
    setupDataContainer()
    val data = getDataContainer()
    careerMap[uniqueId] = data["career"]?.let { Gson().fromJson(it, Savable::class.java).toCareer() } ?: Career().remake()
    staminaMap[uniqueId] = data["stamina"]?.let { Gson().fromJson(it, Stamina::class.java) } ?: Stamina()
    Resonate.resonateMap[uniqueId] = mutableMapOf()
}