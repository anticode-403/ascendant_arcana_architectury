package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.init.AArcanaSoundEvents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class GlacioclasmEntity extends OwnedEntity {
    public final static EntityDataAccessor<Integer> life = SynchedEntityData.defineId(GlacioclasmEntity.class, EntityDataSerializers.INT);
    public final static EntityDataAccessor<Integer> maxLife = SynchedEntityData.defineId(GlacioclasmEntity.class, EntityDataSerializers.INT);

    public GlacioclasmEntity(EntityType<? extends GlacioclasmEntity> entityType, Level level) {
        super(entityType, level);
        entityData.set(maxLife, 35);
        entityData.set(life, 35);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public GlacioclasmEntity(Level level, LivingEntity livingEntity, int delay) {
        super(AArcanaEntities.GLACIOCLASM_ENTITY.get(), level);
        entityData.set(maxLife, delay + 5);
        entityData.set(life, delay + 5);
        this.noPhysics = true;
        this.noCulling = true;
        setOwner(livingEntity);
    }

    @Override
    public void tick() {
        if (entityData.get(life).equals(entityData.get(maxLife))) {
            level().playSound(null, getX(), getY(), getZ(), AArcanaSoundEvents.SINGULARITY_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 3.0F);
        } else if (entityData.get(life) == 2) {
            if (!level().isClientSide()) {
                level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(5F), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(this::notOwnerAlly)).forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.setTicksFrozen(240);

                });
            }
            discard();
        }
        entityData.set(life, entityData.get(life) - 1);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(life, 35);
        entityData.define(maxLife, 35);
    }
}
