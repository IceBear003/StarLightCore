package world.icebear03.starlight.tool.info

import org.bukkit.Bukkit
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.secToFormattedTime
import world.icebear03.starlight.utils.toName
import java.io.File
import java.util.*

object CareerChart {

    var chart = listOf<Pair<UUID?, Int>>()

    fun initialize() {
        releaseResourceFile("tool/chart.yml", false)
        val file = File(getDataFolder().absolutePath + "/tool/", "chart.yml")
        val config = Configuration.loadFromFile(file)
        for (i in 0 until 10) {
            chart += if (config.contains(i.toString())) UUID.fromString(config.getString("$i.uid")) to config.getInt("$i.time")
            else null to 0
        }

        submit(period = 20L) {
            val online = onlinePlayers.filter { !it.isOp }
                .map { it.uniqueId to (it["career_time", PersistentDataType.INTEGER] ?: 0) }
            val sorted = (chart.filter {
                it.first?.let { !Bukkit.getOfflinePlayer(it).isOnline } ?: true
            } + online).sortedBy { 1e9 - it.second }

            chart = sorted.subList(0, minOf(10, sorted.size))

            for (i in 0 until 10) {
                val pair = chart[i]
                if (pair.first != null) {
                    config["$i.uid"] = pair.first.toString()
                    config["$i.time"] = pair.second
                }
            }

            config.saveToFile(file)
        }
    }

    fun rank(index: Int): Pair<String, String> {
        val pair = chart[index]
        return if (pair.first == null) "虚位以待" to "N/A"
        else pair.first!!.toName() to pair.second.secToFormattedTime()
    }

    fun rankColor(index: Int): String {
        return when (index) {
            1 -> "<g:#6EABFF:#D076FF>"
            2 -> "&{#e18fff}"
            3 -> "&{#ff8fe1}"
            4, 5 -> "&{#ff8f8f}"
            6, 7, 8, 9, 10 -> "&{#ffd68f}"
            else -> "&e"
        }
    }
}