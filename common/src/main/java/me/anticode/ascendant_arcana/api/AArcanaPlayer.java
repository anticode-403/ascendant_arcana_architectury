package me.anticode.ascendant_arcana.api;

import net.minecraft.world.phys.Vec3;

public interface AArcanaPlayer {
    void ascendant_arcana$setShieldBashStatus(boolean status);

    boolean ascendant_arcana$getShieldBashStatus();

    int ascendant_arcana$getShieldBashTicks();

    Vec3 ascendant_arcana$getShieldBashDirection();

    void ascendant_arcana$setWhirlwindCharge(boolean status);

    boolean ascendant_arcana$isWhirlwindCharging();

    int ascendant_arcana$getWhirlwindCharge();

    void ascendant_arcana$setWhirlwinding(boolean status);

    boolean ascendant_arcana$isWhirlwinding();

    float ascendant_arcana$getWhirlwindCooldown();

    int ascendant_arcana$getLaunchingCharge();

    boolean ascendant_arcana$isLaunching();
}
