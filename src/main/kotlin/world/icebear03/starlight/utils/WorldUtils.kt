package world.icebear03.starlight.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.TNTPrimed
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.function.submit
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


fun Block.loadPdc(): MutableMap<String, String> {
    val loc = location
    val chunk = chunk

    val data = chunk[loc.toPDCString(), PersistentDataType.STRING] ?: return mutableMapOf()

    val resultType = object : TypeToken<MutableMap<String, String>>() {}.type
    return Gson().fromJson(data, resultType)
}

fun Block.savePdc(data: MutableMap<String, String>) {
    val loc = location
    val chunk = chunk

    if (data.isEmpty())
        chunk.remove(loc.toPDCString())
    else
        chunk[loc.toPDCString(), PersistentDataType.STRING] = Gson().toJson(data)
}

fun Location.toPDCString(): String {
    return "${world?.name}_${blockX}_${blockY}_$blockZ"
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

fun Chunk.chunksInCircle(radius: Int): List<Chunk> {
    val chunks = mutableListOf<Chunk>()
    for (xx in x - radius..x + radius) {
        val interval = radius - abs(xx - x)
        for (zz in z - interval + 1 until z + interval) {
            if ((xx.toDouble() - x).pow(2) + (zz.toDouble() - z).pow(2) <= radius)
                chunks.add(world.getChunkAt(xx, zz))
        }
    }
    return chunks
}

fun Location.toSavableString(): String {
    return "${world?.name}:$blockX:$blockY:$blockZ"
}

fun Location.hasBlockAside(types: Collection<Material>, range: Int = 3): Boolean {
    for (x in -range..range)
        for (y in -range..range)
            for (z in -range..range) {
                val newLoc = this.clone()
                newLoc.add(x, y, z)
                if (types.contains(newLoc.block.type))
                    return true
            }
    return false
}

fun Location.add(x: Int, y: Int, z: Int) {
    this.add(x.toDouble(), y.toDouble(), z.toDouble())
}

fun Location.hasBlockAside(type: Material, range: Int = 3): Boolean {
    for (x in -range..range)
        for (y in -range..range)
            for (z in -range..range) {
                val newLoc = this.clone()
                newLoc.add(x, y, z)
                if (newLoc.block.type == type)
                    return true
            }
    return false
}

fun String.toLocation(): Location {
    val split = split(":")
    return Location(
        Bukkit.getWorld(split[0]),
        split[1].toDouble(),
        split[2].toDouble(),
        split[3].toDouble()
    )
}

fun generateEdges(chunks: List<Chunk>, y: Double): List<Pair<Chunk, Chunk>> {
    if (chunks.isEmpty()) throw Exception()
    val graph = mutableMapOf<Chunk, Vector<Chunk>>()

    //生成一个单环连通双向图
    chunks.forEach { chunk ->
        val world = chunk.world
        val x = chunk.x
        val z = chunk.z
        graph.putIfAbsent(chunk, Vector())
        listOf(
            world.getChunkAt(x - 1, z),
            world.getChunkAt(x, z + 1),
            world.getChunkAt(x + 1, z),
            world.getChunkAt(x, z - 1)
        ).filter { chunks.contains(it) }.forEach { nearby ->
            graph.putIfAbsent(nearby, Vector())
            graph[nearby]!! += chunk
            graph[chunk]!! += nearby
        }
    }
    //图生成好啦
    //接下来搜索最大环，也就刷接通所有chunks的环
    //需要用到单向DFS深度优先搜索
    //现在我们随便选取一个起点
    val start = chunks[0]
    val visited = chunks.associateWith { false }.toMutableMap()
    val edges = mutableListOf<Pair<Chunk, Chunk>>()
    fun dfs(current: Chunk): Boolean {
        //如果连满了，回到起点了
        if (current == start && !visited.values.contains(false)) {
            return true
        }
        //本条搜搜已经访问过了
        if (visited[current]!!)
            return false
        //加标记
        visited[current] = true
        //搜点
        graph[current]!!.forEach { nearby ->
            val pair = current to nearby
            //尝试加边
            edges += pair
            if (dfs(nearby)) {
                //成了，返回
                return true
            } else {
                //本条搜索路线没成，标记还原，把边删掉
                visited[nearby] = false
                edges -= pair
            }
        }
        //全部搜完了还没有true，只能返回false了
        return false
    }

    val dfsResult = dfs(start)
    if (!dfsResult) {
        //TODO 你的区块没有连接成环，而是一条线呜呜呜
    }
//
//    graph.clear()
//    edges.forEach { (u, v) ->
//        graph.putIfAbsent(u, Vector())
//        graph.putIfAbsent(v, Vector())
//        graph[u]!! += v
//        graph[v]!! += u
//    }
//
//    val result = mutableListOf<Pair<Location, Location>>()
//
//    chunks.forEach { visited[it] = false }
//    fun connect(last: Chunk, current: Chunk) {
//        if (!visited.values.contains(false)) {
//            //连接头尾
//        }
//        if (visited[current]!!)
//            return
//        visited[current] = true
//        val next = graph[current]!![0]
//
//
//
//        connect(current, next)
//    }

    return edges
}

fun Location.shootPrimedTNT(velocity: org.bukkit.util.Vector, fuseTicks: Int = 100, breakBlocks: Boolean = false) {
    if (this.world == null)
        return
    val tnt = this.world!!.spawnEntity(this, EntityType.PRIMED_TNT) as TNTPrimed
    submit {
        tnt.velocity = velocity
        tnt.fuseTicks = fuseTicks
        tnt.isGlowing = true
        if (!breakBlocks) {
//            DemolitionistActive.tnts += tnt.uniqueId FIXME
        }
    }
}

fun Location.getBlockAside(range: Int, type: Material): List<Location> {
    val result = mutableListOf<Location>()
    for (x in -range..range)
        for (y in -range..range)
            for (z in -range..range) {
                val newLoc = this.clone()
                newLoc.add(x, y, z)
                if (newLoc.block.type == type)
                    result += newLoc
            }
    return result
}