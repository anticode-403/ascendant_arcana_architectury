package me.anticode.ascendant_arcana.forge;

import me.anticode.ascendant_arcana.AscendantArcana;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AscendantArcana.MOD_ID)
public final class AscendantArcanaForge {
    public AscendantArcanaForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(AscendantArcana.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        AscendantArcana.init();
    }
}
