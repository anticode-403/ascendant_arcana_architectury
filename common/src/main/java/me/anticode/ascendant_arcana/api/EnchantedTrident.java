package me.anticode.ascendant_arcana.api;

import net.minecraft.world.entity.Entity;

public interface EnchantedTrident {
    void ascendant_arcana$setSingularityLevel(int singularityLevel);

    int ascendant_arcana$getSingularityLevel();

    void ascendant_arcana$setAmbushLevel(int ambushLevel);

    void ascendant_arcana$setLifetideLevel(int lifetideLevel);

    int ascendant_arcana$getLifetideLevel();

    void ascendant_arcana$setSunderingLevel(int sunderingLevel);

    Entity ascendant_arcana$getStuckEntity();

    boolean ascendant_arcana$wasStuck();

    float ascendant_arcana$getStabTicks();

    int ascendant_arcana$getLoyaltyLevel();

    void ascendant_arcana$setClientStuckEntity(int stuck);

    int ascendant_arcana$getStormAnchorLevel();

    void ascendant_arcana$setStormAnchorLevel(int stormAnchorLevel);

    void ascendant_arcana$stickEntity(Entity entity);
}
