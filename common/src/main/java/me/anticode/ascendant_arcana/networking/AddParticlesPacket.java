package me.anticode.ascendant_arcana.networking;

import io.netty.buffer.Unpooled;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record AddParticlesPacket(String particleType, ParticleOptions particleOptions, int count, Vec3 pos, Vec3 vel, float posVariance, float velVariance, float speed) {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "add_particles");

    public FriendlyByteBuf write() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(particleType);
        particleOptions.writeToNetwork(buf);
        buf.writeInt(count);
        buf.writeVector3f(pos.toVector3f());
        buf.writeVector3f(vel.toVector3f());
        buf.writeFloat(posVariance);
        buf.writeFloat(velVariance);
        buf.writeFloat(speed);
        return buf;
    }

    public static AddParticlesPacket read(FriendlyByteBuf buf) {
        String particleType = buf.readUtf();
        ParticleOptions particleOptions;
        if (BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(particleType)) == ParticleTypes.BLOCK)
            particleOptions = BlockParticleOption.DESERIALIZER.fromNetwork(ParticleTypes.BLOCK, buf);
        else
            particleOptions = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(particleType));
        int count = buf.readInt();
        Vector3f posVector3f = buf.readVector3f();
        Vec3 pos = new Vec3(posVector3f.x, posVector3f.y, posVector3f.z);
        Vector3f velVector3f = buf.readVector3f();
        Vec3 vel = new Vec3(velVector3f.x, velVector3f.y, velVector3f.z);
        float posVariance = buf.readFloat();
        float velVariance = buf.readFloat();
        float speed = buf.readFloat();
        return new AddParticlesPacket(particleType, particleOptions, count, pos, vel, posVariance, velVariance, speed);
    }
}
