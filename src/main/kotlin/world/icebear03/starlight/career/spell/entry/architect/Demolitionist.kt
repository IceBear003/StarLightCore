package world.icebear03.starlight.career.spell.entry.architect

import org.bukkit.Material
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object Demolitionist {
    fun initialize() {
        val basic = "爆破师" to 0
        addLimit(HandlerType.USE, basic, Material.END_CRYSTAL)
        addLimit(HandlerType.PLACE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(HandlerType.USE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(HandlerType.CRAFT, basic, Material.FIRE_CHARGE)
        addLimit(HandlerType.USE, basic, Material.FIRE_CHARGE)
    }
    
}