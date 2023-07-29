package world.icebear03.starlight.utils

import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

fun <T, Z> ItemMeta.get(key: String, type: PersistentDataType<T, Z>): Z? {
    return persistentDataContainer.get(NamespacedKey.minecraft(key), type)
}

fun <T, Z : Any> ItemMeta.set(key: String, type: PersistentDataType<T, Z>, value: Z) {
    persistentDataContainer.set(NamespacedKey.minecraft(key), type, value)
}


fun <T, Z : Any> ItemMeta.has(key: String, type: PersistentDataType<T, Z>): Boolean {
    return persistentDataContainer.has(NamespacedKey.minecraft(key), type)
}