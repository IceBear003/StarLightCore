package world.icebear03.starlight.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import java.util.*

fun Player.hasBlockAside(type: Material, range: Int = 3): Boolean {
    return this.location.hasBlockAside(type, range)
}

fun Player.hasBlockAside(types: Collection<Material>, range: Int = 3): Boolean {
    return this.location.hasBlockAside(types, range)
}

fun Player.secondLived(): Int {
    return getStatistic(Statistic.TIME_SINCE_DEATH) / 20
}

fun UUID.toName(): String {
    return Bukkit.getOfflinePlayer(this).name!!
}

fun LivingEntity.effect(type: PotionEffectType, duration: Int, level: Int = 1) {
    this.addPotionEffect(type.createEffect(duration * 20, level - 1))
}