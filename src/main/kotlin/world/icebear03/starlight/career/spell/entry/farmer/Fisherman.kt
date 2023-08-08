package world.icebear03.starlight.career.spell.entry.farmer

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Guardian
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.attacker
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.entry.farmer.Fisherman.CaughtRarity.*
import world.icebear03.starlight.tool.mechanism.QTEProvider
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.set
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

object Fisherman {

    fun initialize() {
        submit(period = 20L) {
            onlinePlayers.filter { it.meetRequirement("渔夫", 0) }.forEach { player ->
                player.effect(PotionEffectType.LUCK, 2, 1)
                if (player.isInWater) {
                    player.effect(PotionEffectType.CONDUIT_POWER, 2, 1)
                }
                val loc = player.location
                val block = loc.block
                val world = player.world
                if (block.biome.toString().contains("OCEAN") && !world.isClearWeather) {
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, 2, 1)
                }
            }
        }

        "大洋眷顾".discharge { name, level ->
            finish(name)
            val biome = location.block.biome
            val duration =
                if (biome.toString().contains("OCEAN"))
                    if (biome.toString().contains("DEEP") && level == 3) 180
                    else when (level) {
                        1 -> 60
                        2 -> 90
                        3 -> 120
                        else -> 30 + 10 * level
                    }
                else 30 + 10 * level

            effect(PotionEffectType.LUCK, duration, 2)
            "§a技能 ${display(name)} §7释放成功，获得§a${duration}秒§7幸运II效果"
        }

        "凝望反制".discharge { name, _ ->
            removePotionEffect(PotionEffectType.SLOW_DIGGING)
            "§d顿悟 ${display(name)} §7释放成功，清除挖掘疲劳效果，一段时间内受到守卫者伤害降低"
        }

        "收获涛声".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下一次钓鱼时若收获鱼类，其数量会有额外增加"
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun damaged(event: EntityDamageByEntityEvent) {
        val damaged = event.entity
        if (damaged !is Player)
            return

        val attacker = event.attacker
        if (damaged.isDischarging("凝望反制") && attacker is Guardian) {
            event.damage = maxOf(event.damage - 6.0, 0.1)
        }
    }

    val qteing = mutableMapOf<UUID, ItemStack?>()

    @SubscribeEvent(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun fish(event: PlayerFishEvent) {
        val player = event.player
        val uuid = player.uniqueId

        val hook = event.hook
        val world = hook.world
        val loc = hook.location
        val chunk = hook.location.chunk

        val today = ZonedDateTime.now().dayOfMonth
        val date = chunk["fish_date", PersistentDataType.INTEGER] ?: run {
            chunk["fish_date", PersistentDataType.INTEGER] = today
            -1
        }
        if (date != today) {
            chunk["fish_amount", PersistentDataType.INTEGER] = 0
            chunk["fish_date", PersistentDataType.INTEGER] = today
        }
        val dailyAmount = chunk["fish_amount", PersistentDataType.INTEGER] ?: 0

        if (qteing.containsKey(uuid)) {
            val item = if (dailyAmount < 12) qteing[uuid] else ItemStack(TRASH.types.random())
            if (item == null) {
                event.isCancelled = true
                return
            } else {
                if (dailyAmount >= 11)
                    player.sendMessage("§b繁星工坊 §7>> 这个区块的鱼钓光啦，明天再来吧？")
                chunk["fish_amount", PersistentDataType.INTEGER] = dailyAmount + 1

                qteing.remove(uuid)
                hook.remove()
                if (player.isDischarging("收获涛声")) {
                    player.finish("收获涛声")
                    val spellLevel = player.spellLevel("收获涛声")
                    var amount = 1
                    if (spellLevel == 3 && Math.random() <= 0.5) {
                        amount += 1
                    }
                    if (CaughtRarity.getRarity(item) == FISH) {
                        item.amount += amount
                        player.sendMessage("§a生涯系统 §7>> §a技能 ${display("收获涛声")} §7使得本次钓上额外多§a${amount}条§7鱼")
                    }
                }

                if (item.type == Material.POTION) {
                    item.modifyMeta<PotionMeta> {
                        this.basePotionData = PotionData(PotionType.WATER)
                    }
                }

                val dropped = world.dropItem(loc, item)
                val direction = player.eyeLocation.subtract(loc).toVector().normalize()
                submit {
                    dropped.pickupDelay = 0
                    dropped.velocity = direction
                }
                var exp = floor(1 + 6 * Math.random()).roundToInt()
                if (player.meetRequirement("授人以渔"))
                    exp += 6
            }
        }

        val hooked = event.caught ?: return
        if (hooked !is Item) return
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        event.isCancelled = true

        val caught = hooked.itemStack
        val rarity = CaughtRarity.getRarity(caught)

        val spellLevel = player.spellLevel("垂钓熟手")
        val difficulty = run {
            if (spellLevel <= 0)
                return@run when (rarity) {
                    TRASH -> QTEProvider.QTEDifficulty.CHAOS
                    FISH -> QTEProvider.QTEDifficulty.GLITCH
                    TREASURE -> QTEProvider.QTEDifficulty.BETA
                }
            if (spellLevel == 1 || spellLevel == 2)
                return@run when (rarity) {
                    TRASH -> QTEProvider.QTEDifficulty.HARD
                    FISH -> QTEProvider.QTEDifficulty.CHAOS
                    TREASURE -> QTEProvider.QTEDifficulty.GLITCH
                }
            return@run when (rarity) {
                TRASH -> QTEProvider.QTEDifficulty.HARD
                FISH -> QTEProvider.QTEDifficulty.HARD
                TREASURE -> QTEProvider.QTEDifficulty.CHAOS
            }
        }
        val type = when (spellLevel) {
            2 -> QTEProvider.QTEType.TWO_TIMES
            3 -> QTEProvider.QTEType.THREE_TIMES
            else -> QTEProvider.QTEType.ONE_TIME
        }

        var end = false
        qteing[uuid] = null
        submit(period = 20L) {
            if (end) cancel()
            world.spawnParticle(Particle.WATER_SPLASH, loc.add(0.0, 0.5, 0.0), 5)
            hook.velocity = Vector(0.0, -0.05, 0.0)
        }

        QTEProvider.sendQTE(player, difficulty, type, {
            end = true
            qteing.remove(uuid)
            sendMessage(
                when (it) {
                    QTEProvider.QTEResult.ACCEPTED -> {
                        qteing[uuid] = caught
                        "§b繁星工坊 §7>> 校准成功，收钩以获取物品"
                    }

                    QTEProvider.QTEResult.REJECTED -> {
                        qteing[uuid] = ItemStack((rarity.worse ?: TRASH).types.random())
                        "§b繁星工坊 §7>> 校准失败，上钩物品品质降级，收钩以获取物品"
                    }

                    QTEProvider.QTEResult.UNABLE -> {
                        qteing.remove(uuid)
                        return@sendQTE
                    }
                }
            )
        }, "§e上钩", "§7请完成校准收回钓钩，否则战利品品质将§c降级")
    }

    enum class CaughtRarity(val types: List<Material>, val worse: CaughtRarity?) {
        TRASH(
            listOf(
                Material.LILY_PAD,
                Material.BOWL,
                Material.FISHING_ROD,
                Material.LEATHER_BOOTS,
                Material.ROTTEN_FLESH,
                Material.STICK,
                Material.STRING,
                Material.POTION,
                Material.BONE,
                Material.INK_SAC,
                Material.TRIPWIRE_HOOK
            ), null
        ),
        FISH(
            listOf(
                Material.SALMON,
                Material.TROPICAL_FISH,
                Material.PUFFERFISH,
                Material.COD
            ), TRASH
        ),
        TREASURE(
            listOf(
                Material.BOW,
                Material.ENCHANTED_BOOK,
                Material.FISHING_ROD,
                Material.NAME_TAG,
                Material.NAUTILUS_SHELL,
                Material.SADDLE
            ), FISH
        );

        companion object {
            fun getRarity(item: ItemStack): CaughtRarity {
                val type = item.type
                if (type == Material.FISHING_ROD) {
                    return if (item.enchantments.isNotEmpty())
                        TREASURE
                    else TRASH
                }
                values().forEach {
                    if (it.types.contains(type))
                        return it
                }
                return TRASH
            }
        }
    }
}