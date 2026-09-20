package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class AArcanaSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BLAZEBOLT_SHOT = register("blazebolt_shot");
    public static final RegistrySupplier<SoundEvent> SINGULARITY = register("singularity");
    public static final RegistrySupplier<SoundEvent> SINGULARITY_SUMMON = register("singularity_summon");
    public static final RegistrySupplier<SoundEvent> SHIELD_BASH_START = register("shield_bash_start");
    public static final RegistrySupplier<SoundEvent> SHIELD_BASH_HIT = register("shield_bash_hit");
    public static final RegistrySupplier<SoundEvent> SHATTERSHOT = register("shattershot");
    public static final RegistrySupplier<SoundEvent> TICK = register("tick");
    public static final RegistrySupplier<SoundEvent> STORM_ANCHOR_ZAP = register("storm_anchor_zap");
    public static final RegistrySupplier<SoundEvent> LIGHTNING_ZAP = register("lightning_zap");
    public static final RegistrySupplier<SoundEvent> GLACIOCLASM = register("glacioclasm");
    public static final RegistrySupplier<SoundEvent> GLACIOCLASM_SPAWN = register("glacioclasm_spawn");

    public static RegistrySupplier<SoundEvent> register(String id) {
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.tryBuild(AscendantArcana.MOD_ID, id)));
    }

    public static void initialize() {
        SOUND_EVENTS.register();
    }
}
