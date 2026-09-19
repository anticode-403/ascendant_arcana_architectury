package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LightningTurretEntity extends OwnedEntity {
    public final static EntityDataAccessor<Direction> direction = SynchedEntityData.defineId(LightningTurretEntity.class, EntityDataSerializers.DIRECTION);
    private int life;

    public LightningTurretEntity(EntityType<? extends LightningTurretEntity> entityType, Level level) {
        super(entityType, level);
        life = 300;
        this.noPhysics = true;
    }

    public LightningTurretEntity(Level level, LivingEntity livingEntity, Direction attachDir) {
        super(AArcanaEntities.LIGHTNING_TURRET_ENTITY.get(), level);
        life = 300;
        entityData.set(direction, attachDir);
        setOwner(livingEntity);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        BlockPos attachPos = blockPosition().mutable().relative(entityData.get(direction));
        BlockState blockState = level().getBlockState(attachPos);
        if (!blockState.isSolidRender(level(), attachPos)) {
            discard();
        }
        if (life != 300 && life % 40 == 0) {
            if (!level().isClientSide()){
                ServerLevel serverLevel = (ServerLevel) level();
                List<Entity> targets = level().getEntities(getOwner(), AABB.unitCubeFromLowerCorner(position().subtract(0.5F, 0.5F, 0.5F)).inflate(5F), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(this::notOwnerAlly));
                if (!targets.isEmpty()) {
                    LivingEntity target = (LivingEntity) targets.get(serverLevel.getRandom().nextIntBetweenInclusive(0, targets.size() - 1));
                    AArcanaEnchantmentHelper.joltTargets(target, (LivingEntity) getOwner(), this, 3);
                }
            }
        }
        life--;
//        if (life <= 0) {
//            discard();
//        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(direction, Direction.DOWN);
    }
}
