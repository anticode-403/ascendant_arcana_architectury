package me.anticode.ascendant_arcana.init;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.particle.ChainingLightningParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public class AArcanaParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<ParticleType<ChainingLightningParticleOption>> CHAINING_LIGHTNING = PARTICLES.register("chaining_lightning", () -> new ParticleType<>(true, ChainingLightningParticleOption.DESERIALIZER) {
        @Override
        public @NotNull Codec<ChainingLightningParticleOption> codec() {
            return ChainingLightningParticleOption.CODEC;
        }
    });

    public static void initialize() {
        PARTICLES.register();
    }
}
