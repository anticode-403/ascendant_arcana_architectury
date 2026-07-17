package me.anticode.ascendant_arcana.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.entity.BlazeboltEntity;
import net.minecraft.client.Minecraft;
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
        matrices.mulPose(Axis.YP.rotationDegrees((Minecraft.getInstance().getFrameTime() - entity.life) * 30));
        matrices.scale(scale, 1, scale);
        PoseStack.Pose entry = matrices.last();
        for (int j = 0; j < entity.maxLength; j++) {
            drawBox(entry, vertices, 0.5F, u, v);
            matrices.translate(0, 1, 0);
        }
        matrices.popPose();
    }

    private static void drawBox(PoseStack.Pose entry, VertexConsumer vertices, float width, float u, float v) {
        drawQuad(entry, vertices, -width, width, width*2, u, v, true);
        drawQuad(entry, vertices, -width, -width, width*2, u, v, false);
        drawQuad(entry, vertices, -width, -width, width*2, u, v, true);
        drawQuad(entry, vertices, width, -width, width*2, u, v, false);
    }

    private static void drawQuad(PoseStack.Pose entry, VertexConsumer vertices, float x, float z, float width, float u, float v, boolean renderDown) {
        drawVertex(entry, vertices, x, 1, z, 1, u);
        drawVertex(entry, vertices, x, 0, z, 1, v);
        if (renderDown) x += width;
        else z += width;
        drawVertex(entry, vertices, x, 0, z, 0, v);
        drawVertex(entry, vertices, x, 1, z, 0, u);
    }

    private static void drawVertex(PoseStack.Pose entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(entry.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(entry.normal(), 0, 1, 0).endVertex();
    }
}