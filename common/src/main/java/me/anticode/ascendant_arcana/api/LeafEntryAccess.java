package me.anticode.ascendant_arcana.api;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public interface LeafEntryAccess {
    int ascendantArcana$getWeight();

    int ascendantArcana$getQuality();

    LootItemFunction[] ascendantArcana$getFunctions();
}
