package me.anticode.ascendant_arcana.init;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AArcanaDamage {
    public static final ResourceKey<DamageType> BLAZEBOLT = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryBuild(AscendantArcana.MOD_ID, "blazebolt"));

    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolder(key).get(), source, attacker);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolder(key).get(), attacker);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolder(key).get());
    }
}
