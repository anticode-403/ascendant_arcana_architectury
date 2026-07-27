package me.anticode.ascendant_arcana.api;

import net.minecraft.world.phys.Vec3;

public interface AArcanaPlayer {
    void ascendant_arcana$setShieldBashStatus(boolean status);

    boolean ascendant_arcana$getShieldBashStatus();

    int ascendant_arcana$getShieldBashTicks();

    Vec3 ascendant_arcana$getShieldBashDirection();
}
