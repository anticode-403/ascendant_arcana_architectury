package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackEffectLayerMixin<T extends LivingEntity> {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAutoSpinAttack()Z"))
    public boolean isAutoSpinAttackOrShieldBash(LivingEntity instance, Operation<Boolean> original) {
        if (instance instanceof AArcanaPlayer player) return original.call(instance) || player.ascendant_arcana$getShieldBashStatus();
        return original.call(instance);
    }
}
