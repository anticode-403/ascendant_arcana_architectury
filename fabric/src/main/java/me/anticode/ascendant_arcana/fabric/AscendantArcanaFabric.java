package me.anticode.ascendant_arcana.fabric;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.ItemEntryAccess;
import me.anticode.ascendant_arcana.api.LeafEntryAccess;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.loot.PopulateRelicLootFunction;
import me.anticode.ascendant_arcana.networking.RelicRegistrySync;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class AscendantArcanaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        AscendantArcana.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> {
            RelicRegistry.loadRelics(minecraftServer.getResourceManager());
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(((serverPlayer, b) -> {
            NetworkManager.sendToPlayer(serverPlayer, RelicRegistrySync.Id, RelicRegistry.toNetwork());
        }));

        LootTableEvents.MODIFY.register(((resourceManager, lootDataManager, identifier, builder, lootTableSource) -> {
            if (lootTableSource.isBuiltin() && AscendantArcana.config.add_relics_to_entities) {
                if (identifier.equals(ResourceLocation.tryBuild("minecraft", "gameplay/piglin_bartering"))) {
                    builder.modifyPools((poolBuilder) -> poolBuilder.add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(UniformGenerator.between(2, 4), new ResourceLocation[]{RelicTypes.HASTE})).setWeight(13)));
                } else if (identifier.getPath().contains("archaeology/")) {
                    builder.modifyPools((poolBuilder) -> poolBuilder.add(LootItem.lootTableItem(AArcanaItems.RELIC.get()).apply(PopulateRelicLootFunction.builder(UniformGenerator.between(2, 4), new ResourceLocation[]{RelicTypes.DURABILITY}))));
                }
            }
            if (identifier.getPath().contains("chests") && (AscendantArcana.config.add_relics_to_chests || AscendantArcana.config.add_restorine_to_chests)) {
                builder.modifyPools((poolBuilder) -> {
                    LootPool pool = poolBuilder.build();
                    for (LootPoolEntryContainer entry : pool.entries) {
                        if (entry instanceof LootItem) {
                            if (((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.BOOK && AscendantArcana.config.add_relics_to_chests) {
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
                                entryBuilder.setQuality(((LeafEntryAccess) entry).ascendantArcana$getQuality());
                                entryBuilder.apply(PopulateRelicLootFunction.builder(UniformGenerator.between(1, !bonus ? 3 : 4), new ResourceLocation[]{RelicTypes.DAMAGE, RelicTypes.DURABILITY, RelicTypes.PROTECTION, RelicTypes.ENCHANTMENT_CAPACITY}));
                                // I can't figure out how to replicate conditions, so in the off chance the enchanted book has a conditional drop, we will unfortunately ignore it
                                poolBuilder.with(entryBuilder.build());
                            } else if ((((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.AMETHYST_SHARD || ((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.DIAMOND) && AscendantArcana.config.add_restorine_to_chests) {
                                LootPoolSingletonContainer.Builder<?> entryBuilder = LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 15)));
                                if (((ItemEntryAccess)entry).ascendantArcana$getItem() == Items.DIAMOND)
                                    entryBuilder.setWeight(((LeafEntryAccess) entry).ascendantArcana$getWeight() / 4);
                                else
                                    entryBuilder.setWeight(((LeafEntryAccess) entry).ascendantArcana$getWeight() / 2);
                                entryBuilder.setQuality(((LeafEntryAccess) entry).ascendantArcana$getQuality());
                                poolBuilder.with(entryBuilder.build());
                            }
                        }
                    }
                });
            }
        }));
    }
}
