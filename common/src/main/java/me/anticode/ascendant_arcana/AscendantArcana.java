package me.anticode.ascendant_arcana;

import dev.architectury.event.events.common.LootEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.config.ServerConfig;
import me.anticode.ascendant_arcana.config.ServerConfigWrapper;
import me.anticode.ascendant_arcana.init.*;
import me.anticode.ascendant_arcana.loot.PopulateRelicLootFunction;
import me.anticode.ascendant_arcana.networking.EnchantingScreenRemoveRecipe;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSendRecipe;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

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

        LootEvent.MODIFY_LOOT_TABLE.register((dataManager, identifier, context, builtin) -> {
            if (builtin && (config.add_boss_drops || config.add_relics_to_entities)) {
                if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/warden"))) {
                    if (config.add_boss_drops) {
                        LootPool.Builder heartPool = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.WARDEN_HEART.get()));
                        context.addPool(heartPool.build());
                    }
                    if (config.add_relics_to_entities) {
                        LootPool.Builder relicPool = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(ConstantValue.exactly(5), new int[]{0,2,4})));
                        context.addPool(relicPool.build());
                    }
                }
                else if (config.add_relics_to_entities) {
                    if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/witch"))) {
                        LootPool.Builder poolBuilder = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(UniformGenerator.between(2, 4), new int[]{4})).setWeight(1)).add(EmptyLootItem.emptyItem().setWeight(19));
                        context.addPool(poolBuilder.build());
                    }
                    else if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/wither"))) {
                        LootPool.Builder relicPool = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(ConstantValue.exactly(5), new int[]{1,2,3})));
                        context.addPool(relicPool.build());
                    }
                    else if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/ender_dragon"))) {
                        LootPool.Builder relicPool = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(ConstantValue.exactly(5), new int[]{2,3})));
                        context.addPool(relicPool.build());
                    }
                    else if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/wither_skeleton"))) {
                        LootPool.Builder poolBuilder = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(UniformGenerator.between(2, 4), new int[]{0})).setWeight(1)).add(EmptyLootItem.emptyItem().setWeight(19));
                        context.addPool(poolBuilder.build());
                    }
                    else if (identifier.equals(ResourceLocation.tryBuild("minecraft", "entities/evoker"))) {
                        LootPool.Builder poolBuilder = LootPool.lootPool().add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(UniformGenerator.between(2, 4), new int[]{4})).setWeight(1)).add(EmptyLootItem.emptyItem().setWeight(19));
                        context.addPool(poolBuilder.build());
                    }
                }
            }
        });
    }

    public static void initializeConfigIfNull() {
        if (config != null) return;
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
    }
}
