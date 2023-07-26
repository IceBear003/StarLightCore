package world.icebear03.starlight.other

import org.bukkit.NamespacedKey
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object DeathStamp {

    val deathStampKey = NamespacedKey.minecraft("last_death_stamp")

    @SubscribeEvent
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        player.persistentDataContainer.set(deathStampKey, PersistentDataType.LONG, System.currentTimeMillis())
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        val pdc = player.persistentDataContainer
        if (!pdc.has(deathStampKey, PersistentDataType.LONG))
            pdc.set(deathStampKey, PersistentDataType.LONG, System.currentTimeMillis())
    }
}