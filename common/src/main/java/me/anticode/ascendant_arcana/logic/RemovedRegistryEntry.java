package me.anticode.ascendant_arcana.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public record RemovedRegistryEntry(Enchantment enchantment, ResourceLocation identifier, int rawId) {
    public static final Set<RemovedRegistryEntry> REMOVED_ENTRIES = new HashSet<>();

    @Nullable
    public static RemovedRegistryEntry getFromId(ResourceLocation identifier) {
        for (RemovedRegistryEntry entry : REMOVED_ENTRIES) {
            if (entry.identifier.equals(identifier)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static RemovedRegistryEntry getFromEnchantment(Enchantment enchantment) {
        for (RemovedRegistryEntry entry : REMOVED_ENTRIES) {
            if (entry.enchantment.equals(enchantment)) {
                return entry;
            }
        }
        return null;
    }
}