package world.icebear03.starlight.career.core

import taboolib.module.chat.colored
import world.icebear03.starlight.utils.toRoman

abstract class Basic {
    abstract val name: String //名字，即ID
    abstract val skull: String //展示头颅
    abstract val color: String //HEX颜色

    fun display(level: Int? = null): String {
        return (color + name).colored() + if (level != null) " ${level.toRoman()}" else ""
    }
}