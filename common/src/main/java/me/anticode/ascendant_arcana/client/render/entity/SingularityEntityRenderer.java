package me.anticode.ascendant_arcana.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.model.entity.SingularityModel;
import me.anticode.ascendant_arcana.entity.SingularityEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SingularityEntityRenderer extends EntityRenderer<SingularityEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "textures/entity/singularity.png");
    private final SingularityModel singularity;

    public SingularityEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
        singularity = new SingularityModel(context.bakeLayer(SingularityModel.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SingularityEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SingularityEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        SynchedEntityData entityData = entity.getEntityData();
        int life = entityData.get(SingularityEntity.life);
        int time = entityData.get(SingularityEntity.maxLife) - life;
        int cyclicalTime = 30 - entity.getCyclicalLife();
        float scale = Mth.lerp(g, Math.min((time - 1)/3F, 1F), Math.min(time / 3F, 1F));
        // This is incredibly cursed.
        float ringScale = easeInOutQuad(Mth.lerp(g, Math.min(time - 1, 10F)/10F, Math.min(time, 10F)/10F)) * 10F;
        if (cyclicalTime >= 15 && cyclicalTime <= 20) ringScale = easeInOutQuad(Mth.map(Mth.lerp(g, cyclicalTime - 1, cyclicalTime), 15, 20, 9, 0) / 10F) * 10F + 1;
        if (cyclicalTime > 20 && life > 30) {
            ringScale = easeInOutQuad(Mth.lerp(g, Math.min(cyclicalTime - 20, 10F)/10F, Math.min(cyclicalTime - 19, 10F)/10F)) * 10F;
        } else if (cyclicalTime > 20) {
            ringScale = 1;
        }
        if (life <= 5) {
            float fac = (life - g) / 5F;
            ringScale = Math.max(fac, 0);
            scale = ringScale;
        }
        poseStack.scale(ringScale, 1, ringScale);
        poseStack.mulPose(Axis.YP.rotation((Minecraft.getInstance().getFrameTime() - entity.getEntityData().get(SingularityEntity.life)) * 0.2F));
        singularity.ring.render(poseStack, multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(TEXTURE)), i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotation((Minecraft.getInstance().getFrameTime() - entity.getEntityData().get(SingularityEntity.life)) * -0.1F));
        singularity.outline.render(poseStack, multiBufferSource.getBuffer(RenderType.eyes(TEXTURE)), i, OverlayTexture.NO_OVERLAY);
        singularity.singularity.render(poseStack, multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(TEXTURE)), i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    private float easeInOutQuad(float x) {
        return (float) (x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);
    }
}
