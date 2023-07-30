package world.icebear03.starlight.station.core

import taboolib.common5.format
import taboolib.module.chat.colored
import kotlin.math.max
import kotlin.math.min

data class Stamina(var stamina: Double = 1800.0) {
    fun addStamina(amount: Double) {
        stamina = min(stamina + amount, 1800.0)
    }

    fun takeStamina(amount: Double) {
        stamina = max(stamina - amount, 0.0)
    }

    fun set(amount: Double) {
        stamina = max(min(amount, 1800.0), 0.0)
    }

    fun display(): String {
        if (stamina in 1600.0..1800.0)
            return "&{#18fe00}".colored() + stamina.format(1)
        if (stamina in 1000.0..1600.0)
            return "&{#a3fe00}".colored() + stamina.format(1)
        if (stamina in 750.0..1000.0)
            return "&{#eafe00}".colored() + stamina.format(1)
        if (stamina in 500.0..750.0)
            return "&{#fecc00}".colored() + stamina.format(1)
        if (stamina in 250.0..500.0)
            return "&{#fe7100}".colored() + stamina.format(1)
        if (stamina in 0.0..250.0)
            return "&{#fe0000}".colored() + stamina.format(1)
        return "§7" + stamina.format(1)
    }
}