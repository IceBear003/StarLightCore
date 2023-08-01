package world.icebear03.starlight.tool.info

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryOpenEvent
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.SubscribeEvent

object SkullAdaptor {

    @SubscribeEvent
    fun open(event: InventoryOpenEvent) {
        val inv = event.inventory
        repeat(inv.size) {
            val item = inv.getItem(it) ?: return@repeat
            if (item.type != Material.PLAYER_HEAD) return@repeat
            val meta = item.itemMeta ?: return@repeat
            val name = meta.displayName ?: return@repeat
            if (name.contains("下一页"))
                item.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI3MWE0NzEwNDQ5NWUzNTdjM2U4ZTgwZjUxMWE5ZjEwMmIwNzAwY2E5Yjg4ZTg4Yjc5NWQzM2ZmMjAxMDVlYiJ9fX0=")
            if (name.contains("上一页"))
                item.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjllYTFkODYyNDdmNGFmMzUxZWQxODY2YmNhNmEzMDQwYTA2YzY4MTc3Yzc4ZTQyMzE2YTEwOThlNjBmYjdkMyJ9fX0=")
            if (name.contains("关闭"))
                item.textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=")

        }
    }
}