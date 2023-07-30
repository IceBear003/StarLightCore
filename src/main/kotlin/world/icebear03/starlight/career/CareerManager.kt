package world.icebear03.starlight.career

import world.icebear03.starlight.career.core.Resonate
import world.icebear03.starlight.career.core.`class`.ClassLoader
import world.icebear03.starlight.career.spell.SpellManager

object CareerManager {

    fun initialize() {
        ClassLoader.initialize()
        Resonate.initialize()
        SpellManager.initialize()
    }
}