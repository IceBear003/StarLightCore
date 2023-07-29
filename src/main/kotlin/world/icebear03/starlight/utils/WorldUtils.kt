package world.icebear03.starlight.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.abs


fun Block.loadPdc(): MutableMap<String, String> {
    val loc = location
    val chunk = chunk
    val pdc = chunk.persistentDataContainer

    val key = loc.toKey()
    val data = pdc.get(key, PersistentDataType.STRING) ?: return mutableMapOf()

    val resultType = object : TypeToken<MutableMap<String, String>>() {}.type
    return Gson().fromJson(data, resultType)
}

fun Block.savePdc(data: MutableMap<String, String>) {
    val loc = location
    val chunk = chunk
    val pdc = chunk.persistentDataContainer

    val key = loc.toKey()
    if (data.isEmpty())
        pdc.remove(key)
    else
        pdc.set(key, PersistentDataType.STRING, Gson().toJson(data))
}

fun Location.toKey(): NamespacedKey {
    return NamespacedKey.minecraft("${world?.name}_${blockX}_${blockY}_$blockZ")
}

fun Location.horizontalDistance(to: Location): Double {
    val tmp = this.clone()
    tmp.y = to.y
    return tmp.distance(to)
}

fun Location.verticalDistance(to: Location): Double {
    return abs(this.y - to.y)
}

fun getSurroundings(chunks: List<Chunk>): List<Chunk> {
    val world = chunks[0].world
    val graph = mutableMapOf<Chunk, Vector<Chunk>>()
    chunks.forEach { chunk ->
        val x = chunk.x
        val z = chunk.z
        listOf(
            world.getChunkAt(x - 1, z),
            world.getChunkAt(x, z + 1),
            world.getChunkAt(x + 1, z),
            world.getChunkAt(x, z - 1)
        ).forEach { nearby ->
            graph.putIfAbsent(nearby, Vector())
            graph[nearby]!! += chunk
        }
    }
    return chunks.filter { chunk ->
        graph[chunk]!!.size < 4
    }
}