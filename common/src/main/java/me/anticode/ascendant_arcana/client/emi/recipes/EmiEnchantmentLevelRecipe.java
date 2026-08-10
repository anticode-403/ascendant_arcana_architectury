package me.anticode.ascendant_arcana.client.emi.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.emi.AscendantArcanaEmi;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EmiEnchantmentLevelRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final boolean isFinal;
    private final int levelCost;
    private final int level;
    private final EmiIngredient scrapStack;
    private final EmiIngredient primaryStack;
    private final EmiIngredient secondaryStack;
    private final Enchantment output;
    private final EmiIngredient targets;

    public EmiEnchantmentLevelRecipe(EnchantmentRecipe recipe, EnchantmentRecipe.EnchantmentLevelRecipe levelRecipe, int level) {
        this.id = recipe.getId().withPrefix("/").withSuffix("/" + level);
        this.level = level;
        this.output = recipe.enchantment;
        this.levelCost = levelRecipe.levelCost();
        if (levelRecipe.scrapStack() != null) {
            this.scrapStack = EmiIngredient.of(levelRecipe.scrapStack().getIngredient(), levelRecipe.scrapStack().getCount());
        } else scrapStack = null;
        if (levelRecipe.primaryIngredientStack() != null) {
            this.primaryStack = EmiIngredient.of(levelRecipe.primaryIngredientStack().getIngredient(), (long)levelRecipe.primaryIngredientStack().getCount());
        } else primaryStack = null;
        if (levelRecipe.secondaryIngredientStack() != null) {
            this.secondaryStack = EmiIngredient.of(levelRecipe.secondaryIngredientStack().getIngredient(), (long)levelRecipe.secondaryIngredientStack().getCount());
        } else secondaryStack = null;

        this.targets = EmiIngredient.of(BuiltInRegistries.ITEM.stream().filter((item) -> output.canEnchant(new ItemStack(item))).map((item) -> EmiStack.of(new ItemStack(item))).toList());
        this.isFinal = recipe.getLevels().size() == level;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AscendantArcanaEmi.ENCHANTING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        if (scrapStack != null) inputs.add(scrapStack);
        if (primaryStack != null) inputs.add(primaryStack);
        if (secondaryStack != null) inputs.add(secondaryStack);
        inputs.add(targets);
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        for (int i = 0; i + level <= this.output.getMaxLevel(); i++) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(output, level + i));
            outputs.add(EmiStack.of(book));
            if (!isFinal) break;
        }
        outputs.addAll(targets.getEmiStacks());
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 106;
    }

    @Override
    public int getDisplayHeight() {
        return 27;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.addSlot(scrapStack, 0, 0);
        EmiIngredient primaryDisplayStack = EmiStack.EMPTY;
        if (primaryStack != null) {
            primaryDisplayStack = primaryStack;
        }
        widgetHolder.addSlot(primaryDisplayStack, 18, 0);
        EmiIngredient secondaryDisplayStack = EmiStack.EMPTY;
        if (secondaryStack != null) {
            secondaryDisplayStack = secondaryStack;
        }
        widgetHolder.addSlot(secondaryDisplayStack, 36, 0);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(output, level);
        widgetHolder.addTexture(EmiTexture.EMPTY_ARROW, 59, 1);
        widgetHolder.addSlot(EmiStack.of(enchantedBook, 1), 88, 0).recipeContext(this);

        // Enchanting Power
        widgetHolder.addTexture(AscendantArcanaEmi.EMI_SPRITES, 0, 19, 7, 7, 7, 0);
        int requiredPower = switch (output.getRarity()) {
            case COMMON -> AscendantArcana.config.minimum_enchanting_power;
            case UNCOMMON -> AscendantArcana.config.uncommon_enchanting_power;
            case RARE -> AscendantArcana.config.rare_enchanting_power;
            case VERY_RARE -> AscendantArcana.config.very_rare_enchanting_power;
        };
        Component requiredPowerText = Component.literal(String.valueOf(requiredPower));
        widgetHolder.addText(requiredPowerText, 8, 19, 5592405, false);
        List<Component> enchantingPowerTooltip = List.of(
                Component.translatable("gui.emi.ascendant_arcana.enchanting_power")
        );
        widgetHolder.addTooltipText(enchantingPowerTooltip, 0, 19, 20, 8);

        // Enchantment Capacity
        widgetHolder.addTexture(AscendantArcanaEmi.EMI_SPRITES, 27, 19, 9, 7, 14, 0);
        widgetHolder.addText(Component.literal(String.valueOf(AArcanaEnchantmentHelper.getEnchantmentCost(output))), 37, 19, 5592405, false);
        widgetHolder.addTooltipText(List.of(Component.translatable("gui.emi.ascendant_arcana.capacity_cost")), 27, 19, 20, 8);

        // XP
        if (!AscendantArcana.config.disable_xp) {
            widgetHolder.addTexture(AscendantArcanaEmi.EMI_SPRITES, 48, 19, 7, 7, 0, 0);
            List<Component> levelCostTooltip = List.of(
                    Component.translatable("gui.emi.ascendant_arcana.level_cost")
            );
            widgetHolder.addText(Component.literal(String.valueOf(levelCost)), 56, 19, 5592405, false);
            widgetHolder.addTooltipText(levelCostTooltip, 48, 19, 20, 8);
        }
    }
}
