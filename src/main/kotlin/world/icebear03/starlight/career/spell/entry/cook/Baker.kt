package world.icebear03.starlight.career.spell.entry.cook

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import taboolib.platform.util.giveItem
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.*
import kotlin.math.floor
import kotlin.math.roundToInt

object Baker {

    val bakery = listOf(Material.BREAD, Material.CAKE, Material.COOKIE, Material.PUMPKIN_PIE)
    val soup = listOf(Material.SUSPICIOUS_STEW, Material.MUSHROOM_STEW, Material.RABBIT_STEW)
    val food = bakery + soup
    fun initialize() {
        food.addLowestListener(HandlerType.CRAFT) { _, player, type ->
            val rate = when (player.spellLevel("预热烤箱")) {
                -1 -> 0.5
                0 -> 0.75
                1 -> 0.9
                2, 3 -> 1.0
                else -> 0.5
            }
            if (Math.random() > rate && type != Material.SUSPICIOUS_STEW) {
                false to "合成食品失败，解锁 §e职业分支 §7${display("烘焙师")} §7可以提高成功率"
            } else true to null
        }

        food.addHighListener(HandlerType.CRAFT) { _, player, type ->
            if (player.meetRequirement("预热烤箱", 3) && Math.random() <= 0.1) {
                player.giveItem(ItemStack(type))
                "合成烘培/汤煲食品时额外获得了产物"
            } else null
        }

        Material.CAKE.addHighListener(HandlerType.CRAFT) { _, player, _ ->
            if (player.meetRequirement("烘焙师", 0)) {
                player.giveItem(ItemStack(Material.CAKE))
                "合成蛋糕时额外获得了产物"
            } else null
        }

        "文火慢炖".discharge { name, _ ->
            "${display(name)} §7释放成功，下次合成汤煲食品时获得额外随机产物"
        }.finish { _, _ ->
            "合成汤煲食品时获得了额外产物"
        }

        soup.addHighListener(HandlerType.CRAFT) { _, player, type ->
            val level = player.spellLevel("文火慢炖")
            if (player.isDischarging("文火慢炖")) {
                player.giveItem(ItemStack(soup.random()))
                if (level >= 3 && type == Material.SUSPICIOUS_STEW) {
                    repeat(2) {
                        val stew = ItemStack(Material.SUSPICIOUS_STEW).modifyMeta<SuspiciousStewMeta> {
                            clearCustomEffects()
                            val random = PotionEffectType.values().random()
                            val time = if (random.isInstant) 0 else 200
                            addCustomEffect(PotionEffect(random, time, 0), true)
                        }
                        player.giveItem(stew)
                    }
                }
                player.finish("文火慢炖")
            }
            null
        }

        "甜点派对".discharge { name, level ->
            val loc = location
            var amount = getNearbyEntities(4.0, 4.0, 4.0).filterIsInstance<Player>().size + 1
            var max = level + 1
            val per = if (level == 1) 3 else 2
            while (max > 0 && amount > 0) {
                amount -= per
                max -= 1
                val tmp = loc.clone().add(-4.0 + 8.0 * Math.random(), -4.0 + 8.0 * Math.random(), -4.0 + 8.0 * Math.random())
                val position = world.getHighestBlockAt(tmp).location
                position.add(0, 1, 0)
                position.block.type = Material.CAKE
                world.spawnParticle(Particle.VILLAGER_HAPPY, position.clone().add(-0.5, 0.5, 0.5), 1)
            }
            finish(name)
            "召唤了一些蛋糕，甜点派对开始咯"
        }.finish { _, _ ->
            null
        }

        Material.COOKIE.addHighListener(HandlerType.CRAFT) { event, player, _ ->
            val craftEvent = event as CraftItemEvent
            val item = craftEvent.currentItem!!
            if (player.meetRequirement("幸运曲奇")) {
                craftEvent.currentItem = item.modifyMeta<ItemMeta> {
                    setDisplayName("&{#ffd965}幸运曲奇".colored())
                    lore = listOf("§8| ${"&{#426AB3}".colored()}安迪§7和§f白熊§7钦点的曲奇", "§8| §7吃下去有奇效")
                    this["fortune_cookie", PersistentDataType.INTEGER] = 0
                }
            }
            null
        }
    }

    val prays = listOf(
        "愿你被这个世界温柔以待，躲不过的惊吓都只是一场虚惊，收到的欢喜从无空欢喜。",
        "愿你所到之处皆为热土，愿你所遇之人皆为挚友；愿你余生不负忧，自在如风常欢笑。",
        "祝你不用奔赴大海，也能春暖花开。祝你不用颠沛流离，也能遇到陪伴。祝你不用熬过黑夜，已经等到晚安。如果这些都很难，祝你平平安安。",
        "愿所有的欢乐都陪伴着你，仰首是春，俯首是秋；愿所有的幸福都追随着你，月圆是画，月缺是诗。",
        "愿你走完山水万城 仍与理想重逢。",
        "平安喜樂 萬事勝意 祝你 祝我 祝我們。",
        "愿你一路多些坦途，少些曲折，多些喜悦，少些愁苦。",
        "我希望你读很多书，走很远的路。我希望你爱很多人，也被很多人爱。我希望你走过人山人海，也遍览山河湖海。我希望你看纸质书，送手写的祝福。我要你独立坚强温暖明亮，我要你在这寡淡的世上，深情的活。",
        "祝所求皆如愿，所行皆坦途。万物更新，往事清零，旧疾当愈，长安常安。",
        "我祝你万事胜意吧！万事胜意的意思是，一切结果都比你当初想象的，好那么一点点。这是我最后的祝福，我只送给你。",
        "愿一生努力，一生被爱，想要的都拥有，得不到的都释怀。",
        "愿少年，乘风破浪，他日勿忘化雨功。",
        "吾以过客之名，祝汝岁岁平安。",
        "愿神明偏爱，一切从欢；愿此生顺遂，所求皆所愿。",
        "有人并肩很好，自己一个人时也不要太落寞，每一步脚踏实地走好了，生活是在自己过，不是为了谁才去好好生活，所以祝你也祝我自己“一个人时也能过上精彩纷呈的人生”。",
        "碎碎念念，岁岁年年。愿你看过落日长河，仍能明亮如初；历尽千帆，仍能保持赤诚。加油，要是坚持不下去了，我会鼓励你的。",
        "我筑山川，我筑明月，我祝你梦想成真。",
        "虽然没有最好的祝福，但有最好的我们。",
        "我祝福你，愿你经得起长久的离别、种种考验、吉凶未卜的折磨、漫长的昏暗的路程。依照你的意愿安排生活吧，只要你觉得好就行。",
        "万事胜意，岁岁安安，你会像太阳一样，有起有落而不失光彩，祝你开心，无论何时，无论和谁。",
    )
    val prayMedal = ItemStack(Material.PLAYER_HEAD)
        .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmJkZjlmYjdmMmFjNTk4N2VjZmJkMTczZThiOTcxMWQ5OWI1ODExNmY4Y2E0ZDc3ZDBhZjhiMzg5ZiJ9fX0=")
        .modifyMeta<ItemMeta> {
            setDisplayName("§e幸运之星")
            lore = listOf(
                "§8§o 繁星与你，带着这些祝福，前行不止.",
                "§7",
                "§8| §7获得条件: §b收集到所有幸运曲奇祝福语"
            )
        }

    @SubscribeEvent
    fun eat(event: FoodLevelChangeEvent) {
        val item = event.item ?: return
        val type = item.type
        val player = event.entity as Player
        if (player.meetRequirement("烘焙师", 0) && food.contains(type)) {
            event.foodLevel += 2
        }

        if (type == Material.COOKIE) {
            val meta = item.itemMeta ?: return
            if (meta.has("fortune_cookie", PersistentDataType.INTEGER)) {
                if (Math.random() <= 0.2)
                    player.effect(PotionEffectType.LUCK, 30, 1)
                if (Math.random() <= 0.05)
                    player.effect(PotionEffectType.LUCK, 30, 2)
                if (Math.random() <= 0.001)
                    player.giveItem(ItemStack(Material.DIAMOND))
                val index = floor(prays.size * Math.random()).roundToInt()
                val pray = prays[index]
                player.sendMessage("§6幸运曲奇 §7>> ${"&{#426AB3}".colored()}安迪§7&§f白熊 §7> $pray")
                var current = player["pray_collected_amount", PersistentDataType.INTEGER_ARRAY] ?: listOf<Int>().toIntArray()
                if (!current.contains(index)) {
                    current += index
                    if (current.size == 20) {
                        player.sendMessage("§b繁星工坊 §7>> 祝福收集完毕，纪念品已发送至背包")
                        player.giveItem(prayMedal)
                    } else {
                        player.sendMessage("§b繁星工坊 §7>> 祝福当前已收集 §e${current.size}§7/§a20")
                    }
                }
                player["pray_collected_amount", PersistentDataType.INTEGER_ARRAY] = current
            }
        }
    }

    @SubscribeEvent
    fun attack(event: EntityDamageByEntityEvent) {
        val player = event.damager
        if (player !is Player)
            return
        if (!player.meetRequirement("法棍"))
            return

        val damaged = event.entity
        if (damaged !is LivingEntity)
            return

        val type = player.inventory.itemInMainHand.type
        if (type == Material.BREAD && player.attackCooldown >= 0.9f) {
            damaged.effect(PotionEffectType.CONFUSION, 15, 2)
            damaged.effect(PotionEffectType.DARKNESS, 5, 1)
            if (Math.random() <= 0.2) {
                damaged.realDamage(2.0, player)
                damaged.health = maxOf(0.02, damaged.health - 2.0)
                damaged.damage(0.1, player)
            }
        }
    }

    @SubscribeEvent
    fun interact(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (!player.meetRequirement("糖霜"))
            return

        if (player.foodLevel >= 20)
            return

        if (item.type == Material.SUGAR) {
            val amount = item.amount
            if (amount == 1)
                item.type = Material.AIR
            else item.amount = amount - 1

            player.foodLevel = player.foodLevel + 1
        }
    }
}