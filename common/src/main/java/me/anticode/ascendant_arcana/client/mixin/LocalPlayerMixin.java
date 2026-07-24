package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow @Final
    protected Minecraft minecraft;

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;hasForwardImpulse()Z"))
    private boolean preventSprintCancelWithStrafe(Input instance, Operation<Boolean> original) {
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), (LocalPlayer)(Object)this) >= 1) return ascendant_arcana$hasMovement(instance);
        return original.call(instance);
    }

    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F))
    private float modifyUseMovementPenalty(float value) {
        if (((AArcanaPlayer)(Object)this).ascendant_arcana$getShieldBashStatus()) return 1F;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), (LocalPlayer)(Object)this) >= 1) return 0.6F;
        return value;
    }

    @WrapOperation(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEnoughImpulseToStartSprinting()Z"))
    private boolean startSprintWithStrafe(LocalPlayer instance, Operation<Boolean> original) {
        if (!instance.isUnderWater() && EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), instance) >= 1) {
            return (Mth.abs(instance.input.forwardImpulse) >= 0.8 || Mth.abs(instance.input.leftImpulse) >= 0.8)
                    && minecraft.options.keySprint.isDown();
        }
        return original.call(instance);
    }

    @Unique
    private boolean ascendant_arcana$hasMovement(Input instance) {
        return Mth.abs(instance.forwardImpulse) > 1.0E-5F || Mth.abs(instance.leftImpulse) > 1.0E-5F;
    }
}
