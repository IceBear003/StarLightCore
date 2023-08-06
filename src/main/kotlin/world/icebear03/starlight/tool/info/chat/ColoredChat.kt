package world.icebear03.starlight.tool.info.chat

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.remove
import world.icebear03.starlight.utils.set

object ColoredChat {

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCancelled = true)
    fun chat(event: AsyncPlayerChatEvent) {
        if (event.message.length > 1) event.message = getColor(event.player).colored() + event.message
    }

    fun setColor(player: Player, color: String? = null) {
        if (color != null) player["chat_color", PersistentDataType.STRING] = color
        else player.remove("chat_color")
    }

    fun getColor(player: Player): String {
        return player["chat_color", PersistentDataType.STRING] ?: ""
    }
}