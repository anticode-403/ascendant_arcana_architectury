package me.anticode.ascendant_arcana.logic;

import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.relics.RelicEntry;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RelicHelper {
    public static final String RELICS_KEY = "AscendantArcanaRelics";

    public static final String BONUS_RELIC_CAPACITY = "AscendantArcanaRelicCapacity";

    public static int getRelicCapacity(ItemStack stack) {
        int base_capacity = AscendantArcana.config.base_relic_capacity;
        if (AscendantArcana.config.base_relic_capacity_overrides.containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            base_capacity = AscendantArcana.config.base_relic_capacity_overrides.get(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        }
        if (!stack.hasTag()) {
            return base_capacity;
        }
        return base_capacity + stack.getTag().getInt(BONUS_RELIC_CAPACITY);
    }

    public static Map<RelicEntry, Integer> fromNbt(CompoundTag nbt) {
        if (nbt == null) return new HashMap<>();
        if (shouldConvertRelicNbtData(nbt)) convertRelicNbtData(nbt);
        return fromNbtList((ListTag) nbt.get(RELICS_KEY));
    }

    public static Map<RelicEntry, Integer> fromNbtList(ListTag list) {
        Map<RelicEntry, Integer> map = new HashMap<>();
        if (list == null) return map;
        for  (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            RelicEntry key = RelicRegistry.get(ResourceLocation.tryParse(tag.getString("id")));
            int value = tag.getInt("strength");
            map.put(key, value);
        }
        return map;
    }

    public static double applyAllRelicsOfType(ResourceLocation relicType, double base, CompoundTag tag) {
        Map<RelicEntry, Integer> map = fromNbt(tag);
        if (map.isEmpty()) return base;
        double returnValue = base;
        for (Map.Entry<RelicEntry, Integer> entry : map.entrySet()) {
            if (!entry.getKey().getType().equals(relicType)) continue;
            returnValue = entry.getKey().applyOperation(returnValue, entry.getValue());
        }
        return returnValue;
    }

    public static boolean containsAnyOfType(ResourceLocation relicType, CompoundTag tag) {
        Map<RelicEntry, Integer> map = fromNbt(tag);
        for (Map.Entry<RelicEntry, Integer> entry : map.entrySet()) {
            if (entry.getKey().getType().equals(relicType)) return true;
        }
        return false;
    }

    public static boolean shouldConvertRelicNbtData(CompoundTag nbt) {
        if (nbt == null) return false;
        if (!nbt.contains(RELICS_KEY)) return false;
        return ((ListTag) nbt.get(RELICS_KEY)).getCompound(0).get("id") instanceof IntTag;
    }

    public static void convertRelicNbtData(CompoundTag nbt) {
        if (nbt == null) return;
        ListTag list = (ListTag) nbt.get(RELICS_KEY);
        ListTag newList = new ListTag();
        if (list == null) return;
        for  (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            CompoundTag newTag = new CompoundTag();
            newTag.putString("id", convertFromOldRelicIds(tag.getInt("id")).toString());
            newTag.putInt("strength", tag.getInt("strength"));
            newList.add(newTag);
        }
        nbt.put(RELICS_KEY, newList);
    }

    public static ResourceLocation convertFromOldRelicIds(int oldId) {
        return switch (oldId) {
            case 1 -> new ResourceLocation(AscendantArcana.MOD_ID, "durability");
            case 2 -> new ResourceLocation(AscendantArcana.MOD_ID, "protection");
            case 3 -> new ResourceLocation(AscendantArcana.MOD_ID, "haste");
            case 4 -> new ResourceLocation(AscendantArcana.MOD_ID, "enchantment_capacity");
            default -> new ResourceLocation(AscendantArcana.MOD_ID, "damage");
        };
    }

    public static ListTag toNbt(Map<RelicEntry, Integer> map) {
        ListTag nbtList = new ListTag();
        for(Map.Entry<RelicEntry, Integer> entry : map.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", RelicRegistry.getId(entry.getKey()).toString());
            tag.putInt("strength", entry.getValue());
            nbtList.add(tag);
        }
        return nbtList;
    }

    public static boolean canApplyRelic (ItemStack stack, RelicEntry relic, int strength) {
        if (!stack.hasTag() && getRelicCapacity(stack) > 0) return true;
        Map<RelicEntry, Integer> relics = fromNbt(stack.getTag());
        if (relics.containsKey(relic) && strength > relics.get(relic)) return true;
        else return getRelicCapacity(stack) > relics.keySet().size();
    }

    public static void infuseRelic(ItemStack stack, RelicEntry relicType, int strength) {
        Map<RelicEntry, Integer> relics = fromNbt(stack.getTag());
        relics.put(relicType, strength);
        stack.getOrCreateTag().putInt(RELICS_KEY, relics.size());
    }

    public static int getValueFromNbt(CompoundTag nbt, RelicEntry key) {
        if (nbt == null) return 0;
        Map<RelicEntry, Integer> map = fromNbt(nbt);
        return map.get(key) != null ? map.get(key) : 0;
    }

    public static double applyRelicBonus(RelicEntry relicEntry, double base, int strength) {
        if (strength == 0) return 0;
        return relicEntry.applyOperation(base, strength);
    }

    public static double applyStrengthFromNbt(RelicEntry relicType, double base, CompoundTag nbt) {
        return applyRelicBonus(relicType, base, getValueFromNbt(nbt, relicType));
    }

    public static double getRawBonus(RelicEntry relicEntry, int strength) {
        return relicEntry.getStrength(strength);
    }

    public static double getAllRawBonusesOfType(ResourceLocation relicType, CompoundTag tag) {
        Map<RelicEntry, Integer> map = fromNbt(tag);
        if (map.isEmpty()) return 0;
        double returnValue = 0;
        for (Map.Entry<RelicEntry, Integer> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                // SOMETHING HAS GONE VERY WRONG!!
                continue;
            }
            if (!entry.getKey().getType().equals(relicType)) continue;
            returnValue = entry.getKey().getStrength(entry.getValue());
        }
        return returnValue;
    }

    public static int getTooltipStrength(RelicEntry relicEntry, int strength) {
        if (strength == 0) return 0;
        double base = getRawBonus(relicEntry, strength);
        return switch (relicEntry.getOperation()) {
            case multiply_total -> Mth.floor(base * 100);
            case addition -> (int) base;
        };
    }

    public static Component getRelicTypeText(RelicEntry relicType) {
        ResourceLocation resourceLocation = relicType.getType();
        return Component.translatable("item." + resourceLocation.getNamespace() + ".relics.type." + resourceLocation.getPath());
    }

    public static Component getRelicStrengthName(int strength) {
        return Component.translatable("item.relics.strength." + strength);
    }

    public static Component getRelicTypeName(RelicEntry relicType) {
        ResourceLocation resourceLocation = RelicRegistry.getId(relicType);
        return Component.translatable("item." + resourceLocation.getNamespace() + ".relics.name." + resourceLocation.getPath());
    }
}
