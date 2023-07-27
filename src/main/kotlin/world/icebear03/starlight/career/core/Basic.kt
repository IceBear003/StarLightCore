package world.icebear03.starlight.career.core

import taboolib.module.chat.colored
import world.icebear03.starlight.utils.toRoman

abstract class Basic {
    abstract val name: String //名字，即ID
    abstract val skull: String //展示头颅
    abstract val color: String //HEX颜色

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }

    fun display(level: Int? = null): String {
        return (color + name).colored() + if (level != null) " ${level.toRoman()}" else ""
    }
}