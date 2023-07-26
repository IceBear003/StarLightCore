package world.icebear03.starlight.stamina

import org.bukkit.entity.Player
import world.icebear03.starlight.loadStaminaData
import java.util.*

data class Stamina(
    var ownerId: UUID,
    var stamina: Double = 1800.0
) {

    //死了
    fun remake(): Stamina {
        stamina = 1800.0
        return this
    }

    fun addStamina(amount: Double) {
        stamina = minOf(stamina + amount, 1800.0)
    }

    fun setStamina(amount: Double) {
        stamina = minOf(1800.0, maxOf(0.0, amount))
    }

    fun takeStamina(amount: Double) {
        stamina = maxOf(stamina + amount, 0.0)
    }
}

fun Player.addStamina(amount: Double) {
    loadStaminaData(this).addStamina(amount)
}

fun Player.takeStamina(amount: Double) {
    loadStaminaData(this).takeStamina(amount)
}

fun Player.setStamina(amount: Double) {
    loadStaminaData(this).setStamina(amount)
}