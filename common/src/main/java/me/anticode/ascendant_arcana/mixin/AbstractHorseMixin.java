package me.anticode.ascendant_arcana.mixin;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.api.AArcanaHorse;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.networking.ChargingSync;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractHorse.class)
public class AbstractHorseMixin implements AArcanaHorse {
    @Shadow
    protected SimpleContainer inventory;

    @Unique
    private static final UUID CHARGING_UUID = UUID.fromString("028dd599-ae28-41d6-991e-dc498d82bba0");

    @Unique
    private boolean chargingMaxSpeed = false;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;tick()V", shift = At.Shift.AFTER))
    private void tickRidden(CallbackInfo ci) {
        if (!((Object)this instanceof Horse)) return;
        Horse horse = (Horse) (Object)this;
        if (horse.level().isClientSide()) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.CHARGING.get(), inventory.getItem(1)) <= 0) {
            if (horse.getAttributes().hasModifier(Attributes.MOVEMENT_SPEED, CHARGING_UUID)) {
                horse.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(CHARGING_UUID);
                chargingMaxSpeed = false;
                NetworkManager.sendToPlayers(((ServerLevel)horse.level()).players(), ChargingSync.Id, new ChargingSync(horse.getId(), false).write());
            }
        }
        if (!horse.isAlive()) return;
        LivingEntity rider = horse.getControllingPassenger();
        if (rider == null) return;
        if (rider.getDeltaMovement().with(Direction.Axis.Y, 0).length() * 43 < horse.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()) {
            if (horse.getAttributes().hasModifier(Attributes.MOVEMENT_SPEED, CHARGING_UUID)) {
                horse.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(CHARGING_UUID);
                chargingMaxSpeed = false;
                NetworkManager.sendToPlayers(((ServerLevel)horse.level()).players(), ChargingSync.Id, new ChargingSync(horse.getId(), false).write());
            }
            return;
        }
        double oldAmount;
        if (!horse.getAttributes().hasModifier(Attributes.MOVEMENT_SPEED, CHARGING_UUID)) oldAmount = 0;
        else {
            oldAmount = horse.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).getModifier(CHARGING_UUID).getAmount();
            horse.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(CHARGING_UUID);
        }
        double targetAmount = oldAmount < 2D ? oldAmount + 0.01D : 2D;
        horse.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(CHARGING_UUID, "charging", targetAmount, AttributeModifier.Operation.MULTIPLY_BASE));
        if (targetAmount == 2D) {
            chargingMaxSpeed = true;
            NetworkManager.sendToPlayers(((ServerLevel)horse.level()).players(), ChargingSync.Id, new ChargingSync(horse.getId(), true).write());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void chargingDamage(CallbackInfo ci) {
        if (!((Object)this instanceof Horse)) return;
        Horse horse = (Horse) (Object)this;
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.CHARGING.get(), inventory.getItem(1)) <= 0) return;
        if (!horse.isAlive()) return;
        if (horse.level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel)horse.level();
        LivingEntity rider = horse.getControllingPassenger();
        if (rider == null) return;
        if (!chargingMaxSpeed) return;
        Vec3 chargeDirection = rider.getDeltaMovement().normalize();
        AABB shieldbashBox = horse.getBoundingBox().move(chargeDirection.scale(horse.getBoundingBox().getXsize())).inflate(0.75);

        for (int i = 0; i < 2; i++) {
            for (Entity entity : serverLevel.getEntities(horse, shieldbashBox.move(chargeDirection.scale(i)))) {
                if (entity == rider || entity == horse) continue;
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.hurt(horse.damageSources().mobAttack(rider), 4);
                    livingEntity.knockback(1, -chargeDirection.x, -chargeDirection.z);
                }
            }
        }
    }

    @Override
    public void ascendant_arcana$setCharging(boolean status) {
        chargingMaxSpeed = status;
    }

    @Override
    public boolean ascendant_arcana$getCharging() {
        return chargingMaxSpeed;
    }
}
