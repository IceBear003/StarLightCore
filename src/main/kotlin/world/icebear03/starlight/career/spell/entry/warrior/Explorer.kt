package world.icebear03.starlight.career.spell.entry.warrior

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career
import world.icebear03.starlight.career.discharge
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.finish
import world.icebear03.starlight.career.meetRequirement
import world.icebear03.starlight.utils.effect

object Explorer {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.filter { it.career().hasBranch("探险家") }.forEach { player ->
                player.effect(PotionEffectType.SPEED, 2, if (player.meetRequirement("奔赴新边疆")) 2 else 1)

                if (player.meetRequirement("旷野互助")) {
                    player.getNearbyEntities(6.0, 6.0, 6.0).filterIsInstance<LivingEntity>().forEach {
                        it.effect(PotionEffectType.REGENERATION, 2, 2)
                    }
                }
            }
        }

        "探索者的行囊".discharge { name, level ->
            finish(name)
            val item = ItemStack(
                when (level) {
                    2 -> Material.COOKED_COD
                    3 -> Material.PUMPKIN_PIE
                    else -> Material.BREAD
                }
            )
            val heal = 4 + 4 * level
            if (health == maxHealth && foodLevel >= 20)
                effect(PotionEffectType.SPEED, 75, if (level >= 3) 3 else 2)
            if (health != maxHealth)
                health = minOf(health + heal, maxHealth)
            if (foodLevel < 20)
                giveItem(item)

            "§a技能 ${display(name)} §7释放成功，获得状态与物品补给"
        }
    }
}