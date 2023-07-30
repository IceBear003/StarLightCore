package world.icebear03.starlight.career.spell

import world.icebear03.starlight.career.spell.discharge.DischargeHandler
import world.icebear03.starlight.career.spell.discharge.ShortcutDischarge
import world.icebear03.starlight.career.spell.entry.architect.Demolitionist
import world.icebear03.starlight.career.spell.entry.cook.Brewer

object SpellManager {

    fun initialize() {
        DischargeHandler.initialize()
        ShortcutDischarge.initialize()

        Demolitionist.initialize()

        Brewer.initialize()
    }
}