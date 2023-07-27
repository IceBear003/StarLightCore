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
import world.icebear03.starlight.career.data.Career
import world.icebear03.starlight.career.data.Savable
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.stamina.Stamina
import java.util.*

object PlayerData {

    init {
        submit(delay = 100L, period = 100L) {
            onlinePlayers.forEach { saveCareerData(it) }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: PlayerLoginEvent) {
        load(event.player)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun event(event: PlayerQuitEvent) {
        save(event.player)
    }

    fun load(player: Player) {
        player.setupDataContainer()
        loadCareerData(player)
        Resonate.resonating[player.uniqueId] = mutableMapOf()
    }

    fun save(player: Player) {
        saveCareerData(player)
        player.releaseDataContainer()
    }

    val careerData = mutableMapOf<UUID, Career>()
    var staminaData = mutableMapOf<UUID, Stamina>()
}

fun loadCareerData(player: Player): Career {
    val uuid = player.uniqueId
    return PlayerData.careerData.getOrDefault(uuid,
        run {
            val data =
                if (player.getDataContainer()["career"] != null) {
                    val string = player.getDataContainer()["career"]
                    Gson().fromJson(string, Savable::class.java).toUsableCareer()
                } else {
                    Career().remake()
                }
            PlayerData.careerData[uuid] = data
            data
        })
}

fun saveCareerData(player: Player) {
    val string = Gson().toJson(loadCareerData(player).toSavableCareer())
    player.getDataContainer()["career"] = string
}

fun loadStaminaData(player: Player): Stamina {
    val uuid = player.uniqueId
    return PlayerData.staminaData.getOrDefault(uuid,
        run {
            val data =
                if (player.getDataContainer()["stamina"] != null) {
                    val string = player.getDataContainer()["stamina"]
                    Gson().fromJson(string, Stamina::class.java)
                } else {
                    Stamina(uuid)
                }
            PlayerData.staminaData[uuid] = data
            data
        })
}

fun saveStaminaData(player: Player) {
    val string = Gson().toJson(loadStaminaData(player))
    player.getDataContainer()["stamina"] = string
}