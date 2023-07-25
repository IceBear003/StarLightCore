package world.icebear03.starlight.career.mechanism.discharge

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.loadCareerData


object DischargeHandler {
    val dischargeSkillMap = mutableMapOf<Skill, Player.(level: Int) -> Unit>()
    val dischargeEurekaMap = mutableMapOf<Eureka, Player.() -> Unit>()

    val item = ItemStack(Material.PLAYER_HEAD).modifyMeta<ItemMeta> {
        setDisplayName("§b职业信物")
        modifyLore {
            listOf(
                "§8| §7将此物置于副手",
                "§8| §7并按下交换键(默认F)",
                "§8| §7即可施放主手格子",
                "§8| §7编号对应的技能"
            )
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun swapItem(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        val inv: PlayerInventory = player.inventory
        if (inv.itemInOffHand == item) {
            event.isCancelled = true
            val slot = inv.heldItemSlot
            triggerShortCut(player, slot)
        }
    }

    fun triggerShortCut(player: Player, key: Int): String? {
        val data = loadCareerData(player)
        val id = data.shortCuts[key] ?: return "该按键未绑定任何可释放的技能/顿悟"

        val skill = Skill.fromId(id)
        if (skill != null) {
            val level = data.getSkillLevel(skill)
            if (level <= 0)
                return "请先升级技能 ${skill.display()}"

            val cd = player.checkCooldownStamp(id, skill.level(level).cooldown.toDouble(), true)
            if (!cd.first) {
                return null
            }
            player.addCooldownStamp(id)

            dischargeSkillMap[skill]!!.invoke(player, level)
            return "技能 ${skill.display()} &r释放成功"
        }

        val eureka = Eureka.fromId(id)
        if (eureka != null) {
            if (!data.hasEureka(eureka))
                return "请先激活顿悟 ${eureka.display()}"

            val cd = player.checkCooldownStamp(id, eureka.cooldown.toDouble(), true)
            if (!cd.first) {
                return null
            }
            player.addCooldownStamp(id)

            dischargeEurekaMap[eureka]!!.invoke(player)
            return "顿悟 ${eureka.display()} &r释放成功"
        }

        return "该按键貌似未绑定任何技能/顿悟"
    }
}