package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.api.AArcanaHorse;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackEffectLayerMixin<T extends LivingEntity> {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAutoSpinAttack()Z"))
    public boolean isAutoSpinAttackOrShieldBash(LivingEntity instance, Operation<Boolean> original) {
        if (instance instanceof AArcanaPlayer player) return original.call(instance) || player.ascendant_arcana$getShieldBashStatus();
        else if (instance instanceof AArcanaHorse horse) return horse.ascendant_arcana$getCharging();
        return original.call(instance);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"))
    private void changeRotationForShieldBash(PoseStack instance, Quaternionf quaternionf, Operation<Void> original, @Local(argsOnly = true) LivingEntity livingEntity, @Local(ordinal=6) float n) {
        if (livingEntity instanceof AArcanaPlayer player && player.ascendant_arcana$getShieldBashStatus()) {
            Vec3 direction = player.ascendant_arcana$getShieldBashDirection();
            instance.translate(0, 1, -2F);
            instance.mulPose(Axis.YP.rotationDegrees((float)Math.atan2(direction.x, direction.z)));
            instance.mulPose(Axis.XP.rotationDegrees(90));
            original.call(instance, Axis.YP.rotationDegrees(n));
            instance.scale(1.75F, 1.75F, 1.75F);
        } else if (livingEntity instanceof AArcanaHorse horse && horse.ascendant_arcana$getCharging()) {
            AbstractHorse abstractHorse = (AbstractHorse) horse;
            instance.translate(0, 0.5F, -2F);
            instance.mulPose(Axis.YP.rotationDegrees((float)Math.atan2(abstractHorse.getControllingPassenger().getDeltaMovement().normalize().x, abstractHorse.getControllingPassenger().getDeltaMovement().normalize().z)));
            instance.mulPose(Axis.XP.rotationDegrees(90));
            original.call(instance, Axis.YP.rotationDegrees(n * 0.5F));
            instance.scale(2F, 2F, 2F);
        } else original.call(instance, quaternionf);
    }
}
