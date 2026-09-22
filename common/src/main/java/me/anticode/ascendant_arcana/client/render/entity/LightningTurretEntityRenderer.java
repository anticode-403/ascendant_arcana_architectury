package me.anticode.ascendant_arcana.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.model.entity.LightningTurretModel;
import me.anticode.ascendant_arcana.client.render.types.AArcanaRenderTypes;
import me.anticode.ascendant_arcana.entity.LightningTurretEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LightningTurretEntityRenderer extends EntityRenderer<LightningTurretEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "textures/entity/lightning_turret.png");
    private final LightningTurretModel lightningTurretModel;

    public LightningTurretEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.lightningTurretModel = new LightningTurretModel(context.bakeLayer(LightningTurretModel.LAYER_LOCATION));
    }

    @Override
    public void render(LightningTurretEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        Direction facing = entity.getEntityData().get(LightningTurretEntity.direction);
        poseStack.translate(0, 0.25, 0);
        Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ()));
        poseStack.mulPose(rotation);
        poseStack.translate(0, -0.25, 0);
        lightningTurretModel.lightningTurret.render(poseStack, multiBufferSource.getBuffer(AArcanaRenderTypes.emissive(TEXTURE)), i, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        lightningTurretModel.outline.render(poseStack, multiBufferSource.getBuffer(AArcanaRenderTypes.emissiveBackfaceCull(TEXTURE)), i, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(LightningTurretEntity entity) {
        return TEXTURE;
    }
}
