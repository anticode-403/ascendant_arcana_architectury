package me.anticode.ascendant_arcana.logic;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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
        return base_capacity + stack.getOrCreateTag().getInt(BONUS_RELIC_CAPACITY);
    }

    public static Map<Relics, Integer> fromNbt(CompoundTag nbt) {
        return fromNbtList((ListTag) nbt.get(RELICS_KEY));
    }

    public static Map<Relics, Integer> fromNbtList(ListTag list) {
        Map<Relics, Integer> map = new HashMap<>();
        if (list == null) return map;
        for  (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            Relics key = Relics.fromId(tag.getInt("id"));
            int value = tag.getInt("strength");
            map.put(key, value);
        }
        return map;
    }

    public static ListTag toNbt(Map<Relics, Integer> map) {
        ListTag nbtList = new ListTag();
        for(Map.Entry<Relics, Integer> entry : map.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", Relics.toId(entry.getKey()));
            tag.putInt("strength", entry.getValue());
            nbtList.add(tag);
        }
        return nbtList;
    }

    public static boolean canApplyRelic (ItemStack stack, Relics relic, int strength) {
        Map<Relics, Integer> relics = fromNbt(stack.getOrCreateTag());
        if (relics.containsKey(relic) && strength > relics.get(relic)) return true;
        else return getRelicCapacity(stack) > relics.keySet().size();
    }

    public static ItemStack applyRelic(ItemStack stack, Relics relicType, int strength) {
        Map<Relics, Integer> relics = fromNbt(stack.getOrCreateTag());
        relics.put(relicType, strength);
        stack.getOrCreateTag().putInt(RELICS_KEY, relics.size());
        return stack;
    }

    public static int getValueFromNbt(CompoundTag nbt, Relics key) {
        if (nbt == null) return 0;
        Map<Relics, Integer> map = fromNbt(nbt);
        return map.get(key) != null ? map.get(key) : 0;
    }

    public static double getRelicStrength(Relics relicType, int strength) {
        if (strength == 0) return 0;
        return switch (relicType) {
            case DAMAGE -> AscendantArcana.config.damage_relic_strengths.get(strength);
            case DURABILITY -> AscendantArcana.config.durability_relic_strengths.get(strength);
            case PROTECTION -> AscendantArcana.config.protection_relic_strengths.get(strength);
            case HASTE -> AscendantArcana.config.haste_relic_strengths.get(strength);
            case ENCHANTMENT_CAPACITY -> AscendantArcana.config.enchantment_capacity_relic_strengths.get(strength);
        };
    }

    public static double getStrengthFromNbt(Relics relicType, CompoundTag nbt) {
        return getRelicStrength(relicType, getValueFromNbt(nbt, relicType));
    }

    public static int getTooltipStrength(Relics relicType, int strength) {
        if (strength == 0) return 0;
        return switch (relicType) {
            case DAMAGE -> Mth.floor(AscendantArcana.config.damage_relic_strengths.get(strength) * 100);
            case DURABILITY -> AscendantArcana.config.durability_relic_strengths.get(strength);
            case PROTECTION -> Mth.floor(AscendantArcana.config.protection_relic_strengths.get(strength) * 100);
            case HASTE -> Mth.floor(AscendantArcana.config.haste_relic_strengths.get(strength) * 100);
            case ENCHANTMENT_CAPACITY -> AscendantArcana.config.enchantment_capacity_relic_strengths.get(strength);
        };
    }

    public static Component getRelicTypeText(Relics relicType) {
        return Component.translatable("item.relics.type." + relicType.toString().toLowerCase());
    }

    public static Component getRelicStrengthName(int strength) {
        return Component.translatable("item.relics.strength." + strength);
    }

    public static Component getRelicTypeName(Relics relicType) {
        return Component.translatable("item.relics.name." + relicType.toString().toLowerCase());
    }
}
