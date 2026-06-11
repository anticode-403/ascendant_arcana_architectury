package me.anticode.ascendant_arcana.forge.api;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public interface LootPoolAccess {
    LootPoolEntryContainer[] getEntries();
}
