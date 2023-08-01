package world.icebear03.starlight.utils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

fun ItemStack?.getEnchants(): Map<Enchantment, Int> {
    this ?: return mutableMapOf()
    if (type == Material.ENCHANTED_BOOK)
        return (itemMeta as EnchantmentStorageMeta).storedEnchants
    return enchantments
}