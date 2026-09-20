package me.anticode.ascendant_arcana.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.model.entity.GlacioclasmModel;
import me.anticode.ascendant_arcana.client.render.types.AArcanaRenderTypes;
import me.anticode.ascendant_arcana.entity.GlacioclasmEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GlacioclasmEntityRenderer extends EntityRenderer<GlacioclasmEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "textures/entity/freezing_burst.png");
    private final GlacioclasmModel glacioclasm;

    public GlacioclasmEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        glacioclasm = new GlacioclasmModel(context.bakeLayer(GlacioclasmModel.LAYER_LOCATION));
    }

    @Override
    public void render(GlacioclasmEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        SynchedEntityData entityData = entity.getEntityData();
        int life = entityData.get(GlacioclasmEntity.life);
        int maxLife = entityData.get(GlacioclasmEntity.maxLife);
        int time = Math.max(0, entityData.get(GlacioclasmEntity.maxLife) - life);
        float scale = Mth.lerp(g, Math.min((time - 1)/Math.min(maxLife, 3F), 1F), Math.min(time / Math.min(maxLife, 3F), 1F)) * 1.5F;
        float ringScale = easeInOutQuad(Mth.lerp(g, Math.min(time - 1, 6F)/Math.min(maxLife, 10F), Math.min(time, 6F)/Math.min(maxLife, 10F)));
        float outerRingScale = ringScale;
        float ringHeightScale = 1F;
        if (life <= 4) {
            float multiplier = easeOutQuad(Mth.map(life - g, 4, -1, 0, 1));
            ringScale *= 1 + (multiplier * 7.5F);
            outerRingScale *= 1 + (multiplier * 9F);
        } if (life <= 1) {
            ringHeightScale = easeInOutQuad(Mth.map(life - g, 1, -1, 1, 0));
            scale *= ringHeightScale;
        }
        poseStack.scale(scale, 1, scale);
        poseStack.translate(0, -1.4, 0);
        poseStack.mulPose(Axis.YP.rotation((Minecraft.getInstance().getFrameTime() - life) * 0.2F));
        glacioclasm.freezing_burst.render(poseStack, multiBufferSource.getBuffer(AArcanaRenderTypes.emissive(TEXTURE)), i, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, Mth.map(ringHeightScale, 1, 0, 0, 0.75), 0);
        poseStack.scale(ringScale, ringHeightScale, ringScale);
        poseStack.mulPose(Axis.YP.rotation((Minecraft.getInstance().getFrameTime() - life) * -0.2F));
        glacioclasm.mist.render(poseStack, multiBufferSource.getBuffer(AArcanaRenderTypes.emissive(TEXTURE)), i, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, Mth.map(ringHeightScale, 1, 0, 0, 0.75), 0);
        poseStack.scale(outerRingScale, ringHeightScale, outerRingScale);
        poseStack.mulPose(Axis.YP.rotation((Minecraft.getInstance().getFrameTime() - life) * 0.1F));
        glacioclasm.mist_outer.render(poseStack, multiBufferSource.getBuffer(AArcanaRenderTypes.emissive(TEXTURE)), i, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    private float easeInOutQuad(float x) {
        return (float) (x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);
    }

    private float easeOutQuad(float x) {
        return 1 - (1 - x) * (1 - x);

    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(GlacioclasmEntity entity) {
        return TEXTURE;
    }
}
