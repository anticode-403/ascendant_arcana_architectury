package me.anticode.ascendant_arcana.networking;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.resources.ResourceLocation;

public record RelicRegistrySync() {
    public static ResourceLocation Id = new ResourceLocation(AscendantArcana.MOD_ID, "relic_registry_sync");
}
