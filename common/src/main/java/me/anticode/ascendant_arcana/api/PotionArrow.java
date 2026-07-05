package me.anticode.ascendant_arcana.api;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

import java.util.Set;

public interface PotionArrow {
    Set<MobEffectInstance> ascendant_arcana$getEffects();

    Potion ascendant_arcana$getPotion();
}
