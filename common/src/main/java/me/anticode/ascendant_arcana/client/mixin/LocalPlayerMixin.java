package me.anticode.ascendant_arcana.client.mixin;

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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow @Final
    protected Minecraft minecraft;

    @Shadow
    protected abstract boolean hasEnoughImpulseToStartSprinting();

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;hasForwardImpulse()Z"))
    private boolean preventSprintCancelWithStrafe(Input instance) {
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), (LocalPlayer)(Object)this) >= 1) return ascendant_arcana$hasMovement(instance);
        return instance.hasForwardImpulse();
    }

    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F))
    private float modifyUseMovementPenalty(float value) {
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), (LocalPlayer)(Object)this) >= 1) return 0.6F;
        return value;
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEnoughImpulseToStartSprinting()Z"))
    private boolean startSprintWithStrafe(LocalPlayer instance) {
        if (!instance.isUnderWater() && EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STRAFE.get(), instance) >= 1) {
            return (Mth.abs(instance.input.forwardImpulse) >= 0.8 || Mth.abs(instance.input.leftImpulse) >= 0.8)
                    && minecraft.options.keySprint.isDown();
        }
        return hasEnoughImpulseToStartSprinting();
    }

    @Unique
    private boolean ascendant_arcana$hasMovement(Input instance) {
        return Mth.abs(instance.forwardImpulse) > 1.0E-5F || Mth.abs(instance.leftImpulse) > 1.0E-5F;
    }
}
