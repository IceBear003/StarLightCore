package world.icebear03.starlight.tag

import org.bukkit.entity.EnderDragon
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored

object SpecialTagGiver {

    fun initialize() {

    }

    @SubscribeEvent
    fun kill(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity !is EnderDragon)
            return
        val killer = entity.killer ?: return
        if (!PlayerTag.addTag(killer, "屠龙者")) {
            killer.sendMessage("§b繁星工坊 §7>> 千里而来，终于征服了末影龙，回去又是一段新的征程呢……(获得称号${"&{#9f8bff}屠龙者".colored()}§7)")
        }
    }
}