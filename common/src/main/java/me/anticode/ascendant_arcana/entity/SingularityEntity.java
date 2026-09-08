package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.init.AArcanaSoundEvents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SingularityEntity extends OwnedEntity {
    public final static EntityDataAccessor<Integer> maxLife = SynchedEntityData.defineId(SingularityEntity.class, EntityDataSerializers.INT);
    public final static EntityDataAccessor<Integer> life = SynchedEntityData.defineId(SingularityEntity.class, EntityDataSerializers.INT);

    public SingularityEntity(EntityType<? extends SingularityEntity> entityType, Level level) {
        super(entityType, level);
        entityData.set(life, entityData.get(maxLife));
        this.noPhysics = true;
        this.noCulling = true;
    }

    public SingularityEntity(Level level, LivingEntity livingEntity, int singularityLevel) {
        super(AArcanaEntities.SINGULARITY_ENTITY.get(), level);
        entityData.set(maxLife, 30 * singularityLevel);
        entityData.set(life, entityData.get(maxLife));
        this.noPhysics = true;
        this.noCulling = true;
        setOwner(livingEntity);

    }

    @Override
    public void tick() {
        if (entityData.get(life).equals(entityData.get(maxLife))) {
            level().playSound(null, getX(), getY(), getZ(), AArcanaSoundEvents.SINGULARITY_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 3.0F);
        } else if (getCyclicalLife() == 19) {
            level().playSound(null, getX(), getY(), getZ(), AArcanaSoundEvents.SINGULARITY.get(), SoundSource.PLAYERS, 1F, 3F);
        } else if (getCyclicalLife() == 15) {
            if (!level().isClientSide()){
                level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(5F), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(this::notOwnerAlly)).forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    Vec3 offset = livingEntity.position().vectorTo(position());
                    double knockbackStrength = Math.max(position().distanceTo(livingEntity.position()) / 5F, 0);
                    Vec3 knockbackVector = offset.normalize();
                    livingEntity.knockback(knockbackStrength, -knockbackVector.x, -knockbackVector.z);
                    livingEntity.hurtMarked = true;
                });
            }
        } else if (entityData.get(life) == 0) {
            discard();
        }
        if (entityData.get(life) % 5 == 0) {
            if (!level().isClientSide()) {
                level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(0.05F), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(this::notOwnerAlly)).forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.addEffect(new MobEffectInstance(AArcanaMobEffects.HOBBLED.get(), 40, 2, false, false, true));
                    livingEntity.hurt(damageSources().indirectMagic(this, getOwner()), 1F);
                });
            }
        }
        entityData.set(life, entityData.get(life) - 1);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(maxLife, 30);
        entityData.define(life, 30);
    }

    public int getCyclicalLife() {
        return entityData.get(life) % 30;
    }
}
