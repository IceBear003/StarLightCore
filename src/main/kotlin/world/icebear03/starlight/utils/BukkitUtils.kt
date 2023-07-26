package world.icebear03.starlight.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType

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