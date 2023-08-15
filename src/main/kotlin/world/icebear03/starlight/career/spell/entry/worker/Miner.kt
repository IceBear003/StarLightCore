package world.icebear03.starlight.career.spell.entry.worker

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.navigation.Fluid
import taboolib.module.navigation.Fluid.Companion.getFluid
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.getBlockAside
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.skull
import java.util.*


object Miner {

    val ores = Material.values().filter {
        it.toString().contains("_ORE")
    }

    val stoneBlocks = listOf(
        Material.STONE,
        Material.COBBLESTONE,
        Material.MOSSY_COBBLESTONE,
        Material.GRANITE,
        Material.DIORITE,
        Material.ANDESITE,
        Material.CALCITE,
        Material.TUFF,
        Material.DEEPSLATE,
        Material.COBBLED_DEEPSLATE,
        Material.SANDSTONE,
        Material.RED_SANDSTONE,
        Material.BLACKSTONE,
        Material.NETHERRACK,
        Material.CRIMSON_NYLIUM,
        Material.WARPED_NYLIUM,
        Material.END_STONE,
        Material.BASALT,
        Material.OBSIDIAN,
        Material.CRYING_OBSIDIAN
    ) + Material.values().filter {
        val string = it.toString()
        (string.contains("BRICKS") || string.contains("_WALL") ||
                string.contains("_STAIRS") || string.contains("_SLAB")) &&
                !Lumberjack.woodenBlocks.contains(it) && !string.contains("POLISH") &&
                !string.contains("CUT_") && !string.contains("CHISELED_")
    }

    val colors = mutableMapOf(
        Material.DIAMOND_ORE to ChatColor.AQUA,
        Material.DEEPSLATE_DIAMOND_ORE to ChatColor.AQUA,
        Material.EMERALD_ORE to ChatColor.GREEN,
        Material.DEEPSLATE_EMERALD_ORE to ChatColor.GREEN,
        Material.GOLD_ORE to ChatColor.YELLOW,
        Material.DEEPSLATE_GOLD_ORE to ChatColor.YELLOW,
        Material.ANCIENT_DEBRIS to ChatColor.DARK_GRAY
    )
    val skulls = mutableMapOf(
        Material.DIAMOND_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYxNzhhM2U3MDU4YTEzYWFjNTBkMTkyOWE1NGQ0ODRkYmRmZmMwMzg1MzI0YzE5YjE4ZGZlMzQ0MWI0Yzc5ZSJ9fX0=",
        Material.DEEPSLATE_DIAMOND_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYxNzhhM2U3MDU4YTEzYWFjNTBkMTkyOWE1NGQ0ODRkYmRmZmMwMzg1MzI0YzE5YjE4ZGZlMzQ0MWI0Yzc5ZSJ9fX0=",
        Material.EMERALD_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU1ZGJjNDllODE4OGRhMWI5MzYwNzJkNDViYzdjMTNiNDJhMGE0YzA2MjRiMjEwNThhYTNmNTI4Mzk1NWI4ZSJ9fX0=",
        Material.DEEPSLATE_EMERALD_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU1ZGJjNDllODE4OGRhMWI5MzYwNzJkNDViYzdjMTNiNDJhMGE0YzA2MjRiMjEwNThhYTNmNTI4Mzk1NWI4ZSJ9fX0=",
        Material.GOLD_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEyZjI1NWRlOTU5OGQ1ZDJmOGViZmNmYzBlMTgzNTdhMzI4MWM1Y2JlMGEwMjZkYTMxZmE2YzRmMTQzN2M1In19fQ==",
        Material.DEEPSLATE_GOLD_ORE to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEyZjI1NWRlOTU5OGQ1ZDJmOGViZmNmYzBlMTgzNTdhMzI4MWM1Y2JlMGEwMjZkYTMxZmE2YzRmMTQzN2M1In19fQ==",
        Material.ANCIENT_DEBRIS to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyMGNlMzJmYWEyN2Q1MDQzNmQ1NWZkODg0ZDJlN2VmYjEwODcwZmY2MTllM2I1Y2M4MjczNjYyNzk3YzgyZCJ9fX0="
    )

    val armorStands = mutableListOf<UUID>()
    val coloredTeams = mutableMapOf<ChatColor, Team>()

    fun initialize() {
        addLimit(
            HandlerType.USE, "矿工" to 0,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
        )
        ores.addLowestListener(HandlerType.DROP_IF_BREAK) { _, player, _ ->
            if (!player.meetRequirement("矿工", 0) && Math.random() <= 0.5) {
                false to "挖掘矿物无掉落，解锁 ${display("矿工")} §7可以提高成功率"
            } else true to null
        }

        stoneBlocks.addHighListener(HandlerType.BREAK) { _, player, type ->
            val level = player.spellLevel("奋力挖掘")
            if (level >= 0) {
                val duration = if (type.toString().contains("OBSIDIAN")) 10 else 2
                player.effect(PotionEffectType.FAST_DIGGING, duration, level + 1)
            }
            null
        }

        "强力运镐".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，下次使用镐不消耗耐久值"
        }

        "不灭矿灯".discharge { name, level ->
            val intensity = 6 + level * 3
            var loc = location
            var lastType = loc.block.type
            submit(period = 5L) {
                val oldBlock = loc.block
                val newLoc = location
                val newBlock = newLoc.block

                if (!isDischarging(name)) {
                    if (oldBlock.type == Material.LIGHT)
                        oldBlock.type = lastType
                    cancel()
                    return@submit
                }

                if (oldBlock.location == newBlock.location)
                    return@submit

                val canPlace = newBlock.type == Material.WATER
                val waterLogged = newBlock.getFluid() == Fluid.WATER

                if (newBlock.type == Material.AIR || canPlace) {
                    if (oldBlock.type == Material.LIGHT)
                        oldBlock.type = lastType
                    loc = newLoc
                    lastType = newBlock.type
                    newBlock.type = Material.LIGHT

                    val light = newBlock.blockData as Light
                    light.level = intensity
                    light.isWaterlogged = waterLogged
                    newBlock.blockData = light
                }
            }
            "§a技能 ${display(name)} §7释放成功，一段时间内将会自带光源"
        }

        "地质学".discharge { name, _ ->
            finish(name)
            val locs = location.getBlockAside(
                8, Material.GOLD_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.ANCIENT_DEBRIS
            )
            if (locs.isNotEmpty()) {
                var distance = 100.0
                var result = locs[0]
                locs.forEach { loc ->
                    val tmp = loc.distance(location)
                    if (tmp <= distance) {
                        distance = tmp
                        result = loc
                    }
                }
                //高亮
                val block = result.block
                val loc = block.location
                val type = block.type
                val color = colors[type]!!
                val armorStand = block.world.spawnEntity(loc.add(0.5, -1.0, 0.5), EntityType.ARMOR_STAND) as ArmorStand

                val equipment = armorStand.equipment

                equipment?.helmet = ItemStack(Material.PLAYER_HEAD).skull(skulls[type]!!)
                armorStand.setBasePlate(false)
                armorStand.setArms(false)
                armorStand.isCollidable = false
                armorStand.isMarker = true
                armorStand.isInvulnerable = true
                armorStand.isInvisible = true
                armorStand.isGlowing = true
                armorStand.setAI(false)
                armorStand.setGravity(false)

                armorStands += armorStand.uniqueId
                coloredTeams[color]!!.addEntry(armorStand.uniqueId.toString())

                "§d顿悟 ${display(name)} §7释放成功，最近的高价值矿物已高亮"
            } else {
                "§d顿悟 ${display(name)} §7释放成功，但是附近貌似没有高品质矿物"
            }
        }

        submit(period = 20L) {
            onlinePlayers.filter { it.meetRequirement("自然矿洞勘探") }.forEach { player ->
                val loc = player.location
                val block = loc.block
                if (block.biome.toString().contains("CAVE")) {
                    player.effect(PotionEffectType.DAMAGE_RESISTANCE, 2, 1)
                }
            }
            armorStands.forEach { uid ->
                val armorStand = Bukkit.getEntity(uid) ?: return@forEach
                if (armorStand.ticksLived >= 600)
                    armorStand.remove()
            }
        }

        val manager = Bukkit.getScoreboardManager()
        val board = manager!!.mainScoreboard

        ChatColor.values().forEach { color ->
            if (board.getTeam("SL-" + color.name) == null) {
                val team: Team = board.registerNewTeam("SL-" + color.name)
                team.color = color
            }
            coloredTeams[color] = board.getTeam("SL-" + color.name)!!
        }
    }

    @SubscribeEvent
    fun damageItem(event: PlayerItemDamageEvent) {
        val player = event.player
        val item = event.item
        if (!item.type.toString().contains("_PICKAXE"))
            return
        if (player.isDischarging("强力运镐")) {
            player.finish("强力运镐")
            event.isCancelled = true
        }
    }
}