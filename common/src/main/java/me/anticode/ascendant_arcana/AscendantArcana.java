package me.anticode.ascendant_arcana;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.config.ServerConfig;
import me.anticode.ascendant_arcana.config.ServerConfigWrapper;
import me.anticode.ascendant_arcana.init.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class AscendantArcana {
    public static final String MOD_ID = "ascendant_arcana";
    public static ServerConfig config;

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> ASCENDANT_ARCANA_TAB = TABS.register("ascendant_arcana", () -> CreativeTabRegistry.create(
            Component.translatable("category.ascendant_arcana"),
            () -> new ItemStack(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get())
    ));

    public static void initialize() {
        initializeConfigIfNull();

        AArcanaBlocks.initialize();
        AArcanaItems.initialize();
        AArcanaTags.initialize();
        AArcanaRecipes.initialize();
        AArcanaEnchantments.initialize();
        AArcanaMobEffects.initialize();
        AArcanaMenus.initialize();
        AArcanaLootFunctionTypes.initialize();
        AArcanaFeatures.initialize();
        TABS.register();
    }

    public static void initializeConfigIfNull() {
        if (config != null) return;
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
    }
}
