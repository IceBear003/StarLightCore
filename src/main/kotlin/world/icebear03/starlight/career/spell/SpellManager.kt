package world.icebear03.starlight.career.spell

import world.icebear03.starlight.career.spell.discharge.DischargeHandler
import world.icebear03.starlight.career.spell.discharge.ShortcutDischarge
import world.icebear03.starlight.career.spell.entry.architect.Demolitionist
import world.icebear03.starlight.career.spell.entry.cook.Baker
import world.icebear03.starlight.career.spell.entry.cook.Brewer
import world.icebear03.starlight.career.spell.entry.cook.Butcher
import world.icebear03.starlight.career.spell.entry.cook.Chef
import world.icebear03.starlight.career.spell.entry.farmer.Fisherman
import world.icebear03.starlight.career.spell.entry.scholar.Enchanter
import world.icebear03.starlight.career.spell.entry.scholar.RedstoneEngineer
import world.icebear03.starlight.career.spell.entry.scholar.Teacher

object SpellManager {

    fun initialize() {
        DischargeHandler.initialize()
        ShortcutDischarge.initialize()

        Demolitionist.initialize()

        Brewer.initialize()
        Baker.initialize()
        Chef.initialize()
        Butcher.initialize()

        Enchanter.initialize()
        RedstoneEngineer.initialize()
        Teacher.initialize()

        Fisherman.initialize()
    }
}