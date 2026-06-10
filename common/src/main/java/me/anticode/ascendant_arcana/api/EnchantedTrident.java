package me.anticode.ascendant_arcana.api;

import net.minecraft.world.entity.LivingEntity;

public interface EnchantedTrident {
    void ascendant_arcana$setAmbushLevel(int ambushLevel);

    void ascendant_arcana$setLifetideLevel(int lifetideLevel);

    int ascendant_arcana$getLifetideLevel();

    void ascendant_arcana$setSunderingLevel(int sunderingLevel);

    net.minecraft.world.entity.LivingEntity ascendant_arcana$getStuckEntity();

    float ascendant_arcana$getRenderTicks();

    float ascendant_arcana$getStabTicks();

    int ascendant_arcana$getLoyaltyLevel();
}
