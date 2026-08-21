package me.anticode.ascendant_arcana.forge;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import dev.architectury.platform.forge.EventBuses;
import me.anticode.ascendant_arcana.api.ItemEntryAccess;
import me.anticode.ascendant_arcana.api.LeafEntryAccess;
import me.anticode.ascendant_arcana.client.AscendantArcanaClient;
import me.anticode.ascendant_arcana.client.model.entity.SingularityModel;
import me.anticode.ascendant_arcana.client.render.entity.BlazeboltEntityRenderer;
import me.anticode.ascendant_arcana.client.render.entity.SingularityEntityRenderer;
import me.anticode.ascendant_arcana.client.screen.AArcanaEnchantingScreen;
import me.anticode.ascendant_arcana.forge.api.LootPoolAccess;
import me.anticode.ascendant_arcana.forge.api.LootTableAccess;
import me.anticode.ascendant_arcana.init.*;
import me.anticode.ascendant_arcana.loot.PopulateRelicLootFunction;
import me.anticode.ascendant_arcana.networking.RelicRegistrySync;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod(AscendantArcana.MOD_ID)
public final class AscendantArcanaForge {
    public AscendantArcanaForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(AscendantArcana.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        // For some reason Forge requires attributes to be registered twice
        AArcanaAttributes.initialize();
        AscendantArcana.initialize();
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class AscendantArcanaModEvents {
        @SubscribeEvent
        public static void existingEntityAttributes(EntityAttributeModificationEvent event) {
            for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
                event.add(entityType, AArcanaAttributes.PROTECTION.get());
                event.add(entityType, AArcanaAttributes.DRAW_SPEED.get());
                event.add(entityType, AArcanaAttributes.DAMAGE_TAKEN.get());
            }
        }

        @SubscribeEvent
        public static void addPackFinders(AddPackFindersEvent event) {
            if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
            Path packPath = ModList.get().getModFileById(AscendantArcana.MOD_ID).getFile().findResource("resourcepacks", "ascendant_arcana_classic");
            event.addRepositorySource(consumer -> consumer.accept(Pack.readMetaAndCreate(
                    "builtin/ascendant_arcana_classic",
                    Component.translatable("pack.ascendant_arcana.ascendant_arcana_classic"),
                    false,
                    (path) -> new PathPackResources(
                            path,
                            packPath,
                            false),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.BOTTOM,
                    PackSource.BUILT_IN
            )));
        }
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AscendantArcanaForgeEvents {
        @SubscribeEvent
        public static void lootLoad(LootTableLoadEvent event) {
            ResourceLocation identifier = event.getName();
            LootTable lootTable = event.getTable();
            List<LootPool> lootPoolList = new ArrayList<>(((LootTableAccess)lootTable).ascendant_arcana$getLootPools());
            if (identifier.getPath().contains("chests") && (AscendantArcana.config.add_relics_to_chests || AscendantArcana.config.add_restorine_to_chests)) {
                for (LootPool lootPool : lootPoolList) {
                    LootPool.Builder poolBuilder = LootPool.lootPool();
                    LootPoolEntryContainer[] entries = ((LootPoolAccess)lootPool).getEntries();
                    int totalWeight = 0;
                    int addedWeights = 0;
                    for (LootPoolEntryContainer entry : entries) {
                        if (entry instanceof LootItem lootItem) {
                            Item item = ((ItemEntryAccess)lootItem).ascendantArcana$getItem();
                            if (item == Items.BOOK && AscendantArcana.config.add_relics_to_chests) {
                                boolean enchanted = false;
                                boolean bonus = false;
                                for (LootItemFunction function : ((LeafEntryAccess) entry).ascendantArcana$getFunctions()) {
                                    if (function instanceof EnchantRandomlyFunction || function instanceof EnchantWithLevelsFunction) {
                                        enchanted = true;
                                        if (function instanceof EnchantRandomlyFunction) bonus = true;
                                        break;
                                    }
                                }
                                if (!enchanted) continue;
                                LootPoolSingletonContainer.Builder<?> entryBuilder = LootItem.lootTableItem(AArcanaItems.RELIC.get());
                                entryBuilder.setWeight(((LeafEntryAccess) entry).ascendantArcana$getWeight());
                                addedWeights += ((LeafEntryAccess) entry).ascendantArcana$getWeight();
                                entryBuilder.setQuality(((LeafEntryAccess) entry).ascendantArcana$getQuality());
                                entryBuilder.apply(PopulateRelicLootFunction.builder(UniformGenerator.between(1, !bonus ? 3 : 4), new ResourceLocation[]{RelicTypes.DAMAGE, RelicTypes.DURABILITY, RelicTypes.PROTECTION, RelicTypes.HASTE, RelicTypes.ENCHANTMENT_CAPACITY}));
                                // I can't figure out how to replicate conditions, so in the off chance the enchanted book has a conditional drop, we will unfortunately ignore it
                                poolBuilder.add(entryBuilder);
                            } else if ((((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.AMETHYST_SHARD || ((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.DIAMOND) && AscendantArcana.config.add_restorine_to_chests) {
                                LootPoolSingletonContainer.Builder<?> entryBuilder = LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 15)));
                                int targetWeight;
                                if (((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.DIAMOND) {
                                    targetWeight = ((LeafEntryAccess) entry).ascendantArcana$getWeight() / 4;
                                } else {
                                    targetWeight = ((LeafEntryAccess) entry).ascendantArcana$getWeight() / 2;
                                }
                                entryBuilder.setWeight(targetWeight);
                                addedWeights += targetWeight;
                                entryBuilder.setQuality(((LeafEntryAccess) entry).ascendantArcana$getQuality());
                                poolBuilder.add(entryBuilder);
                            }
                            totalWeight += ((LeafEntryAccess)entry).ascendantArcana$getWeight();
                        }
                    }
                    if (addedWeights != 0) {
                        poolBuilder.add(EmptyLootItem.emptyItem().setWeight(totalWeight - addedWeights));
                        poolBuilder.setRolls(lootPool.getRolls());
                        poolBuilder.setBonusRolls(lootPool.getBonusRolls());
                        lootTable.addPool(poolBuilder.build());
                    }
                }
            }
        }

        @SubscribeEvent
        public static void serverStarting(ServerStartingEvent event) {
            RelicRegistry.loadRelics(event.getServer().getResourceManager());
        }

        @SubscribeEvent
        public static void datapackSync(OnDatapackSyncEvent event) {
            if (event.getPlayer() == null) return;
            NetworkManager.sendToPlayer(event.getPlayer(), RelicRegistrySync.Id, RelicRegistry.toNetwork());
        }
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class AscendantArcanaForgeClient {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            AscendantArcanaClient.initialize();

            event.enqueueWork(() -> {
                MenuScreens.register(AArcanaMenus.ENCHANTING.get(), AArcanaEnchantingScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(AArcanaBlocks.COPPER_ENCHANTING_TABLE_BLOCK_ENTITY.get(), EnchantTableRenderer::new);
            event.registerEntityRenderer(AArcanaEntities.BLAZEBOLT_ENTITY.get(), BlazeboltEntityRenderer::new);
            event.registerEntityRenderer(AArcanaEntities.SINGULARITY_ENTITY.get(), SingularityEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(SingularityModel.LAYER_LOCATION, SingularityModel::createBodyLayer);
        }
    }
}
