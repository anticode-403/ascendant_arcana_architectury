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

    public static RegistrySupplier<SoundEvent> register(String id) {
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.tryBuild(AscendantArcana.MOD_ID, id)));
    }

    public static void initialize() {
        SOUND_EVENTS.register();
    }
}
