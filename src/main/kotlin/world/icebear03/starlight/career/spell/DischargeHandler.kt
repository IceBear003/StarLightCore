package world.icebear03.starlight.career.spell

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
import taboolib.platform.util.actionBar
import taboolib.platform.util.hasName
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.career.meetRequirement

object DischargeHandler {
    val dischargeMap = mutableMapOf<String, Player.(id: String, level: Int) -> String?>()
    val finishMap = mutableMapOf<String, Player.(id: String, level: Int) -> Unit>()

    val item = ItemStack(Material.PLAYER_HEAD).modifyMeta<ItemMeta> {
        setDisplayName("§b职业信物")
        lore = listOf(
            "§8| §7将此物置于副手，按下交换键(默认F)时",
            "§8| §7即可施放快捷栏格子编号对应的技能",
            "§8| §c蹲下时不触发此判定",
            ""
        )
    }
        .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E5Y2IwNDU3ZDUwMTVkZmJkM2UyNTJkNzY3MDcxMjc1OTEwNjNhMGIyZmViYWY4YzY0NGFjYWRhOTBiZDRkMCJ9fX0=")

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.forEach { player ->
                val career = player.career()
                career.autoDischarges.forEach spells@{ name ->
                    if (player.isDischarging(name, false))
                        return@spells

                    val spell = getSpell(name)!!
                    val level = career.getSpellLevel(spell)
                    if (level <= 0)
                        return@spells

                    val cd = spell.cd(career.getSpellLevel(spell))
                    if (!player.checkCooldownStamp(name, cd).first)
                        return@spells

                    val duration = spell.duration(level)

                    player.addCooldownStamp(name)
                    player.addDischargeStamp(name)

                    if (duration != -1) {
                        submit(delay = 20L * duration) {
                            if (player.meetRequirement(name, level)) {
                                finishMap[name]?.invoke(player, name, level)
                                player.actionBar("§7自动释放 > ${spell.prefix()} §7${spell.display()} §7已经结束")
                            }
                        }
                    }
                    val msg = dischargeMap[name]?.invoke(player, name, level) ?: return@spells
                    player.actionBar("§7自动释放 > $msg")
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun swapItem(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (player.isSneaking)
            return
        val inv: PlayerInventory = player.inventory
        val item = inv.itemInOffHand
        if (item.hasName()) {
            if (item.itemMeta!!.displayName == "§b职业信物") {
                event.isCancelled = true
                val slot = inv.heldItemSlot + 1
                val msg = triggerShortCut(player, slot) ?: return
                player.sendMessage("§a生涯系统 §7>> $msg")
            }
        }
    }

    fun triggerShortCut(player: Player, key: Int): String? {
        val data = player.career()
        val name = data.shortCuts[key] ?: return "该按键未绑定任何可释放的§a技能§7/§d顿悟"

        val spell = getSpell(name)!!
        val level = data.getSpellLevel(spell)
        if (level <= 0)
            return "请先升级${spell.prefix()}§7 ${spell.display()}"

        val cd = player.checkCooldownStamp(name, spell.cd(level))
        val duration = spell.duration(level)
        if (!cd.first) {
            return "无法释放 ${spell.display()} §7还需等待 §e${cd.second}秒"
        }

        player.addCooldownStamp(name)
        player.addDischargeStamp(name)

        if (duration != -1) {
            submit(delay = 20L * duration) {
                if (player.meetRequirement(name, level)) {
                    finishMap[name]?.invoke(player, name, level)
                    player.sendMessage("§a生涯系统 §7>> ${spell.prefix()} §7${spell.display()} §7已经结束")
                }
            }
        }
        return dischargeMap[name]?.invoke(player, name, level)
    }
}