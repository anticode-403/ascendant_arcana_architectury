package me.anticode.ascendant_arcana.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anticode.ascendant_arcana.entity.GlacioclasmEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class GlacioclasmModel extends EntityModel<GlacioclasmEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "freezing_burst"), "main");
    public final ModelPart freezing_burst;
    public final ModelPart mist_outer;
    public final ModelPart mist;

    public GlacioclasmModel(ModelPart root) {
        this.freezing_burst = root.getChild("freezing_burst");
        this.mist_outer = root.getChild("mist_outer");
        this.mist = root.getChild("mist");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition freezing_burst = partdefinition.addOrReplaceChild("freezing_burst", CubeListBuilder.create().texOffs(1, 1).addBox(-11.5F, 0.0F, -11.5F, 23.0F, 0.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

        PartDefinition mist_outer = partdefinition.addOrReplaceChild("mist_outer", CubeListBuilder.create().texOffs(0, 148).addBox(-12.0F, 0.0F, -12.0F, 24.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition mist = partdefinition.addOrReplaceChild("mist", CubeListBuilder.create().texOffs(0, 24).addBox(-8.0F, -3.0F, -8.0F, 16.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        freezing_burst.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        mist_outer.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        mist.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(GlacioclasmEntity entity, float f, float g, float h, float i, float j) {

    }
}
