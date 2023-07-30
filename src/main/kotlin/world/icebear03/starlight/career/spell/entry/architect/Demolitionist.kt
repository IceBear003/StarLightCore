package world.icebear03.starlight.career.spell.entry.architect

import org.bukkit.Material
import world.icebear03.starlight.career.addLimit
import world.icebear03.starlight.career.spell.handler.limit.LimitType

object Demolitionist {
    fun initialize() {
        val basic = "爆破师" to 0
        addLimit(LimitType.USE, basic, Material.END_CRYSTAL)
        addLimit(LimitType.PLACE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(LimitType.USE, basic, Material.TNT, Material.TNT_MINECART)
        addLimit(LimitType.CRAFT, basic, Material.FIRE_CHARGE)
        addLimit(LimitType.USE, basic, Material.FIRE_CHARGE)
    }


}