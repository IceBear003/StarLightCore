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
        if (!pdc.has(deathStampKey, PersistentDataType.LONG)) {
            pdc.set(deathStampKey, PersistentDataType.LONG, System.currentTimeMillis())
            player.sendMessage("§b繁星工坊 §7>> 这一觉睡了好久...我这是到哪了?")
            player.sendMessage("§b繁星工坊 §7>> 进入新玩家保护模式，持续时间§e10分钟")
            player.sendMessage("               §7|—— 获得持续的§b20%免伤")
            player.sendMessage("               §7|—— 体力值损耗§b减慢80%")
        }
    }
}