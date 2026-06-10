package me.anticode.ascendant_arcana.fabric;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.fabricmc.api.ModInitializer;

public final class AscendantArcanaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        AscendantArcana.initialize();
    }
}
