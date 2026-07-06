package me.anticode.ascendant_arcana.forge;

import me.anticode.ascendant_arcana.AscendantArcana;
import dev.architectury.platform.forge.EventBuses;
import me.anticode.ascendant_arcana.api.ItemEntryAccess;
import me.anticode.ascendant_arcana.api.LeafEntryAccess;
import me.anticode.ascendant_arcana.client.AscendantArcanaClient;
import me.anticode.ascendant_arcana.client.screen.AArcanaEnchantingScreen;
import me.anticode.ascendant_arcana.forge.api.LootPoolAccess;
import me.anticode.ascendant_arcana.forge.api.LootTableAccess;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.Relics;
import me.anticode.ascendant_arcana.loot.PopulateRelicLootFunction;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AscendantArcanaForgeEvents {
        @SubscribeEvent
        public static void lootLoad(LootTableLoadEvent event) {
            ResourceLocation identifier = event.getName();
            LootTable lootTable = event.getTable();
            List<LootPool> lootPoolList = new ArrayList<>(((LootTableAccess)lootTable).ascendant_arcana$getLootPools());
            if (identifier.getPath().contains("chests") && AscendantArcana.config.add_relics_to_chests || AscendantArcana.config.add_restorine_to_chests) {
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
                                entryBuilder.apply(PopulateRelicLootFunction.builder(UniformGenerator.between(1, !bonus ? 3 : 4), new int[]{0, 1, 2, 4}));
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
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class AscendantArcanaForgeClient {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            AscendantArcanaClient.initialize();

            event.enqueueWork(() -> {
                MenuScreens.register(AArcanaMenus.ENCHANTING.get(), AArcanaEnchantingScreen::new);

                ItemProperties.register(AArcanaItems.RELIC.get(), ResourceLocation.tryBuild("minecraft", "relic_type"), ((itemStack, clientLevel, livingEntity, i) -> Relics.toId(RelicItem.getRelicType(itemStack)) / 5F));
                ItemProperties.register(AArcanaItems.RELIC.get(), ResourceLocation.tryBuild("minecraft", "relic_strength"), (itemStack, clientWorld, livingEntity, seed) -> RelicItem.getRelicStrength(itemStack) / 5F);
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(AArcanaBlocks.COPPER_ENCHANTING_TABLE_BLOCK_ENTITY.get(), EnchantTableRenderer::new);
        }
    }
}
