package world.icebear03.starlight.career.spell.entry.worker

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.*

object Lumberjack {

    val stems = listOf(Material.CRIMSON_STEM, Material.WARPED_STEM)

    val logs = Material.entries.filter {
        val string = it.toString()
        string.contains("_LOG")
    } + stems

    val woodenItems = Material.entries.filter {
        val string = it.toString()
        it.isFuel &&
                !string.contains("WOOL") &&
                !string.contains("CARPET") &&
                !string.contains("BANNER") &&
                it != Material.JUKEBOX &&
                it != Material.NOTE_BLOCK &&
                it != Material.DAYLIGHT_DETECTOR &&
                it != Material.SMITHING_TABLE &&
                it != Material.BEEHIVE &&
                it != Material.BEE_NEST &&
                it != Material.COAL &&
                it != Material.BLAZE_ROD &&
                it != Material.LAVA_BUCKET &&
                it != Material.COAL_BLOCK &&
                it != Material.CHARCOAL
    }

    val woodenBlocks = Material.entries.filter {
        var flag = false
        try {
            val withoutWall = Material.valueOf(it.toString().replace("_WALL", ""))
            if (woodenItems.contains(withoutWall))
                flag = true
        } catch (ignored: Exception) {
        }
        woodenItems.contains(it) || flag
    }

    fun initialize() {
        addLimit(HandlerType.USE, "伐木工" to 0, Material.DIAMOND_AXE, Material.NETHERITE_AXE)
        addLimit(HandlerType.PLACE, "巨树攀登者" to 1, Material.SCAFFOLDING)

        logs.addLowestListener(HandlerType.DROP_IF_BREAK) { _, player, _ ->
            (if (!player.meetRequirement("伐木工", 0) && Math.random() > 0.2) {
                player.sendMessage("§a生涯系统 §7>> 破坏原木/菌柄无掉落，解锁 ${display("伐木工")} §7可以提高成功率")
                false
            } else true) to null
        }

        logs.addHighListener(HandlerType.BREAK) { _, player, _ ->
            if (player.isDischarging("斩尽桎梏")) {
                val amount = player["lumberjack_logs", PersistentDataType.INTEGER] ?: 0
                player["lumberjack_logs", PersistentDataType.INTEGER] = amount + 1
            }
            null
        }

        stems.addHighListener(HandlerType.BREAK) { event, player, type ->
            val breakEvent = event as BlockBreakEvent
            if (player.meetRequirement("抽丝剥茧") && player.world.environment == World.Environment.NETHER && Math.random() <= 0.33)
                breakEvent.block.world.dropItem(event.block.location, ItemStack(type))
            null
        }

        woodenBlocks.addHighListener(HandlerType.BREAK) { _, player, _ ->
            val level = player.spellLevel("全力劈砍")
            if (level >= 0)
                player.effect(PotionEffectType.FAST_DIGGING, 2, level + 1)
            null
        }

        "巧力用斧".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下次使用斧不消耗耐久值"
        }

        "斩尽桎梏".discharge { name, level ->
            submit(period = 20L) {
                if (!isDischarging(name))
                    cancel()
                val amount = this@discharge["lumberjack_logs", PersistentDataType.INTEGER] ?: 0
                val potionLevel = minOf(amount / (if (level >= 2) 3 else 4), if (level >= 3) 4 else 3)
                if (potionLevel >= 1)
                    this@discharge.effect(PotionEffectType.INCREASE_DAMAGE, 2, potionLevel)
            }
            "§a技能 ${display(name)} §7释放成功，破坏原木或菌柄可获得力量加成"
        }.finish { _, _ ->
            this.remove("lumberjack_logs")
            null
        }

        "护林员".discharge { name, _ ->
            finish(name)
            location.getBlockAside(5, Material.FIRE, Material.SOUL_FIRE).forEach { loc ->
                loc.block.type = Material.AIR
            }
            "§d顿悟 ${display(name)} §7释放成功，范围内火被熄灭"
        }
    }

    @SubscribeEvent
    fun damageItem(event: PlayerItemDamageEvent) {
        val player = event.player
        val item = event.item
        if (!item.type.toString().contains("_AXE"))
            return
        if (player.isDischarging("巧力用斧")) {
            player.finish("巧力用斧")
            event.isCancelled = true
        }
    }
}