package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.entity.SingularityEntity;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.networking.TridentSync;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin implements EnchantedTrident {
    @Shadow
    @Final
    private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow
    private ItemStack tridentItem;
    @Unique
    private int ascendant_arcana$singularityLevel;

    @Unique
    private int ascendant_arcana$ambushLevel;

    @Unique
    private int ascendant_arcana$lifetideLevel;

    @Unique
    private int ascendant_arcana$sunderingLevel;

    @Unique
    private Entity ascendant_arcana$stuckEntity = null;

    @Unique
    private int ascendant_arcana$stuckEntityId = -1;

    @Unique
    private int ascendant_arcana$ticksStuck = 0;

    @Unique
    private float ascendant_arcana$stabTicks = 0;

    @Unique
    private int ascendant_arcana$disabledLoyaltyLevels = 0;

    @Unique private boolean ascendant_arcana$wasStuck = false;

    @Unique
    private float ascendant_arcana$relicDamageMultiplier = 1;

    @Unique
    private int ascendant_arcana$stormAnchorLevel = 0;

    @Override
    public Entity ascendant_arcana$getStuckEntity() {
        if (ascendant_arcana$stuckEntityId >= 0 && ascendant_arcana$stuckEntity == null) {
            ascendant_arcana$stuckEntity = ((ThrownTrident)(Object)this).level().getEntity(ascendant_arcana$stuckEntityId);
        }
        return ascendant_arcana$stuckEntity;
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
    public int ascendant_arcana$getSingularityLevel() {
        return this.ascendant_arcana$singularityLevel;
    }

    @Override
    public void ascendant_arcana$setSingularityLevel(int value) {
        this.ascendant_arcana$singularityLevel = value;
    }

    @Override
    public int ascendant_arcana$getLoyaltyLevel() {
        return ((ThrownTrident)(Object)this).getEntityData().get(ID_LOYALTY);
    }

    @Override
    public void ascendant_arcana$setClientStuckEntity(int value) {
        this.ascendant_arcana$stuckEntityId = value;
        if (value >= 0) this.ascendant_arcana$stuckEntity = ((ThrownTrident)(Object)this).level().getEntity(ascendant_arcana$stuckEntityId);
        else this.ascendant_arcana$stuckEntity = null;
    }

    @Override
    public int ascendant_arcana$getStormAnchorLevel() {
        return this.ascendant_arcana$stormAnchorLevel;
    }

    @Override
    public void ascendant_arcana$setStormAnchorLevel(int value) {
        this.ascendant_arcana$stormAnchorLevel = value;
    }

    @Override
    public void ascendant_arcana$stickEntity(Entity entity) {
        ascendant_arcana$stuckEntity = entity;
        ascendant_arcana$stuckEntityId = entity.getId();
        ThrownTrident projectile = (ThrownTrident) (Object)this;
        if (projectile.level() instanceof ServerLevel serverLevel) {
            NetworkManager.sendToPlayers(serverLevel.players(), TridentSync.Id, new TridentSync(projectile.getId(), ascendant_arcana$stuckEntityId).write());
        }
    }

    @Override
    public boolean ascendant_arcana$wasStuck() {
        return this.ascendant_arcana$wasStuck;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    private void addEnchantmentsToTrident(Level level, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
        int ambushLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.AMBUSH.get(), itemStack);
        int lifetideLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.LIFETIDE.get(), itemStack);
        int sunderingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SUNDERING.get(), itemStack);
        int singularityLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SINGULARITY.get(), itemStack);
        int stormAnchorLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.STORM_ANCHOR.get(), itemStack);

        ascendant_arcana$setAmbushLevel(ambushLevel);
        ascendant_arcana$setLifetideLevel(lifetideLevel);
        ascendant_arcana$setSunderingLevel(sunderingLevel);
        ascendant_arcana$setSingularityLevel(singularityLevel);
        ascendant_arcana$setStormAnchorLevel(stormAnchorLevel);
        this.ascendant_arcana$disabledLoyaltyLevels = ((ThrownTrident)(Object)this).getEntityData().get(ID_LOYALTY);
        this.ascendant_arcana$relicDamageMultiplier = (float) RelicHelper.applyAllRelicsOfType(RelicTypes.DAMAGE, 1, itemStack.getTag());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomAttributes(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt("singularityLevel", ascendant_arcana$singularityLevel);
        nbt.putInt("ambushLevel", ascendant_arcana$ambushLevel);
        nbt.putInt("lifetideLevel", ascendant_arcana$lifetideLevel);
        nbt.putInt("stuckEntityId", ascendant_arcana$stuckEntityId);
        nbt.putInt("ticksStuck", ascendant_arcana$ticksStuck);
        nbt.putFloat("stabTicks", ascendant_arcana$stabTicks);
        nbt.putFloat("relicDamageMultiplier", ascendant_arcana$relicDamageMultiplier);
        nbt.putInt("stormAnchorLevel", ascendant_arcana$stormAnchorLevel);
        nbt.putInt("disabledLoyaltyLevels", ascendant_arcana$disabledLoyaltyLevels);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        this.ascendant_arcana$singularityLevel = nbt.getInt("singularityLevel");
        this.ascendant_arcana$ambushLevel = nbt.getInt("ambushLevel");
        this.ascendant_arcana$lifetideLevel = nbt.getInt("lifetideLevel");
        this.ascendant_arcana$stuckEntityId = nbt.getInt("stuckEntityId");
        this.ascendant_arcana$ticksStuck = nbt.getInt("ticksStuck");
        this.ascendant_arcana$stabTicks = nbt.getFloat("stabTicks");
        this.ascendant_arcana$relicDamageMultiplier = nbt.getFloat("relicDamageMultiplier");
        this.ascendant_arcana$stormAnchorLevel = nbt.getInt("stormAnchorLevel");
        this.ascendant_arcana$disabledLoyaltyLevels = nbt.getInt("disabledLoyaltyLevels");
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onEntityHitHead(EntityHitResult entityHitResult, CallbackInfo ci) {
        AbstractArrow projectile = (AbstractArrow)((Object)this);
        if (ascendant_arcana$lifetideLevel >= 1 || ascendant_arcana$sunderingLevel >= 1) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && ascendant_arcana$stuckEntity == null && !ascendant_arcana$wasStuck) {
                ascendant_arcana$stickEntity(livingEntity);
                SoundSource soundCategory = SoundSource.PLAYERS;
                if (projectile.getOwner() != null) soundCategory = projectile.getOwner().getSoundSource();
                projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.TRIDENT_HIT, soundCategory);
                if (ascendant_arcana$lifetideLevel >= 1) {
                    ascendant_arcana$stuckParticleSpray((ThrownTrident) projectile, livingEntity, true);
                    projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, soundCategory, 1, 2);
                    if (livingEntity.getMobType() == MobType.UNDEAD) livingEntity.hurt(projectile.damageSources().trident(projectile, projectile.getOwner()), (float) 4 * ascendant_arcana$relicDamageMultiplier);
                    else livingEntity.heal((float) 4 * ascendant_arcana$relicDamageMultiplier);
                } else if (ascendant_arcana$sunderingLevel >= 1) {
                    ascendant_arcana$stuckParticleSpray((ThrownTrident) projectile, livingEntity, false);
                    projectile.level().playSound(null, projectile.blockPosition(), SoundEvents.ITEM_BREAK, soundCategory, 1, 0.5F);
                    livingEntity.addEffect(new MobEffectInstance(AArcanaMobEffects.SUNDERED.get(), 60, 0, true, false, true));
                    livingEntity.hurt(projectile.damageSources().trident(projectile, projectile.getOwner()), 2);
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
        if (ascendant_arcana$singularityLevel >= 1 && !ascendant_arcana$wasStuck) {
            SingularityEntity singularity = new SingularityEntity(projectile.level(), (LivingEntity) projectile.getOwner(), ascendant_arcana$singularityLevel);
            Vec3 averagePosition = projectile.position().add(entityHitResult.getLocation()).multiply(0.5, 0.5, 0.5);
            singularity.setPos(averagePosition);
            projectile.level().addFreshEntity(singularity);
            ascendant_arcana$stickEntity(singularity);
        }
    }

    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean applyDamageRelic(Entity instance, DamageSource damageSource, float f, Operation<Boolean> original) {
        return original.call(instance, damageSource, f * ascendant_arcana$relicDamageMultiplier);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void stuckTridentEnchants(CallbackInfo ci) {
        if (Platform.isFabric() && !ascendant_arcana$isStickableTrident()) return;
        else if (Platform.isForge() && (ascendant_arcana$stuckEntityId == -1)) return;
        ThrownTrident trident = (ThrownTrident)(Object)this;
        if (ascendant_arcana$stuckEntityId == -2) {
            ascendant_arcana$stuckEntity = null;
            ascendant_arcana$stuckEntityId = -1;
            if (trident.level() instanceof ServerLevel serverLevel) {
                NetworkManager.sendToPlayers(serverLevel.players(), TridentSync.Id, new TridentSync(trident.getId(), ascendant_arcana$stuckEntityId).write());
            }
            trident.getEntityData().set(ID_LOYALTY, (byte)ascendant_arcana$disabledLoyaltyLevels);
        } else if (ascendant_arcana$stuckEntityId != -1 && ascendant_arcana$stuckEntity == null && trident.level().getEntity(ascendant_arcana$stuckEntityId) instanceof LivingEntity living) {
            ascendant_arcana$stuckEntity = living;
        } else {
            if (ascendant_arcana$stuckEntity != null && ascendant_arcana$stuckEntity.isAlive()) {
                trident.setDeltaMovement(Vec3.ZERO);
                trident.setNoPhysics(true);
                // This is a very ugly thing to do, but I couldn't figure out how to fix the Loyalty + Stuck rubber-banding
                trident.getEntityData().set(ID_LOYALTY, (byte)0);
                ascendant_arcana$wasStuck = true;
                if (++ascendant_arcana$ticksStuck > 120) {
                    ascendant_arcana$stuckEntityId = -2;
                    trident.setNoPhysics(false);
                    trident.getEntityData().set(ID_LOYALTY, (byte)ascendant_arcana$disabledLoyaltyLevels);
                }
                ascendant_arcana$stabTicks = Math.max(0, ascendant_arcana$stabTicks - ascendant_arcana$stabTicks / 20F);
            } else if (ascendant_arcana$stuckEntityId != -1) {
                trident.setNoPhysics(false);
                trident.getEntityData().set(ID_LOYALTY, (byte)ascendant_arcana$disabledLoyaltyLevels);
                ascendant_arcana$stuckEntityId = -2;
                ascendant_arcana$ticksStuck = 0;
                ascendant_arcana$stabTicks = 0;
            }
        }
        if (!trident.level().isClientSide()) {
            if (ascendant_arcana$stuckEntity != null && ascendant_arcana$stuckEntity.isAlive()) {
                if (trident.getOwner() instanceof LivingEntity living && living.isAlive()) {
                    trident.teleportTo(ascendant_arcana$stuckEntity.getX(), ascendant_arcana$stuckEntity.getEyeY(), ascendant_arcana$stuckEntity.getZ());
                    if (ascendant_arcana$ticksStuck % 20 == 0) {
                        if (ascendant_arcana$lifetideLevel >= 1) {
                            LivingEntity stuckEntity = (LivingEntity)ascendant_arcana$stuckEntity;
                            ascendant_arcana$stuckParticleSpray(trident, stuckEntity, true);
                            trident.level().playSound(null, trident.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, stuckEntity.getSoundSource(), 1, 2);
                            if (stuckEntity.getMobType() == MobType.UNDEAD) stuckEntity.hurt(trident.damageSources().trident(trident, trident.getOwner()), (float) 2 * ascendant_arcana$relicDamageMultiplier);
                            else stuckEntity.heal((float) 2 * ascendant_arcana$relicDamageMultiplier);
                            living.heal(1);
                        } else if (ascendant_arcana$sunderingLevel >= 1) {
                            LivingEntity stuckEntity = (LivingEntity)ascendant_arcana$stuckEntity;
                            ascendant_arcana$stuckParticleSpray(trident, stuckEntity, false);
                            trident.level().playSound(null, trident.blockPosition(), SoundEvents.ITEM_BREAK, stuckEntity.getSoundSource(), 1, 0.5F);
                            stuckEntity.addEffect(new MobEffectInstance(AArcanaMobEffects.SUNDERED.get(), 60, 0, true, false, true));
                            stuckEntity.hurt(trident.damageSources().trident(trident, trident.getOwner()), 1 * ascendant_arcana$relicDamageMultiplier);
                        }
                        ascendant_arcana$stabTicks = 1;
                    }
                } else {
                    ascendant_arcana$stuckEntityId = -2;
                }
            }
        } else if (ascendant_arcana$stuckEntity != null && ascendant_arcana$stuckEntity.isAlive()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;playerTouch(Lnet/minecraft/world/entity/player/Player;)V"))
    private void cannotPickupWhileStuck(ThrownTrident instance, Player arg, Operation<Void> original) {
        if (ascendant_arcana$stuckEntityId < 0) original.call(instance, arg);
    }

    @Unique
    private void ascendant_arcana$stuckParticleSpray(ThrownTrident projectile, LivingEntity livingEntity, boolean lifetide) {
        if (projectile.level() instanceof ServerLevel serverLevel) {
            for(int i = 0; i < 5; ++i) {
                double offset = livingEntity.getRandom().nextGaussian() * 0.02;
                serverLevel.sendParticles(lifetide ? ParticleTypes.HEART : ParticleTypes.DAMAGE_INDICATOR, livingEntity.getX(2 * livingEntity.getRandom().nextDouble() - 1), livingEntity.getRandomY(), livingEntity.getZ(2 * livingEntity.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
            }
        }
    }

    @Unique
    private boolean ascendant_arcana$isStickableTrident() {
        return ascendant_arcana$singularityLevel > 0 || ascendant_arcana$stormAnchorLevel > 0 || ascendant_arcana$lifetideLevel > 0 || ascendant_arcana$sunderingLevel > 0;
    }
}
