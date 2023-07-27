package world.icebear03.starlight.career.core

import taboolib.module.chat.colored

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

    fun display(): String {
        return (color + name).colored()
    }
}