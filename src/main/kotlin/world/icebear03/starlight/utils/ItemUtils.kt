package world.icebear03.starlight.utils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.SkullMeta
import org.serverct.parrot.parrotx.function.textured
import taboolib.platform.util.modifyMeta

fun ItemStack?.getEnchants(): Map<Enchantment, Int> {
    this ?: return mutableMapOf()
    if (type == Material.ENCHANTED_BOOK)
        return (itemMeta as EnchantmentStorageMeta).storedEnchants
    return enchantments
}

fun ItemStack.skull(skull: String): ItemStack {
    return if (skull.length <= 20) modifyMeta<SkullMeta> { owner = skull }
    else textured(skull)
}