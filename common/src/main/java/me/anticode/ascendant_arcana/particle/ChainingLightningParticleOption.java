package me.anticode.ascendant_arcana.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.anticode.ascendant_arcana.codecs.EntityListSource;
import me.anticode.ascendant_arcana.init.AArcanaParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChainingLightningParticleOption implements ParticleOptions {
    public static final Codec<ChainingLightningParticleOption> CODEC = RecordCodecBuilder.create((instance) -> instance.group(EntityListSource.CODEC.fieldOf("entities").forGetter((particleOptions) -> particleOptions.entities)).apply(instance, ChainingLightningParticleOption::new));
    public static final ParticleOptions.Deserializer<ChainingLightningParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ChainingLightningParticleOption>() {
        @Override
        public ChainingLightningParticleOption fromCommand(ParticleType<ChainingLightningParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            return new ChainingLightningParticleOption(new EntityListSource(List.of()));
        }

        @Override
        public @NotNull ChainingLightningParticleOption fromNetwork(ParticleType<ChainingLightningParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
            return new ChainingLightningParticleOption(EntityListSource.read(friendlyByteBuf));
        }
    };

    public ChainingLightningParticleOption(EntityListSource entities) {
        this.entities = entities;
    }

    private final EntityListSource entities;

    @Override
    public @NotNull ParticleType<?> getType() {
        return AArcanaParticles.CHAINING_LIGHTNING.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        EntityListSource.write(friendlyByteBuf, entities);
    }

    public List<Vec3> getPositions() {
        List<Vec3> positions = new ArrayList<>();
        for (EntityPositionSource positionSource : entities.entities) {
            positions.add(positionSource.getPosition(null).get());
        }
        return positions;
    }

    @Override
    public @NotNull String writeToString() {
        StringBuilder string = new StringBuilder();
        string.append(BuiltInRegistries.PARTICLE_TYPE.getKey(getType()));
        for (EntityPositionSource positionSource : entities.entities) {
            Vec3 position = positionSource.getPosition(null).get();
            string.append(" ").append(position.x).append(" ").append(position.y).append(" ").append(position.z);
        }
        return string.toString();

    }
}
