package me.anticode.ascendant_arcana.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.entity.LightningTurretEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class LightningTurretModel extends EntityModel<LightningTurretEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(AscendantArcana.MOD_ID, "lightning_turret"), "main");
    public final ModelPart lightningTurret;
    public final ModelPart top;
    public final ModelPart outline;

    public LightningTurretModel(ModelPart root) {
        this.lightningTurret = root.getChild("lightning_turret");
        this.top = this.lightningTurret.getChild("top");
        this.outline = root.getChild("outline");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lightningTurret = partdefinition.addOrReplaceChild("lightning_turret", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -8.0F, -2.5F, 5.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition top = lightningTurret.addOrReplaceChild("top", CubeListBuilder.create().texOffs(20, 0).addBox(4.0F, -18.0F, -12.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 2.0F, 8.0F));

        PartDefinition outline = partdefinition.addOrReplaceChild("outline", CubeListBuilder.create().texOffs(49, 54).addBox(12.0F, -9.0F, -4.0F, -8.0F, -8.0F, -8.0F, new CubeDeformation(-0.5F)), PartPose.offset(-8.0F, 17.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LightningTurretEntity entity, float f, float g, float h, float i, float j) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        lightningTurret.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
