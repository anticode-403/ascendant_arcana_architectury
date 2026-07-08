package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public class ThrownTridentMixin implements EnchantedTrident {
    @Shadow
    @Final
    private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Unique
    private int ascendant_arcana$ambushLevel;

    @Unique
    private int ascendant_arcana$lifetideLevel;

    @Unique
    private int ascendant_arcana$sunderingLevel;

    @Unique
    private LivingEntity ascendant_arcana$stuckEntity = null;

    @Unique
    private int ascendant_arcana$stuckEntityId = -1;

    @Unique
    private int ascendant_arcana$ticksStuck = 0;

    @Unique
    private float ascendant_arcana$renderTicks = 0;

    @Unique
    private float ascendant_arcana$stabTicks = 0;

    @Unique
    private float ascendant_arcana$relicDamageMultiplier = 1;

    @Override
    public LivingEntity ascendant_arcana$getStuckEntity() {
        return ascendant_arcana$stuckEntity;
    }

    public float ascendant_arcana$getRenderTicks() {
        return ascendant_arcana$renderTicks;
    }

    public float ascendant_arcana$getStabTicks() {
        return ascendant_arcana$stabTicks;
    }

    @Override
    public void ascendant_arcana$setLifetideLevel(int value) {
        this.ascendant_arcana$lifetideLevel = value;
    }

    @Override
    public int ascendant_arcana$getLifetideLevel() {
        return ascendant_arcana$lifetideLevel;
    }

    @Override
    public void ascendant_arcana$setSunderingLevel(int value) {
        this.ascendant_arcana$sunderingLevel = value;
    }

    @Override
    public void ascendant_arcana$setAmbushLevel(int value) {
        this.ascendant_arcana$ambushLevel = value;
    }

    @Override
    public int ascendant_arcana$getLoyaltyLevel() {
        return ((ThrownTrident)(Object)this).getEntityData().get(ID_LOYALTY);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    private void addEnchantmentsToTrident(Level level, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
        int ambushLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.AMBUSH.get(), itemStack);
        int lifetideLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.LIFETIDE.get(), itemStack);
        int sunderingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SUNDERING.get(), itemStack);

        ascendant_arcana$setAmbushLevel(ambushLevel);
        ascendant_arcana$setLifetideLevel(lifetideLevel);
        ascendant_arcana$setSunderingLevel(sunderingLevel);
        this.ascendant_arcana$relicDamageMultiplier = 1 + (float)RelicHelper.getTooltipStrength(Relics.DAMAGE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.DAMAGE))*0.01F;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomAttributes(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt("ambushLevel", ascendant_arcana$ambushLevel);
        nbt.putInt("lifetideLevel", ascendant_arcana$lifetideLevel);
        nbt.putInt("stuckEntityId", ascendant_arcana$stuckEntityId);
        nbt.putInt("ticksStuck", ascendant_arcana$ticksStuck);
        nbt.putFloat("renderTicks", ascendant_arcana$renderTicks);
        nbt.putFloat("stabTicks", ascendant_arcana$stabTicks);
        nbt.putFloat("relicDamageMultiplier", ascendant_arcana$relicDamageMultiplier);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        this.ascendant_arcana$ambushLevel = nbt.getInt("ambushLevel");
        this.ascendant_arcana$lifetideLevel = nbt.getInt("lifetideLevel");
        this.ascendant_arcana$stuckEntityId = nbt.getInt("stuckEntityId");
        this.ascendant_arcana$ticksStuck = nbt.getInt("ticksStuck");
        this.ascendant_arcana$renderTicks = nbt.getFloat("renderTicks");
        this.ascendant_arcana$stabTicks = nbt.getFloat("stabTicks");
        this.ascendant_arcana$relicDamageMultiplier = nbt.getFloat("relicDamageMultiplier");
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onEntityHitHead(EntityHitResult entityHitResult, CallbackInfo ci) {
        AbstractArrow projectile = (AbstractArrow)((Object)this);
        if (ascendant_arcana$lifetideLevel >= 1 || ascendant_arcana$sunderingLevel >= 1) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && ascendant_arcana$stuckEntity == null) {
                ascendant_arcana$stuckEntity = livingEntity;
                ascendant_arcana$stuckEntityId = livingEntity.getId();
                SoundSource soundCategory = SoundSource.PLAYERS;
                if (projectile.getOwner() != null) soundCategory = projectile.getOwner().getSoundSource();
                projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.TRIDENT_HIT, soundCategory);
                if (ascendant_arcana$lifetideLevel >= 1) {
                    if (projectile.level() instanceof ServerLevel serverWorld) {
                        for(int i = 0; i < 5; ++i) {
                            double offset = livingEntity.getRandom().nextGaussian() * 0.02;
                            serverWorld.sendParticles(ParticleTypes.HEART, livingEntity.getX(2 * livingEntity.getRandom().nextDouble() - 1), livingEntity.getRandomY(), livingEntity.getZ(2 * livingEntity.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
                        }
                    }
                    projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, soundCategory, 1, 2);
                    if (livingEntity.getMobType() == MobType.UNDEAD) ascendant_arcana$stuckEntity.hurt(projectile.damageSources().trident(projectile, projectile.getOwner()), (float) 4 * ascendant_arcana$relicDamageMultiplier);
                    else ascendant_arcana$stuckEntity.heal((float) 4 * ascendant_arcana$relicDamageMultiplier);
                } else if (ascendant_arcana$sunderingLevel >= 1) {
                    if (projectile.level() instanceof ServerLevel serverWorld) {
                        for(int i = 0; i < 5; ++i) {
                            double offset = livingEntity.getRandom().nextGaussian() * 0.02;
                            serverWorld.sendParticles(ParticleTypes.DAMAGE_INDICATOR, livingEntity.getX(2 * livingEntity.getRandom().nextDouble() - 1), livingEntity.getRandomY(), livingEntity.getZ(2 * livingEntity.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
                        }
                    }
                    projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.ITEM_BREAK, soundCategory, 1, 0.5F);
                    ascendant_arcana$stuckEntity.addEffect(new MobEffectInstance(AArcanaMobEffects.SUNDERED.get(), 60, 0, true, false, true));
                    ascendant_arcana$stuckEntity.hurt(projectile.damageSources().trident(projectile, projectile.getOwner()), 2);
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void onEntityHitTail(EntityHitResult entityHitResult, CallbackInfo ci) {
        AbstractArrow projectile = (AbstractArrow) ((Object)this);
        LivingEntity owner = (LivingEntity)projectile.getOwner();
        Level world = entityHitResult.getEntity().level();

        if (ascendant_arcana$ambushLevel >= 1 && (entityHitResult.getEntity() instanceof LivingEntity)) {
            Vec3 teleTarget = entityHitResult.getLocation();
            world.playSound(null, owner, SoundEvents.ENDERMAN_TELEPORT, owner.getSoundSource(), 1, 1);
            world.gameEvent(GameEvent.TELEPORT, owner.position(), GameEvent.Context.of(owner, owner.getFeetBlockState()));
            owner.teleportTo(teleTarget.x(), teleTarget.y(), teleTarget.z());
            world.broadcastEntityEvent(owner, (byte)46);
            if (owner instanceof PathfinderMob pathAware) {
                pathAware.getNavigation().stop();
            }
        }
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean applyDamageRelic(Entity instance, DamageSource arg, float f) {
        return instance.hurt(arg, f * ascendant_arcana$relicDamageMultiplier);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void stuckTridentEnchants(CallbackInfo ci) {
        if (ascendant_arcana$lifetideLevel <= 0 && ascendant_arcana$sunderingLevel <= 0) return;
        ThrownTrident trident = (ThrownTrident)(Object)this;
        if (ascendant_arcana$stuckEntityId == -2) {
            ascendant_arcana$stuckEntity = null;
            ascendant_arcana$stuckEntityId = -1;
        } else if (ascendant_arcana$stuckEntityId != -1 && ascendant_arcana$stuckEntity == null && trident.level().getEntity(ascendant_arcana$stuckEntityId) instanceof LivingEntity living) {
            ascendant_arcana$stuckEntity = living;
        } else {
            if (ascendant_arcana$stuckEntity != null && ascendant_arcana$stuckEntity.isAlive()) {
                trident.setDeltaMovement(Vec3.ZERO);
                if (++ascendant_arcana$ticksStuck > 120) {
                    ascendant_arcana$stuckEntityId = -2;
                }
                ascendant_arcana$renderTicks += 1 / 20F;
                ascendant_arcana$stabTicks = Math.max(0, ascendant_arcana$stabTicks - ascendant_arcana$stabTicks / 20F);
            } else {
                ascendant_arcana$stuckEntityId = -2;
                ascendant_arcana$ticksStuck = 0;
                ascendant_arcana$renderTicks = 0;
                ascendant_arcana$stabTicks = 0;
            }
        }
        if (!trident.level().isClientSide()) {
            if (ascendant_arcana$stuckEntity != null && ascendant_arcana$stuckEntity.isAlive()) {
                if (trident.getOwner() instanceof LivingEntity living && living.isAlive()) {
                    trident.teleportTo(ascendant_arcana$stuckEntity.getX(), ascendant_arcana$stuckEntity.getEyeY(), ascendant_arcana$stuckEntity.getZ());
                    if (ascendant_arcana$ticksStuck % 20 == 0) {
                        if (ascendant_arcana$lifetideLevel >= 1) {
                            if (trident.level() instanceof ServerLevel serverWorld) {
                                for(int i = 0; i < 5; ++i) {
                                    double offset = ascendant_arcana$stuckEntity.getRandom().nextGaussian() * 0.02;
                                    serverWorld.sendParticles(ParticleTypes.HEART, ascendant_arcana$stuckEntity.getX(2 * ascendant_arcana$stuckEntity.getRandom().nextDouble() - 1), ascendant_arcana$stuckEntity.getRandomY(), ascendant_arcana$stuckEntity.getZ(2 * ascendant_arcana$stuckEntity.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
                                }
                            }
                            trident.level().playSound(null, trident.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, ascendant_arcana$stuckEntity.getSoundSource(), 1, 2);
                            if (ascendant_arcana$stuckEntity.getMobType() == MobType.UNDEAD) ascendant_arcana$stuckEntity.hurt(trident.damageSources().trident(trident, trident.getOwner()), (float) 2 * ascendant_arcana$relicDamageMultiplier);
                            else ascendant_arcana$stuckEntity.heal((float) 2 * ascendant_arcana$relicDamageMultiplier);
                            living.heal(1);
                        } else if (ascendant_arcana$sunderingLevel >= 1) {
                            if (trident.level() instanceof ServerLevel serverWorld) {
                                for(int i = 0; i < 5; ++i) {
                                    double offset = ascendant_arcana$stuckEntity.getRandom().nextGaussian() * 0.02;
                                    serverWorld.sendParticles(ParticleTypes.DAMAGE_INDICATOR, ascendant_arcana$stuckEntity.getX(2 * ascendant_arcana$stuckEntity.getRandom().nextDouble() - 1), ascendant_arcana$stuckEntity.getRandomY(), ascendant_arcana$stuckEntity.getZ(2 * ascendant_arcana$stuckEntity.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
                                }
                            }
                            trident.level().playSound(null, trident.blockPosition(), SoundEvents.ITEM_BREAK, ascendant_arcana$stuckEntity.getSoundSource(), 1, 0.5F);
                            ascendant_arcana$stuckEntity.addEffect(new MobEffectInstance(AArcanaMobEffects.SUNDERED.get(), 60, 0, true, false, true));
                            ascendant_arcana$stuckEntity.hurt(trident.damageSources().trident(trident, trident.getOwner()), 1 * ascendant_arcana$relicDamageMultiplier);
                        }
                        ascendant_arcana$stabTicks = 1;
                    }
                } else {
                    ascendant_arcana$stuckEntityId = -2;
                }
            }
        }
    }
}
