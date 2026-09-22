package me.anticode.ascendant_arcana.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.anticode.ascendant_arcana.init.AArcanaParticles;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChainingLightningParticleOption implements ParticleOptions {
    public static final Codec<ChainingLightningParticleOption> CODEC = RecordCodecBuilder.create((instance) -> instance.group(new ListCodec<>(Codec.INT).fieldOf("entities").forGetter((particleOptions) -> particleOptions.entities)).apply(instance, ChainingLightningParticleOption::new));
    public static final ParticleOptions.Deserializer<ChainingLightningParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ChainingLightningParticleOption>() {
        @Override
        public ChainingLightningParticleOption fromCommand(ParticleType<ChainingLightningParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            return new ChainingLightningParticleOption(List.of());
        }

        @Override
        public @NotNull ChainingLightningParticleOption fromNetwork(ParticleType<ChainingLightningParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
            int length = friendlyByteBuf.readInt();
            List<Integer> entities = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                entities.add(friendlyByteBuf.readInt());
            }
            return new ChainingLightningParticleOption(entities);
        }
    };

    public ChainingLightningParticleOption(List<Integer> entities) {
        this.entities = entities;
    }

    private final List<Integer> entities;

    @Override
    public @NotNull ParticleType<?> getType() {
        return AArcanaParticles.CHAINING_LIGHTNING.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.entities.size());
        for (Integer entity : this.entities) {
            friendlyByteBuf.writeInt(entity);
        }
    }

    public static ChainingLightningParticleOption fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        int length = friendlyByteBuf.readInt();
        List<Integer> entities = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            entities.add(friendlyByteBuf.readInt());
        }
        return new ChainingLightningParticleOption(entities);
    }

    public List<Entity> getEntities(Level level) {
        List<Entity> entities = new ArrayList<>();
        for (int entityId : this.entities) {
            entities.add(level.getEntity(entityId));
        }
        return entities;
    }

    @Override
    public @NotNull String writeToString() {
        StringBuilder string = new StringBuilder();
        string.append(BuiltInRegistries.PARTICLE_TYPE.getKey(getType())).append("[");
        for (int id : entities) {
            string.append(" ").append(id);
        }
        string.append("]");
        return string.toString();

    }
}
