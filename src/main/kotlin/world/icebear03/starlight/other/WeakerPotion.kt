package world.icebear03.starlight.other

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.takeItem
import world.icebear03.starlight.utils.toRoman

object WeakerPotion {

    @SubscribeEvent
    fun drop(event: EntityDropItemEvent) {
        val item = event.itemDrop.itemStack
        if (item.type != Material.POTION)
            return
        event.isCancelled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun drink(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item
        if (item.type != Material.POTION)
            return

        event.isCancelled = true
        player.inventory.takeItem(1) {
            it == item
        }
        player.inventory.addItem(ItemStack(Material.GLASS_BOTTLE))
        player.drink(item)
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun splash(event: PotionSplashEvent) {
        val item = event.potion.item
        val entity = event.potion
        event.isCancelled = true
        entity.world.spawnParticle(Particle.WATER_SPLASH, entity.location, 3)
        event.affectedEntities.forEach {
            it.drink(item)
        }
    }

    fun LivingEntity.drink(potion: ItemStack) {
        val meta = potion.itemMeta as PotionMeta
        if (!meta.hasCustomEffects()) {
            val data = meta.basePotionData
            val potionType = data.type
            val time = if (potionType.isInstant) 0 else {
                if (data.isExtended) 8 * 60 * 20 else 3 * 60 * 20
            }
            val level = if (data.isUpgraded) 2 else 1
            val effect = potionType.effectType?.createEffect(time, level - 1) ?: return
            addPotionEffect(effect)
        } else
            meta.customEffects.forEach {
                if (hasPotionEffect(it.type)) {
                    val origin = getPotionEffect(it.type)!!
                    if (origin.amplifier >= it.amplifier && origin.duration >= it.duration) {
                        return@forEach
                    }
                }
                this.addPotionEffect(it)
            }
    }


    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun fuel(event: BrewingStandFuelEvent) {
        event.fuelPower = event.fuelPower / 3
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun brew(event: BrewEvent) {
        val material = event.contents.ingredient?.type ?: return
        event.results.forEach { item ->
            if (item.type == Material.AIR)
                return@forEach
            item.modifyMeta<PotionMeta> {
                val data = basePotionData
                val potion = this.basePotionData.type
                val type = potion.effectType ?: return@modifyMeta
                if (potion.isInstant) {
                    addCustomEffect(type.createEffect(0, if (data.isUpgraded) 1 else 0), true)
                } else {
                    val base = getNewTime(type)
                    var time = 20 * if (data.isExtended && potion.isExtendable) (1.5 * base).toInt() else base
                    val amplifier = if (data.isUpgraded && potion.isUpgradeable) {
                        time /= 2
                        1
                    } else 0
                    addCustomEffect(type.createEffect(time, amplifier), true)
                }

                addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                lore = listOf("&8| &7效果列表:".colored()) + customEffects.map {
                    "    &7|—— &b${it.type.name} ${(it.amplifier + 1).toRoman()} &a${it.duration / 20}s".colored()
                }
            }
        }
    }

    fun getNewTime(type: PotionEffectType): Int {
        return when (type) {
            PotionEffectType.POISON -> 16
            PotionEffectType.WEAKNESS -> 24
            PotionEffectType.SLOW -> 24
            PotionEffectType.SLOW_DIGGING -> 24
            else -> 40
        }
    }
}