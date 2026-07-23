package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.api.EnchantedRocket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements EnchantedRocket {
    @Unique
    private int rocketry_level = 0;
    @Unique
    private float damage_multiplier = 1.0F;

    @Override
    public void ascendant_arcana$setRocketryLevel(int level) {
        rocketry_level = level;
    }

    @Override
    public void asecndant_arcana$setDamageMultiplier(float multiplier) {
        damage_multiplier = multiplier;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("rocketryLevel", rocketry_level);
        compoundTag.putFloat("damageMultiplier", damage_multiplier);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        rocketry_level = compoundTag.getInt("rocketryLevel");
        damage_multiplier = compoundTag.getFloat("damageMultiplier");
    }

    @WrapOperation(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean fireworkKnockback(LivingEntity livingEntity, DamageSource damageSource, float f, Operation<Boolean> original) {
        FireworkRocketEntity rocket = (FireworkRocketEntity)(Object)this;

        float damage = (rocket.getOwner() == livingEntity ? f / 2 : f) * damage_multiplier;
        var result = original.call(livingEntity, damageSource, damage);

        if (rocketry_level > 0) {
            Vec3 offset = rocket.position().vectorTo(livingEntity.position());
            double knockbackStrength = Math.max(1 - (offset.length() / 5.0F), 0);
            Vec3 knockbackVector = ((livingEntity.onGround() || livingEntity != rocket.getOwner()) ? offset.with(Direction.Axis.Y, 0) : offset).normalize().add(0, 0.2F, 0).scale(knockbackStrength * 2D);
            Vec3 targetVector = ((livingEntity.onGround()) ? livingEntity.getDeltaMovement().with(Direction.Axis.Y, 0.0) : livingEntity.getDeltaMovement()).add(knockbackVector);
            if (livingEntity == rocket.getOwner()) livingEntity.setDeltaMovement(targetVector);
            else livingEntity.knockback(knockbackStrength, -knockbackVector.x, -knockbackVector.z);
            livingEntity.hurtMarked = true;
        }
        return result;
    }
}
