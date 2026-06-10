package me.anticode.ascendant_arcana.item;

import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RelicItem extends Item {
    public static final String RELIC_STRENGTH_KEY = "RelicStrength";
    public static final String RELIC_TYPE_KEY = "RelicType";

    public RelicItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        if (getRelicStrength(stack) == 0) return Component.translatable("item.relics.unknown");
        return Component.translatable(toString(), RelicHelper.getRelicStrengthName(getRelicStrength(stack)), RelicHelper.getRelicTypeName(getRelicType(stack)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        if (getRelicStrength(stack) == 0) return;
        Relics relicType = getRelicType(stack);
        int visualStrength = RelicHelper.getTooltipStrength(relicType, getRelicStrength(stack));
        Component relicName = RelicHelper.getRelicTypeText(relicType);
        String hasPercent = (relicType == Relics.HASTE || relicType == Relics.PROTECTION || relicType == Relics.DAMAGE) ? "%" : "";
        Component line = Component.translatable("item.relics.tooltip", visualStrength, relicName, hasPercent).withStyle(ChatFormatting.BLUE);
        String appliedToTooltip = "item.relics.tooltip.applied_any";
        if (relicType == Relics.PROTECTION) {
            appliedToTooltip = "item.relics.tooltip.applied_armor";
        }
        else if (relicType == Relics.HASTE || relicType == Relics.DAMAGE) {
            appliedToTooltip = "item.relics.tooltip.applied_tool";
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(appliedToTooltip).withStyle(ChatFormatting.GRAY));
        tooltip.add(line);
    }

    public static Relics getRelicType(ItemStack stack) {
        return Relics.fromId(stack.getOrCreateTag().getInt(RELIC_TYPE_KEY));
    }

    public static int getRelicStrength(ItemStack stack) {
        return stack.getOrCreateTag().getInt(RELIC_STRENGTH_KEY);
    }

    public static void writeRelicData(ItemStack stack, Relics relicType, int strength) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(RELIC_TYPE_KEY, Relics.toId(relicType));
        tag.putInt(RELIC_STRENGTH_KEY, strength);
    }
}