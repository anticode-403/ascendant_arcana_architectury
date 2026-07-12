package me.anticode.ascendant_arcana.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.entity.BlazeboltEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BlazeboltEntityRenderer extends ArrowRenderer<BlazeboltEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "textures/entity/blazebolt.png");

    public BlazeboltEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BlazeboltEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BlazeboltEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        float scale = Mth.lerp((entity.maxLife - entity.life) / 10F, 1, 0.0625F) * Mth.lerp((float) entity.getBaseDamage() / 12F, 0.1F, 1);
        float v = (Math.floorMod(entity.level().getGameTime(), 40) + tickDelta) / 4;
        float u = v + 4 * -0.5F / scale;
        VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.dragonExplosionAlpha(TEXTURE));
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(-entity.getEntityData().get(entity.y) + 90));
        matrices.mulPose(Axis.ZP.rotationDegrees((float)entity.getEntityData().get(entity.x) + 90));
        matrices.mulPose(Axis.YP.rotationDegrees((entity.level().getGameTime() + entity.life) * 12));
        matrices.scale(scale, 1, scale);
        PoseStack.Pose entry = matrices.last();
        for (int j = 0; j < entity.maxLength; j++) {
            for (int i = 0; i < 360; i += 15) {
                drawPlane(entry, vertices, u, v);
                matrices.mulPose(Axis.YP.rotationDegrees(i));
            }
            matrices.translate(0, 1, 0);
        }
        matrices.popPose();
    }

    private static void drawPlane(PoseStack.Pose entry, VertexConsumer vertices, float u, float v) {
        drawVertex(entry, vertices, 1, 0, 1, u);
        drawVertex(entry, vertices, 0, 0, 1, v);
        drawVertex(entry, vertices, 0, 0.25F, 0, v);
        drawVertex(entry, vertices, 1, 0.25F, 0, u);

        drawVertex(entry, vertices, 0, 0.25F, 0, v);
        drawVertex(entry, vertices, 0, 0, 1, v);
        drawVertex(entry, vertices, 1, 0, 1, u);
        drawVertex(entry, vertices, 1, 0.25F, 0, u);
    }

    private static void drawVertex(PoseStack.Pose entry, VertexConsumer vertices, int y, float z, float u, float v) {
        vertices.vertex(entry.pose(), 0, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(entry.normal(), 0, 1, 0).endVertex();
    }
}