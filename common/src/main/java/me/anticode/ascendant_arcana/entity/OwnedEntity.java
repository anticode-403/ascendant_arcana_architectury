package me.anticode.ascendant_arcana.entity;

import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class OwnedEntity extends Entity implements TraceableEntity {
    @Nullable
    protected UUID ownerUUID;
    @Nullable
    protected Entity cachedOwner;

    public OwnedEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
            this.cachedOwner = null;
        }
    }

    public boolean notOwnerAlly(Entity entity) {
        return AArcanaEnchantmentHelper.notAllyToEntity(this.getOwner(), entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public @NotNull SoundSource getSoundSource() {
        if (getOwner() != null) return getOwner().getSoundSource();
        else return SoundSource.PLAYERS;
    }

    @Override
    public @Nullable Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }
}
