package me.anticode.ascendant_arcana.client.model.entity;
// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.entity.SingularityEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class SingularityModel extends EntityModel<SingularityEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(AscendantArcana.MOD_ID, "singularity"), "main");
    public final ModelPart singularity;
    public final ModelPart ring;
    public final ModelPart outline;

    public SingularityModel(ModelPart root) {
        this.singularity = root.getChild("singularity");
        this.ring = root.getChild("ring");
        this.outline = root.getChild("outline");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition singularity = partdefinition.addOrReplaceChild("singularity", CubeListBuilder.create().texOffs(0, 12).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0, 7F, 0));
        PartDefinition outline = partdefinition.addOrReplaceChild("outline", CubeListBuilder.create().texOffs(64, 16).addBox(3.0F, 0.0F, 3.0F, -6.0F, -6.0F, -6.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition ring = partdefinition.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        singularity.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(SingularityEntity entity, float f, float g, float h, float i, float j) {

    }
}