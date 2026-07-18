package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.init.AArcanaSoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SingularityEntity extends Entity implements TraceableEntity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    public final static int maxLife = 30;
    public final static EntityDataAccessor<Integer> life = SynchedEntityData.defineId(SingularityEntity.class, EntityDataSerializers.INT);

    public SingularityEntity(EntityType<? extends SingularityEntity> entityType, Level level) {
        super(entityType, level);
        entityData.set(life, maxLife);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public SingularityEntity(Level level, LivingEntity livingEntity) {
        super(AArcanaEntities.SINGULARITY_ENTITY.get(), level);
        entityData.set(life, maxLife);
        this.noPhysics = true;
        this.noCulling = true;
        setOwner(livingEntity);

    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }

    }

    @Override
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public void tick() {
        if (entityData.get(life) == maxLife) {
            level().playSound(null, getX(), getY(), getZ(), AArcanaSoundEvents.SINGULARITY_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 3.0F);
        } else if (entityData.get(life) == 19) {
            level().playSound(null, getX(), getY(), getZ(), AArcanaSoundEvents.SINGULARITY.get(), SoundSource.PLAYERS, 1F, 3F);
        } else if (entityData.get(life) == 15) {
            level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(5F), EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE)).forEach(entity -> {
                LivingEntity livingEntity = (LivingEntity) entity;
                Vec3 offset = livingEntity.position().vectorTo(position());
                double knockbackStrength = Math.max(position().distanceTo(livingEntity.position()) / 5F, 0);
                Vec3 knockbackVector = offset.normalize();
                livingEntity.knockback(knockbackStrength, -knockbackVector.x, -knockbackVector.z);
                livingEntity.hurtMarked = true;
            });
        } else if (entityData.get(life) == 0) {
            discard();
        }
        if (entityData.get(life) % 5 == 0) {
            level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(0.05F),  EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE)).forEach(entity -> {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.hurt(damageSources().indirectMagic(this, getOwner()), 1F);
            });
        }
        entityData.set(life, entityData.get(life) - 1);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(life, maxLife);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
