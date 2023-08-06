package world.icebear03.starlight.career.spell.entry.worker

import org.bukkit.Material
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object ToolMaker {

    fun initialize() {
        addLimit(HandlerType.USE, "工具制造商" to 0, Material.GRINDSTONE)
    }
}