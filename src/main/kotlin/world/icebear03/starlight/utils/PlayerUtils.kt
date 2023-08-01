package world.icebear03.starlight.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.platform.util.countItem
import taboolib.platform.util.takeItem
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

fun Player.takeItem(amount: Int = 1, matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    if (inventory.countItem(matcher) >= amount) {
        inventory.takeItem(amount, matcher)
        return true
    }
    return false
}

fun Player.realDamage(amount: Double, who: Entity? = null) {
    health = maxOf(0.1, health - amount)
    damage(0.5, who)
}