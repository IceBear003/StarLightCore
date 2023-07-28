package world.icebear03.starlight.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import world.icebear03.starlight.other.DeathStamp.deathStampKey
import java.util.*

fun Player.effect(type: PotionEffectType, duration: Int, level: Int = 1) {
    this.addPotionEffect(type.createEffect(duration * 20, level - 1))
}

fun Player.hasBlockAside(type: Material, range: Int = 3): Boolean {
    return this.location.hasBlockAside(type, range)
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

fun Player.hasBlockAside(types: Collection<Material>, range: Int = 3): Boolean {
    return this.location.hasBlockAside(types, range)
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

fun Player.secondLived(): Int {
    val pdc = this.persistentDataContainer
    val lastDeath = pdc.get(deathStampKey, PersistentDataType.LONG)!!
    return ((System.currentTimeMillis() - lastDeath) / 1000).toInt()
}

fun UUID.toName(): String {
    return Bukkit.getOfflinePlayer(this).name!!
}