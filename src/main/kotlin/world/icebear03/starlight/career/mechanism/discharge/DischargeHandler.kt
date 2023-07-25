package world.icebear03.starlight.career.mechanism.discharge

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.hasName
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.internal.Eureka
import world.icebear03.starlight.career.internal.Skill
import world.icebear03.starlight.loadCareerData

object DischargeHandler {
    val dischargeMap = mutableMapOf<String, Player.(id: String, level: Int) -> String?>()
    val finishMap = mutableMapOf<String, Player.(id: String, level: Int) -> Unit>()

    val item = ItemStack(Material.PLAYER_HEAD).modifyMeta<ItemMeta> {
        setDisplayName("§b职业信物")
        lore = listOf(
            "§8| §7将此物置于副手",
            "§8| §7并按下交换键(默认F)",
            "§8| §7即可施放主手格子",
            "§8| §7编号对应的技能"
        )
    }
        .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E5Y2IwNDU3ZDUwMTVkZmJkM2UyNTJkNzY3MDcxMjc1OTEwNjNhMGIyZmViYWY4YzY0NGFjYWRhOTBiZDRkMCJ9fX0=")

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun swapItem(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        val inv: PlayerInventory = player.inventory
        if (inv.itemInOffHand.hasName()) {
            if (inv.itemInOffHand.itemMeta!!.displayName == "§b职业信物") {
                event.isCancelled = true
                val slot = inv.heldItemSlot + 1
                val msg = triggerShortCut(player, slot) ?: return
                player.sendMessage("§a生涯系统 §7>> $msg")
            }
        }
    }

    fun triggerShortCut(player: Player, key: Int): String? {
        val data = loadCareerData(player)
        val id = data.shortCuts[key] ?: return "该按键未绑定任何可释放的§a技能§7/§d顿悟"

        val skill = Skill.fromId(id)
        if (skill != null) {
            val level = data.getSkillLevel(skill)
            if (level <= 0)
                return "请先升级§a技能§7 ${skill.display()}"

            val leveledSkill = skill.level(level)
            val cd = player.checkCooldownStamp(id, leveledSkill.cooldown.toDouble())
            val duration = leveledSkill.duration
            if (!cd.first) {
                return "无法释放 ${skill.display()} §7还需等待 §e${cd.second}秒"
            }
            player.addCooldownStamp(id)

            if (duration != -1) {
                submit(delay = 20L * duration) {
                    finishMap[id]?.invoke(player, id, level)
                    player.sendMessage("§a生涯系统 §7>> §a技能 §7${skill.display()} §7已经结束")
                }
            }
            return dischargeMap[id]?.invoke(player, id, level)
        }

        val eureka = Eureka.fromId(id)
        if (eureka != null) {
            if (!data.hasEureka(eureka))
                return "请先激活§d顿悟§7 ${eureka.display()}"

            val cd = player.checkCooldownStamp(id, eureka.cooldown.toDouble())
            val duration = eureka.duration
            if (!cd.first) {
                return "无法释放 ${eureka.display()} §7还需等待 §e${cd.second}秒"
            }
            player.addCooldownStamp(id)

            if (duration != -1) {
                submit(delay = 20L * duration) {
                    finishMap[id]?.invoke(player, id, 1)
                    player.sendMessage("§a生涯系统 §7>> §d顿悟 §7${eureka.display()} §7已经结束")
                }
            }
            return dischargeMap[id]?.invoke(player, id, 1)
        }

        return "该按键未绑定任何可释放的§a技能§7/§d顿悟"
    }
}

fun Player.isDischarging(key: String, level: Int = 1, removeIfConsumable: Boolean = true): Boolean {
    if (level <= 0)
        return false

    val stamp = (cooldownStamps[this.uniqueId] ?: return false)[key] ?: return false
    val period = (System.currentTimeMillis() - stamp) / 1000.0

    val skill = Skill.fromId(key)
    if (skill != null) {
        val duration = skill.level(level).duration
        if (duration == -1) {
            if (removeIfConsumable)
                cooldownStamps[this.uniqueId]!!.remove(key)
            return true
        }
        return duration >= period
    }

    val eureka = Eureka.fromId(key)
    if (eureka != null) {
        val duration = eureka.duration
        if (duration == -1) {
            if (removeIfConsumable)
                cooldownStamps[this.uniqueId]!!.remove(key)
            return true
        }
        return duration >= period
    }

    return false
}

fun String.defineDischarge(function: Player.(id: String, level: Int) -> String?) {
    DischargeHandler.dischargeMap[this] = function
}

fun String.defineFinish(function: Player.(id: String, level: Int) -> Unit) {
    DischargeHandler.finishMap[this] = function
}