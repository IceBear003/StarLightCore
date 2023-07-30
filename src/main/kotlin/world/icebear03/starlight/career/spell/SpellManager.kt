package world.icebear03.starlight.career.spell

import world.icebear03.starlight.career.spell.discharge.DischargeHandler
import world.icebear03.starlight.career.spell.discharge.ShortcutDischarge
import world.icebear03.starlight.career.spell.entry.architect.Demolitionist

object SpellManager {

    fun initialize() {
        DischargeHandler.initialize()
        ShortcutDischarge.initialize()
        Demolitionist.initialize()
    }
}