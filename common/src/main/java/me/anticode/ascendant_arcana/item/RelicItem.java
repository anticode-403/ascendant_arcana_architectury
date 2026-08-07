package me.anticode.ascendant_arcana.item;

import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.relics.RelicEntry;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        return Component.translatable(getDescriptionId(), RelicHelper.getRelicStrengthName(getRelicStrength(stack)), RelicHelper.getRelicTypeName(getRelicType(stack)));
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
        RelicEntry relicEntry = getRelicType(stack);
        int visualStrength = RelicHelper.getTooltipStrength(relicEntry, getRelicStrength(stack));
        Component relicName = RelicHelper.getRelicTypeText(relicEntry);
        String hasPercent = (relicEntry.getOperation() == RelicEntry.Operation.multiply_total) ? "%" : "";
        Component line = Component.translatable("item.relics.tooltip", visualStrength, relicName, hasPercent).withStyle(ChatFormatting.BLUE);
        String appliedToTooltip = "item.relics.tooltip.applied_any";
        if (relicEntry.getTarget() == RelicEntry.Target.armor) {
            appliedToTooltip = "item.relics.tooltip.applied_armor";
        }
        else if (relicEntry.getTarget() == RelicEntry.Target.tool) {
            appliedToTooltip = "item.relics.tooltip.applied_tool";
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(appliedToTooltip).withStyle(ChatFormatting.GRAY));
        tooltip.add(line);
    }

    public static RelicEntry getRelicType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.get(RELIC_TYPE_KEY) != null) {
            if (tag.get(RELIC_TYPE_KEY) instanceof IntTag || tag.getString(RELIC_TYPE_KEY).startsWith("minecraft:")) {
                int oldId;
                if (tag.get(RELIC_TYPE_KEY) instanceof IntTag) oldId = tag.getInt(RELIC_TYPE_KEY);
                else oldId = Integer.parseInt(tag.getString(RELIC_TYPE_KEY).replace("minecraft:", ""));
                tag.putString(RELIC_TYPE_KEY, RelicHelper.convertFromOldRelicIds(oldId).toString());
            }
        }
        return RelicRegistry.get(ResourceLocation.tryParse(tag.getString(RELIC_TYPE_KEY)));
    }

    public static int getRelicStrength(ItemStack stack) {
        return stack.getOrCreateTag().getInt(RELIC_STRENGTH_KEY);
    }

    public static void writeRelicData(ItemStack stack, RelicEntry relicEntry, int strength) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(RELIC_TYPE_KEY, relicEntry.getType().toString());
        tag.putInt(RELIC_STRENGTH_KEY, strength);
    }
}