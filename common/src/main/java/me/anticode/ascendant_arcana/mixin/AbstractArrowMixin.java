package me.anticode.ascendant_arcana.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import me.anticode.ascendant_arcana.api.EnchantedArrow;
import me.anticode.ascendant_arcana.api.PotionArrow;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements EnchantedArrow {

    @Shadow
    protected boolean inGround;

    @Shadow
    public abstract void setCritArrow(boolean critical);

    @Shadow
    @Nullable
    private BlockState lastState;

    @Shadow
    public abstract byte getPierceLevel();

    @Shadow
    protected abstract void doPostHurtEffects(LivingEntity target);

    @Shadow
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;

    @Unique
    private int ascendant_arcana$archersGambitLevel;

    @Unique
    private int ascendant_arcana$evokersWrathLevel;

    @Unique
    private int ascendant_arcana$hobblingShotLevel;

    @Unique
    private int ascendant_arcana$ricochetLevel;

    @Unique
    private int ascendant_arcana$ricochetBounces = 0;

    @Unique
    private boolean ascendant_arcana$ricochet;

    @Unique
    @Nullable
    private Vec3 ascendant_arcana$ricochetVector;

    @Unique
    private int ascendant_arcana$rejuvenatingShotLevel;

    @Unique
    private boolean ascendant_arcana$didHitEntity = false;

    @Unique
    private int ascendant_arcana$miasmaLevel = 0;

    @Override
    public void ascendant_arcana$setArchersGambitLevel(int value) {
        this.ascendant_arcana$archersGambitLevel = value;
    }

    @Override
    public void ascendant_arcana$setEvokersWrathLevel(int value) {
        this.ascendant_arcana$evokersWrathLevel = value;
    }

    @Override
    public void ascendant_arcana$setRejuvenatingShotLevel(int value) {
        this.ascendant_arcana$rejuvenatingShotLevel = value;
    }

    @Override
    public void ascendant_arcana$setRicochetLevel(int value) {
        this.ascendant_arcana$ricochetLevel = value;
    }

    @Override
    public void ascendant_arcana$setHobblingShotLevel(int value) {
        this.ascendant_arcana$hobblingShotLevel = value;
    }

    @Override
    public void ascendant_arcana$setMiasmaLevel(int value) {
        this.ascendant_arcana$miasmaLevel = value;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomAttributes(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt("archersGambitLevel", ascendant_arcana$archersGambitLevel);
        nbt.putInt("evokersWrathLevel", ascendant_arcana$evokersWrathLevel);
        nbt.putInt("rejuvenatingShotLevel", ascendant_arcana$rejuvenatingShotLevel);
        nbt.putInt("ricochetLevel", ascendant_arcana$ricochetLevel);
        nbt.putInt("miasmaLevel", ascendant_arcana$miasmaLevel);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        this.ascendant_arcana$archersGambitLevel = nbt.getInt("archersGambitLevel");
        this.ascendant_arcana$evokersWrathLevel = nbt.getInt("evokersWrathLevel");
        this.ascendant_arcana$rejuvenatingShotLevel = nbt.getInt("rejuvenatingShotLevel");
        this.ascendant_arcana$ricochetLevel = nbt.getInt("ricochetLevel");
        this.ascendant_arcana$miasmaLevel = nbt.getInt("miasmaLevel");
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void willRicochet(CallbackInfo ci) {
        Projectile projectile = (Projectile)((Object)this);
        if (projectile.level().isClientSide()) return;

        Vec3 vel = projectile.getDeltaMovement();
        Vec3 pos = projectile.position();
        Vec3 futurePos = pos.add(vel);
        BlockHitResult hitResult = projectile.level().clip(new ClipContext(pos, futurePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile));

        if (hitResult.getType() == HitResult.Type.MISS) return;

        if (ascendant_arcana$ricochetLevel >= 1 && ascendant_arcana$ricochetBounces < ascendant_arcana$ricochetLevel) {
            // Velocity reflection
            Vec3i tempNormal = hitResult.getDirection().getNormal();
            Vec3 normal = new Vec3(tempNormal.getX(), tempNormal.getY(), tempNormal.getZ()).normalize();
            double dotProduct = vel.dot(normal);

            ascendant_arcana$ricochetVector = vel.subtract(normal.scale(2D * dotProduct)).normalize();
            ascendant_arcana$ricochet = true;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void doRicochet(CallbackInfo ci) {
        if (ascendant_arcana$ricochet) {
            // Undo the effects of onBlockHit
            AbstractArrow projectile = (AbstractArrow)(Object)this;
            projectile.shakeTime = 0;
            inGround = false;
            setCritArrow(true);
            lastState = null;

            ascendant_arcana$doRicochet();
        }
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelIfDeflecting(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (AArcanaEnchantmentHelper.doesEntityHitHaveDeflect(entityHitResult, (AbstractArrow)(Object)this)) ci.cancel();
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), cancellable = true)
    private void healInsteadOfDamage(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (ascendant_arcana$rejuvenatingShotLevel < 1) return;
        AbstractArrow projectile = (AbstractArrow) (Object) this;
        Entity entity2 = projectile.getOwner();
        Entity attacker = entityHitResult.getEntity();
        int damage = Mth.ceil(Mth.clamp((double)projectile.getDeltaMovement().length() * projectile.getBaseDamage(), (double)0.0F, (double)Integer.MAX_VALUE));;
        if (attacker instanceof LivingEntity livingTarget) {
            if (attacker == entity2) return;
            livingTarget.heal((float) damage / 2);
            doPostHurtEffects(livingTarget);
            if (!projectile.level().isClientSide()) {
                if (getPierceLevel() <= 0) livingTarget.setArrowCount(livingTarget.getArrowCount() + 1);
                for(int j = 0; j < 5; ++j) {
                    double offset = livingTarget.getRandom().nextGaussian() * 0.02;
                    ((ServerLevel)livingTarget.level()).sendParticles(ParticleTypes.HEART, livingTarget.getX(2 * livingTarget.getRandom().nextDouble() - 1), livingTarget.getRandomY(), livingTarget.getZ(2 * livingTarget.getRandom().nextDouble() - 1), 5, offset, offset, offset, 1);
                }
                SoundSource soundCategory = SoundSource.PLAYERS;
                if (projectile.getOwner() != null) soundCategory = projectile.getOwner().getSoundSource();
                livingTarget.level().playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(), SoundEvents.ARROW_HIT_PLAYER, soundCategory, 1.0F, 1.0F);
            }
            if (livingTarget instanceof Player && entity2 instanceof ServerPlayer && !projectile.isSilent()) {
                ((ServerPlayer) entity2).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
            }
        }
        if (getPierceLevel() <= 0) {
            projectile.discard();
        }
        if (ascendant_arcana$miasmaLevel > 0) {
            ascendant_arcana$createMiasmaCloud(projectile, entityHitResult.getLocation());
        }
        ci.cancel();
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean modifyDamageDealt(Entity instance, DamageSource source, float amount) {
        if (ascendant_arcana$ricochetLevel >= 1 && ascendant_arcana$ricochetBounces == 0) {
            amount /= 2;
        }
        else if (ascendant_arcana$ricochetLevel >= 1 && ascendant_arcana$ricochetBounces > 0) {
            amount += ascendant_arcana$ricochetBounces * 2;
        }
        return instance.hurt(source, amount);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void onEntityHitTail(EntityHitResult entityHitResult, CallbackInfo ci) {
        Projectile projectile = (Projectile)((Object)this);
        LivingEntity owner = (LivingEntity)projectile.getOwner();
        Level level = entityHitResult.getEntity().level();

        if (ascendant_arcana$evokersWrathLevel >= 1) {
            ascendant_arcana$summonEvokersWrathFangs(owner, projectile, entityHitResult.getLocation(), level);
        }

        if (ascendant_arcana$archersGambitLevel >= 1 && (entityHitResult.getEntity() instanceof LivingEntity) && owner != null) {
            MobEffectInstance archersGambitInstance = owner.getEffect(AArcanaMobEffects.ARCHERS_GAMBIT.get());
            int consecutiveShots = Mth.clamp(archersGambitInstance != null ? archersGambitInstance.getAmplifier() + 1 : 0, 0, 2);
            MobEffectInstance newInstance = new MobEffectInstance(
                    AArcanaMobEffects.ARCHERS_GAMBIT.get(),
                    40 * ascendant_arcana$archersGambitLevel,
                    consecutiveShots,
                    false,
                    false,
                    true
            );
            owner.addEffect(newInstance);
            ascendant_arcana$didHitEntity = true;
        }

        if (ascendant_arcana$hobblingShotLevel >= 1 && (entityHitResult.getEntity() instanceof LivingEntity target)) {
            MobEffectInstance hobblingShotInstance = target.getEffect(AArcanaMobEffects.HOBBLED.get());
            int consecutiveShots = Mth.clamp(hobblingShotInstance != null ? hobblingShotInstance.getAmplifier() + 1 : 0, 0, 5);
            MobEffectInstance newInstance = new MobEffectInstance(
                    AArcanaMobEffects.HOBBLED.get(),
                    60 * ascendant_arcana$hobblingShotLevel,
                    consecutiveShots,
                    false,
                    false,
                    true
            );
            target.addEffect(newInstance);
        }

        if (ascendant_arcana$miasmaLevel >= 1) {
            ascendant_arcana$createMiasmaCloud(projectile, entityHitResult.getLocation());
        }
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V"))
    private void ricochetOnEntityHit(AbstractArrow abstractArrow, @Local(argsOnly = true) EntityHitResult entityHitResult) {
        if (ascendant_arcana$ricochetLevel >= 1 && ascendant_arcana$ricochetBounces < ascendant_arcana$ricochetLevel && getPierceLevel() == 0) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                // Removing the stuck arrow applied by the hit
                livingEntity.setArrowCount(livingEntity.getArrowCount() - 1);
            }
            ascendant_arcana$ricochetVector = abstractArrow.getDeltaMovement().multiply(-0.8D, 1D, -0.8D);
            ascendant_arcana$doRicochet();
        }
        else {
            abstractArrow.discard();
        }
    }

    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void onBlockHitTail(BlockHitResult blockHitResult, CallbackInfo ci) {
        Projectile projectile = (Projectile)((Object)this);
        LivingEntity owner = (LivingEntity)projectile.getOwner();
        Level level = projectile.level();
        if (ascendant_arcana$evokersWrathLevel >= 1) {
            ascendant_arcana$summonEvokersWrathFangs(owner, projectile, blockHitResult.getLocation(), level);
        }

        if (ascendant_arcana$archersGambitLevel >= 1) {
            if (owner != null && owner.getEffect(AArcanaMobEffects.ARCHERS_GAMBIT.get()) != null && (ascendant_arcana$ricochetLevel == 0 || ascendant_arcana$ricochetBounces >= ascendant_arcana$ricochetLevel) && (getPierceLevel() == 0 || (piercingIgnoreEntityIds != null && piercingIgnoreEntityIds.isEmpty()))) {
                if (!ascendant_arcana$didHitEntity) owner.removeEffect(AArcanaMobEffects.ARCHERS_GAMBIT.get());
            }
        }

        if (ascendant_arcana$miasmaLevel >= 1) {
            ascendant_arcana$createMiasmaCloud(projectile, blockHitResult.getLocation());
        }
    }

    @Unique
    private void ascendant_arcana$summonEvokersWrathFangs(LivingEntity owner, Projectile projectile, Vec3 pos, Level level) {
        if (ascendant_arcana$evokersWrathLevel >= 1) {
            BlockPos blockPos = BlockPos.containing(pos);
            boolean bl = false;
            double d = 0.0D;

            do {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState = level.getBlockState(blockPos2);
                if (blockState.isFaceSturdy(level, blockPos2, Direction.UP)) {
                    if (!level.isEmptyBlock(blockPos)) {
                        BlockState blockState2 = level.getBlockState(blockPos);
                        VoxelShape voxelShape = blockState2.getCollisionShape(level, blockPos);
                        if (!voxelShape.isEmpty()) {
                            d = voxelShape.max(Direction.Axis.Y);
                        }
                    }

                    bl = true;
                    break;
                }

                blockPos = blockPos.below();
            } while(blockPos.getY() >= level.getMinBuildHeight());

            if (bl) {
                Vec3 Vec3 = blockPos.getCenter();
                level.addFreshEntity(new EvokerFangs(level, Vec3.x(), blockPos.getY() + d, Vec3.z(), projectile.getYRot(), 0, owner));
            }
        }
    }

    @Unique
    private void ascendant_arcana$doRicochet() {
        ascendant_arcana$ricochetBounces++;

        AbstractArrow abstractArrow = (AbstractArrow)(Object)this;

        // Update velocity
        abstractArrow.setDeltaMovement(ascendant_arcana$ricochetVector);
        abstractArrow.flyDist -= 0.5F;
        abstractArrow.hurtMarked = true;
        abstractArrow.hasImpulse = true;
        // We take an extra step to get out of the block onBlockHit lodged us in
        abstractArrow.move(MoverType.SELF, ascendant_arcana$ricochetVector.scale(0.1));

        ascendant_arcana$ricochet = false;
        ascendant_arcana$ricochetVector = null;
    }

    @Unique
    private void ascendant_arcana$createMiasmaCloud(Projectile projectile, Vec3 location) {
        Set<MobEffectInstance> effects = Sets.newHashSet();
        Potion potion = Potions.EMPTY;
        if (projectile instanceof Arrow arrow) {
            potion = ((PotionArrow) arrow).ascendant_arcana$getPotion();
            effects.addAll(((PotionArrow) arrow).ascendant_arcana$getEffects());
            effects.addAll(potion.getEffects());
        } else if (projectile instanceof SpectralArrow) {
            effects.add(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        if (!effects.isEmpty()) {
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(projectile.level(), location.x, location.y, location.z);
            Entity entity = projectile.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloud.setOwner((LivingEntity)entity);
            }

            areaEffectCloud.setRadius(ascendant_arcana$miasmaLevel * 1.5F);
            areaEffectCloud.setRadiusOnUse(-(1 - (ascendant_arcana$miasmaLevel * 0.25F)));
            areaEffectCloud.setWaitTime(10);
            areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / ((float)areaEffectCloud.getDuration() / 2F * ascendant_arcana$miasmaLevel));
            areaEffectCloud.setPotion(potion);

            for(MobEffectInstance mobEffectInstance : effects) {
                areaEffectCloud.addEffect(new MobEffectInstance(mobEffectInstance));
            }

            projectile.level().addFreshEntity(areaEffectCloud);
        }
    }
}
