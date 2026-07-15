package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.init.AArcanaDamage;
import me.anticode.ascendant_arcana.init.AArcanaEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BlazeboltEntity extends AbstractArrow {
    public final int maxLife = 8;
    public final int maxLength = 256;
    public int life;
    public Vec3 directionVec;
    public static EntityDataAccessor<Float> x = SynchedEntityData.defineId(BlazeboltEntity.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> y = SynchedEntityData.defineId(BlazeboltEntity.class, EntityDataSerializers.FLOAT);

    private final Set<Entity> hitEntities = new HashSet<>(), killedEntities = new HashSet<>();

    public BlazeboltEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        life = maxLife;
        noCulling = true;
    }

    public BlazeboltEntity(Level level, LivingEntity owner) {
        super(AArcanaEntities.BLAZEBOLT_ENTITY.get(), owner, level);
        life = maxLife;
        noCulling = true;
        setPos(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
        directionVec = getOwner().getLookAngle();
        entityData.set(x, getOwner().getXRot());
        entityData.set(y, getOwner().getYRot());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(x, 0F);
        entityData.define(y, 0F);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        if (isCritArrow()) {
            setCritArrow(false);
        }
        setDeltaMovement(Vec3.ZERO);
        int length = 0;
        if (getOwner() == null) {
            discard();
            return;
        }
        if (directionVec == null && life == maxLife) {
            directionVec = getOwner().getLookAngle();
        }
        if (life == maxLife) {
            Vec3 start = position(), end = start.add(directionVec);
            while (length < maxLength) {
                length++;
                BlockHitResult hitResult = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this));
                Entity owner = getOwner();
                level().getEntities(owner, AABB.unitCubeFromLowerCorner(hitResult.getLocation()).inflate(0.5), EntitySelector.NO_SPECTATORS.and(entity -> canEntityBeHit(owner, entity))).forEach(entity -> {
                    double damage = getBaseDamage();
                    entity.hurt(AArcanaDamage.source(level(), AArcanaDamage.BLAZEBOLT, this, owner), (float) damage);
                    entity.setSecondsOnFire(5);
                    hitEntities.add(entity);
                    if (entity instanceof LivingEntity living && living.isDeadOrDying()) {
                        killedEntities.add(living);
                    }
                });
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockPos = hitResult.getBlockPos().relative(hitResult.getDirection());
                    if (BaseFireBlock.canBePlacedAt(level(), blockPos, hitResult.getDirection()) && level().isEmptyBlock(blockPos)) {
                        level().setBlock(blockPos, BaseFireBlock.getState(this.level(), blockPos), 11);
                    }
                    break;
                }
                start = end;
                end = start.add(directionVec);
            }
            if (!level().isClientSide) {
                level().gameEvent(GameEvent.PROJECTILE_LAND, end, GameEvent.Context.of(this));
            }
        }
        if (!level().isClientSide) {
            if (life <= 0) {
                if (getOwner() instanceof ServerPlayer player) {
                    CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(player, killedEntities);
                }
                discard();
            }
        }
        life--;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        life = nbt.getInt("Life");
        entityData.set(x, nbt.getFloat("X"));
        entityData.set(y, nbt.getFloat("Y"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Life", life);
        nbt.putFloat("X", entityData.get(x));
        nbt.putFloat("Y", entityData.get(y));
    }

    private boolean canEntityBeHit(Entity owner, Entity entity) {
        if (entity instanceof LivingEntity) {
            return !hitEntities.contains(entity) && entity.isAlive() && owner != entity;
        }
        return false;
    }
}
