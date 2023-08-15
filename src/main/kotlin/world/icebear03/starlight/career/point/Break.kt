package world.icebear03.starlight.career.point

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career
import world.icebear03.starlight.tool.mechanism.AFK
import world.icebear03.starlight.utils.addCooldownStamp
import world.icebear03.starlight.utils.checkCooldownStamp
import world.icebear03.starlight.utils.getEnchants

object Break {
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun breakBlock(event: BlockBreakEvent) {

        val type = event.block.type

        val player = event.player
        if (AFK.isAFKing(player))
            return
        val career = player.career()

        if (player.inventory.itemInMainHand.getEnchants().containsKey(Enchantment.SILK_TOUCH))
            return

        //rate - cd
        var pair = when (type) {
            Material.ANCIENT_DEBRIS -> 0.005 to 25

            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE -> 0.001 to 5

            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE -> 0.0001 to 1

            else -> -1.0 to 0
        }

        val name = "挖掘获得技能点${pair.second}"
        if (!player.checkCooldownStamp(name, pair.second).first)
            return
        player.addCooldownStamp(name)

        var rate = pair.first

        if (career.hasClass("工人"))
            rate *= 2

        if (Math.random() <= rate) {
            career.addPoint(1)
            player.sendMessage("§a生涯系统 §7>> 你挖矿时偶然获得了§a1技能点")
        }
    }
}