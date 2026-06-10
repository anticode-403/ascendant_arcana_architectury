package me.anticode.ascendant_arcana;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.config.ServerConfig;
import me.anticode.ascendant_arcana.config.ServerConfigWrapper;
import me.anticode.ascendant_arcana.init.*;
import me.anticode.ascendant_arcana.networking.EnchantingScreenRemoveRecipe;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSendRecipe;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, EnchantingScreenRemoveRecipe.Id, (buf, packetContext) -> {
            Player player = packetContext.getPlayer();
            EnchantingScreenRemoveRecipe packet = EnchantingScreenRemoveRecipe.read(buf);
            if (player.containerMenu.containerId != packet.syncId()) return;
            AArcanaEnchantingMenu menu = (AArcanaEnchantingMenu) player.containerMenu;
            menu.recipe = null;
            menu.dumpContents(true);
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, EnchantingScreenSendRecipe.Id, (buf, packetContext) -> {
            Player player = packetContext.getPlayer();
            EnchantingScreenSendRecipe packet = EnchantingScreenSendRecipe.read(buf, player.level().getRecipeManager());
            if (player.containerMenu.containerId != packet.syncId()) return;
            AArcanaEnchantingMenu screenHandler = (AArcanaEnchantingMenu) player.containerMenu;
            screenHandler.recipe = packet.recipe();
        });
    }

    public static void initializeConfigIfNull() {
        if (config != null) return;
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
    }
}
