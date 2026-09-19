package me.anticode.ascendant_arcana.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.api.EnchantedTrident;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTridentRenderer.class)
public abstract class ThrownTridentRendererMixin extends EntityRenderer<ThrownTrident> {
    @Shadow
    @Final
    private TridentModel model;

    @Shadow
    public abstract @NotNull ResourceLocation getTextureLocation(ThrownTrident tridentEntity);

    protected ThrownTridentRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void stuckTridents(ThrownTrident thrownTrident, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        EnchantedTrident enchantedTrident = (EnchantedTrident) thrownTrident;
        Entity stuckEntity = enchantedTrident.ascendant_arcana$getStuckEntity();
        if (stuckEntity != null) {
            poseStack.pushPose();
            poseStack.translate(-(thrownTrident.getX() - stuckEntity.getX()), -(thrownTrident.getY() - stuckEntity.getEyeY()), -(thrownTrident.getZ() - stuckEntity.getZ()));
            poseStack.mulPose(Axis.YP.rotationDegrees((thrownTrident.tickCount + tickDelta) * 3));
            poseStack.mulPose(Axis.ZP.rotationDegrees(60));
            poseStack.translate(0, -enchantedTrident.ascendant_arcana$getStabTicks(), 0);
            model.renderToBuffer(poseStack, ItemRenderer.getFoilBufferDirect(multiBufferSource, model.renderType(getTextureLocation(thrownTrident)), false, thrownTrident.isFoil()), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            poseStack.popPose();
            super.render(thrownTrident, yaw, tickDelta, poseStack, multiBufferSource, light);
            ci.cancel();
        }
    }
}
