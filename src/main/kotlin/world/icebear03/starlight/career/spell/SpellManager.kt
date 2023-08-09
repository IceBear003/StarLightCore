package world.icebear03.starlight.career.spell

import world.icebear03.starlight.career.spell.discharge.DischargeHandler
import world.icebear03.starlight.career.spell.discharge.ShortcutDischarge
import world.icebear03.starlight.career.spell.entry.architect.Demolitionist
import world.icebear03.starlight.career.spell.entry.architect.FortressEngineer
import world.icebear03.starlight.career.spell.entry.architect.StructuralEngineer
import world.icebear03.starlight.career.spell.entry.architect.TrafficEngineer
import world.icebear03.starlight.career.spell.entry.cook.Baker
import world.icebear03.starlight.career.spell.entry.cook.Brewer
import world.icebear03.starlight.career.spell.entry.cook.Butcher
import world.icebear03.starlight.career.spell.entry.cook.Chef
import world.icebear03.starlight.career.spell.entry.farmer.Botanist
import world.icebear03.starlight.career.spell.entry.farmer.Fisherman
import world.icebear03.starlight.career.spell.entry.farmer.Rancher
import world.icebear03.starlight.career.spell.entry.scholar.Enchanter
import world.icebear03.starlight.career.spell.entry.scholar.RedstoneEngineer
import world.icebear03.starlight.career.spell.entry.scholar.Teacher
import world.icebear03.starlight.career.spell.entry.warrior.Explorer
import world.icebear03.starlight.career.spell.entry.warrior.MonsterHunter
import world.icebear03.starlight.career.spell.entry.warrior.Soldier
import world.icebear03.starlight.career.spell.entry.warrior.WeaponMaster
import world.icebear03.starlight.career.spell.entry.worker.Lumberjack
import world.icebear03.starlight.career.spell.entry.worker.Miner
import world.icebear03.starlight.career.spell.entry.worker.Smelter

object SpellManager {

    fun initialize() {
        DischargeHandler.initialize()
        ShortcutDischarge.initialize()

        Demolitionist.initialize()
        FortressEngineer.initialize()
        StructuralEngineer.initialize()
        TrafficEngineer.initialize()

        Brewer.initialize()
        Baker.initialize()
        Chef.initialize()
        Butcher.initialize()

        Enchanter.initialize()
        RedstoneEngineer.initialize()
        Teacher.initialize()

        Fisherman.initialize()
        Botanist.initialize()
        Rancher.initialize()

        Lumberjack.initialize()
        Miner.initialize()
        Smelter.initialize()

        Explorer.initialize()
        WeaponMaster.initialize()
        Soldier.initialize()
        MonsterHunter.initialize()
    }
}