package world.icebear03.starlight.utils

import taboolib.common.util.replaceWithOrder

object MathUtils {
    fun numToRoman(num: Int, ignoreI: Boolean, hasPreviousBlank: Boolean = false): String {
        if (num < 1)
            return ""
        if (num == 1 && ignoreI)
            return ""
        var number = num
        var rNumber = StringBuilder()
        val aArray = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val rArray = arrayOf(
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X",
            "IX", "V", "IV", "I"
        )
        if (number > 3999) {
            rNumber = StringBuilder("-1")
        } else {
            for (i in aArray.indices) {
                while (number >= aArray[i]) {
                    rNumber.append(rArray[i])
                    number -= aArray[i]
                }
            }
        }
        return if (hasPreviousBlank) " $rNumber" else rNumber.toString()
    }
}

fun Int.toRoman(ignoreI: Boolean = false): String {
    return MathUtils.numToRoman(this, ignoreI)
}

val DEFAULT_TIME_KEY = mapOf(
    "年" to 365 * 24 * 60 * 60, "月" to 30 * 24 * 60 * 60, "天" to 24 * 60 * 60, "时" to 60 * 60, "分" to 60, "秒" to 1
)

fun Int.secToFormattedTime(): String {
    var second = this
    var cache: Int
    val result = StringBuilder()

    if (second <= 0) {
        return "{0}{1}".replaceWithOrder(second, "秒")
    }

    for (entry in DEFAULT_TIME_KEY.entries) {
        cache = second % entry.value
        val number = (second - cache) / entry.value
        second -= number * entry.value
        if (number != 0) {
            result.append("{0}{1}".replaceWithOrder(number, entry.key))
        }
    }
    return result.toString().trim { it <= ' ' }
}