package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void reflectIfBlocked(HitResult hitResult, CallbackInfo ci) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            if (!(entityHitResult.getEntity() instanceof LivingEntity target)) return;
            if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.DEFLECT.get(), target) <= 0) return;
            Projectile projectile = (Projectile)(Object)this;
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity ? (LivingEntity) projectile.getOwner() : target;
            if (projectile instanceof AbstractHurtingProjectile explosiveProjectile) {
                if (target.isDamageSourceBlocked(explosiveProjectile.damageSources().mobProjectile(projectile, owner))) {
                    projectile.shootFromRotation(target, target.getXRot() - 1, target.getYRot(), 0, (float)projectile.getDeltaMovement().length(), 0.5F);
                    projectile.setOwner(target);
                    Vec3 velocity = projectile.getDeltaMovement();
                    double d = Math.sqrt(velocity.x() * velocity.x() + velocity.y() * velocity.y() + velocity.z() * velocity.z());
                    if (d != (double)0.0F) {
                        explosiveProjectile.xPower = velocity.x() / d * 0.1;
                        explosiveProjectile.yPower = velocity.y() / d * 0.1;
                        explosiveProjectile.zPower = velocity.z() / d * 0.1;
                    }
                    target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, target.getSoundSource(), 1, 1);
                    ci.cancel();
                }
            } else if (target.isDamageSourceBlocked(projectile.damageSources().mobProjectile(projectile, owner))) {
                if (projectile instanceof ThrownTrident tridentEntity) {
                    EnchantedTrident enchantedTrident = (EnchantedTrident)tridentEntity;
                    if (enchantedTrident.ascendant_arcana$getLoyaltyLevel() > 0 || enchantedTrident.ascendant_arcana$getLoyaltyLevel() > 0) {
                        return;
                    }
                }
                projectile.shootFromRotation(target, target.getXRot() - 1, target.getYRot(), 0, (float)projectile.getDeltaMovement().length(), 0.5F);
                projectile.setOwner(target);
                target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, target.getSoundSource(), 1, 1);
                ci.cancel();
            }
        }
    }
}
