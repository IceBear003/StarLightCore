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
import world.icebear03.starlight.career.SavableCareer
import world.icebear03.starlight.career.UsableCareer
import world.icebear03.starlight.career.internal.ResonateType
import world.icebear03.starlight.career.mechanism.data.Resonate
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

    val careerData = mutableMapOf<UUID, UsableCareer>()
}

fun loadCareerData(player: Player): UsableCareer {
    val uuid = player.uniqueId
    return if (PlayerData.careerData.containsKey(uuid)) {
        PlayerData.careerData[uuid]!!
    } else {
        val data =
            if (player.getDataContainer()["career"] != null) {
                val string = player.getDataContainer()["career"]
                Gson().fromJson(string, SavableCareer::class.java).toUsableCareer()
            } else {
                UsableCareer(
                    mutableMapOf(),
                    mutableMapOf(),
                    mutableMapOf(),
                    mutableListOf(),
                    0,
                    null,
                    ResonateType.FRIENDLY
                ).remake()
            }
        PlayerData.careerData[uuid] = data
        data
    }
}

fun saveCareerData(player: Player) {
    val string = Gson().toJson(loadCareerData(player).toSavableCareer())
    player.getDataContainer()["career"] = string
}