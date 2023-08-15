package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.sendActionBar
import world.icebear03.starlight.tool.mechanism.QTEProvider.QTEType.*
import java.util.*
import kotlin.math.roundToInt

// POWERED BY 白熊_IceBear
// GPL v3.0
object QTEProvider {

    val waitingQTEs = mutableMapOf<UUID, MutableList<() -> Unit>>()
    val responseMap = mutableMapOf<UUID, Boolean>()

    fun sendQTE(
        player: Player,
        difficulty: QTEDifficulty,
        type: QTEType,
        function: Player.(result: QTEResult) -> Unit,
        title: String = "",
        subtitle: String = "在读条§e合适时机§b下蹲§7或§b交换副手物品"
    ) {
        //如果正在QTE了，就排队等
        if (isQTEing(player)) {
            waitingQTEs.putIfAbsent(player.uniqueId, mutableListOf())
            waitingQTEs[player.uniqueId]!! += { sendQTE(player, difficulty, type, function, title, subtitle) }
            return
        }

        player.sendTitle(title, subtitle)

        val uuid = player.uniqueId
        responseMap[uuid] = false

        //格式
        val qteFormat = "&7>&f {bar} &7< (容错&e{chance}次&7)"
        val resultFormat = "&7>&f {result} &7<"
        //QTE总长度，如果要修改此处，请一起修改下方的intervalStart计算公式，否则有可能超下标
        val total = 100

        //QTE读条速度（负相关）
        val period = difficulty.period
        //QTE成功区间长度
        val interval = difficulty.interval

        //QTE成功区间开头的index
        var intervalStart = ((0.2 + Math.random() * 0.6) * total).roundToInt()

        //QTE总时长
        val ticks = when (type) {
            ONE_TIME -> 1 * total * period
            TWO_TIMES -> 2 * total * period
            THREE_TIMES -> 3 * total * period
        }

        //失败次数
        var failTime = 0
        //计时器
        var tot = 0
        var lastTickChance = 1
        submit(period = 1L) {
            //完成QTE，并进行下一步操作
            fun finish(result: QTEResult? = null) {
                result?.let {
                    function.invoke(player, result)
                    player.sendActionBar(
                        resultFormat.replace(
                            "{result}",
                            if (it == QTEResult.ACCEPTED) "§a✔" else "§c✘"
                        ).colored()
                    )
                } ?: function.invoke(player, QTEResult.UNABLE)
                responseMap.remove(uuid)
                //延迟，让玩家看到效果
                submit(delay = 20L) {
                    waitingQTEs[player.uniqueId]?.let { qtes ->
                        if (qtes.isNotEmpty()) {
                            qtes[0].invoke()
                            qtes.removeAt(0)
                        }
                    }
                }
                cancel()
            }

            //目前是第几次
            val chance = tot / (total * period) + 1

            //重置校准位置
            if (chance != lastTickChance) {
                lastTickChance = chance
                intervalStart = ((0.1 + Math.random() * 0.6) * total).roundToInt()
            }

            //玩家不能完成QTE了
            if (player.isDead || !player.isOnline) {
                finish()
                return@submit
            }

            //时间超了，或者玩家放弃了
            tot += difficulty.mag
            if (tot >= ticks || !responseMap.containsKey(uuid)) {
                finish(QTEResult.REJECTED)
                return@submit
            }

            //玩家响应了
            if (responseMap[uuid]!!) {
                val intervalThisTime = when (chance) {
                    1 -> intervalStart
                    2 -> total * 2 - intervalStart - interval
                    3 -> total * 2 + intervalStart
                    else -> 0
                } * period

                if (tot in intervalThisTime..intervalThisTime + interval * period) {
                    finish(QTEResult.ACCEPTED)
                    return@submit
                } else {
                    failTime += 1
                    if (failTime >= type.time) {
                        finish(QTEResult.REJECTED)
                        return@submit
                    }
                    responseMap[uuid] = false
                }
            }

            //发送下一单位的QTE状态条
            if (tot % period == 0) {
                var bar = ""
                repeat(100) {
                    val isPassed = when (chance) {
                        1 -> it <= tot / period
                        2 -> it > 2 * total - tot / period
                        3 -> it <= tot / period - total * 2
                        else -> false
                    }
                    bar += if (isPassed) SymbolType.PASSED.colored[chance - 1]
                    else if (it in intervalStart..intervalStart + interval) SymbolType.INTERVAL.colored[chance - 1]
                    else SymbolType.WAITING.colored[chance - 1]
                }
                player.sendActionBar(
                    qteFormat.replace("{bar}", bar).replace("{chance}", (type.time - failTime).toString()).colored()
                )
            }
        }
    }

    fun isQTEing(player: Player): Boolean {
        return responseMap.containsKey(player.uniqueId)
    }

    @SubscribeEvent
    fun shift(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (!player.isSneaking && isQTEing(player)) {
            responseMap[player.uniqueId] = true
            event.isCancelled = true
        }
    }

    @SubscribeEvent
    fun swap(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (isQTEing(player)) {
            responseMap[player.uniqueId] = true
            event.isCancelled = true
        }
    }

    enum class SymbolType(val colored: List<String>) {
        WAITING(listOf("&7|", "&6|", "&c|")),
        INTERVAL(listOf("&e|", "&e|", "&e|")),
        PASSED(listOf("&6|", "&c|", "&4|"))
    }

    enum class QTEDifficulty(val period: Int, val interval: Int, val mag: Int, val easier: QTEDifficulty?) {
        EASY(1, 30, 1, null),
        HARD(1, 15, 1, EASY),
        CHAOS(1, 10, 1, HARD),
        GLITCH(1, 7, 2, CHAOS),
        BETA(1, 4, 3, GLITCH)
    }

    //给玩家几次机会，增加容错率
    enum class QTEType(val time: Int) {
        ONE_TIME(1),
        TWO_TIMES(2),
        THREE_TIMES(3)
    }

    enum class QTEResult {
        ACCEPTED, //通过
        REJECTED, //不通过
        UNABLE //无法响应（退出了服务器，死了等）
    }
}