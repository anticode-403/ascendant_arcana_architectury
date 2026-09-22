package me.anticode.ascendant_arcana.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.anticode.ascendant_arcana.client.render.types.AArcanaRenderTypes;
import me.anticode.ascendant_arcana.entity.LightningTurretEntity;
import me.anticode.ascendant_arcana.particle.ChainingLightningParticleOption;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ChainingLightningParticle extends Particle {
    private final List<Entity> targets;
    private List<Vec3> positions;
    private final RenderType renderType;

    protected ChainingLightningParticle(ClientLevel clientLevel, double d, double e, double f, List<Entity> targets) {
        super(clientLevel, d, e, f);
        this.gravity = 0.0F;
        this.lifetime = 7;
        this.targets = targets;
        this.renderType = AArcanaRenderTypes.lightning();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        if (age < 3 && positions == null) {
            positions = getPositions();
        } else if (age >= 3 && age <= 4) {
            positions = null;
            return;
        } else if (positions == null){
            positions = getPositions();
        }
        Vec3 lastPos = null;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer2 = bufferSource.getBuffer(this.renderType);
        PoseStack matrices = new PoseStack();
        Vec3 cameraPos = camera.getPosition();
        for (Vec3 pos : positions) {
            if (lastPos == null) {
                lastPos = pos;
                continue;
            }

            Vec3 startingPos = lastPos;
            matrices.pushPose();
            matrices.translate(startingPos.x - cameraPos.x, startingPos.y - cameraPos.y, startingPos.z - cameraPos.z);
            Vec3 direction = pos.subtract(startingPos);
            direction = direction.normalize();

            Vector3f from = new Vector3f(0, 1, 0);
            Vector3f to = new Vector3f(
                    (float) direction.x,
                    (float) direction.y,
                    (float) direction.z
            );

            Quaternionf rotation = from.rotationTo(to, new Quaternionf());

            matrices.mulPose(rotation);

            drawBox(matrices.last(), vertexConsumer2, 0.1F, (float)startingPos.distanceTo(pos));
            drawBox(matrices.last(), vertexConsumer2, 0.05F, (float)startingPos.distanceTo(pos));
            lastPos = pos;
            matrices.popPose();
        }
        bufferSource.endBatch();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    private List<Vec3> getPositions() {
        List<Vec3> positions = new ArrayList<>();
        for (Entity entity : targets) {
            if (entity instanceof LightningTurretEntity) positions.add(entity.position().with(Direction.Axis.Y, entity.getY(0.5F)));
            else positions.add(entity.position().with(Direction.Axis.Y, entity.getRandomY()));
        }
        return positions;
    }

    private static void drawBox(PoseStack.Pose entry, VertexConsumer vertices, float width, float length) {
        drawQuad(entry, vertices, -width, width, width*2, length, true);
        drawQuad(entry, vertices, -width, -width, width*2, length, false);
        drawQuad(entry, vertices, -width, -width, width*2, length, true);
        drawQuad(entry, vertices, width, -width, width*2, length, false);
    }

    private static void drawQuad(PoseStack.Pose entry, VertexConsumer vertices, float x, float z, float width, float length, boolean renderDown) {
        drawVertex(entry, vertices, x, length, z);
        drawVertex(entry, vertices, x, 0, z);
        if (renderDown) x += width;
        else z += width;
        drawVertex(entry, vertices, x, 0, z);
        drawVertex(entry, vertices, x, length, z);
    }

    private static void drawVertex(PoseStack.Pose entry, VertexConsumer vertices, float x, float y, float z) {
        vertices.vertex(entry.pose(), x, y, z).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
    }

    public static class Provider implements ParticleProvider<ChainingLightningParticleOption> {
        public Provider(SpriteSet spriteSet) {

        }

        @Override
        public Particle createParticle(ChainingLightningParticleOption particleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new ChainingLightningParticle(clientLevel, d, e, f, particleOption.getEntities(clientLevel));
        }
    }
}
