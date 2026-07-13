package me.anticode.ascendant_arcana.client.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.emi.recipes.*;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import me.anticode.ascendant_arcana.init.AArcanaTags;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.Relics;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import me.anticode.ascendant_arcana.recipe.InfusionRecipe;
import me.anticode.ascendant_arcana.recipe.RelicCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class AscendantArcanaEmi implements EmiPlugin {
    public static final ResourceLocation EMI_SPRITES = new ResourceLocation(AscendantArcana.MOD_ID, "textures/gui/emi_elements.png");
    public static final EmiStack ENCHANTING_TABLE = EmiStack.of(Blocks.ENCHANTING_TABLE);
    public static final EmiStack COPPER_ENCHANTING_TABLE = EmiStack.of(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get());
    public static final EmiRecipeCategory ENCHANTING = new EmiRecipeCategory(new ResourceLocation(AscendantArcana.MOD_ID, "enchanting"), COPPER_ENCHANTING_TABLE, new EmiTexture(EMI_SPRITES, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.setDefaultComparison(AArcanaItems.RELIC.get(), Comparison.compareNbt());

        emiRegistry.addCategory(ENCHANTING);
        emiRegistry.addWorkstation(ENCHANTING, COPPER_ENCHANTING_TABLE);
        emiRegistry.addWorkstation(ENCHANTING, ENCHANTING_TABLE);

        RecipeManager manager = emiRegistry.getRecipeManager();
        for (EnchantmentRecipe recipe : manager.getAllRecipesFor(AArcanaRecipes.ENCHANTMENT_RECIPE_TYPE.get())) {
            emiRegistry.addRecipe(new EmiEnchantmentRecipe(recipe));
        }
        for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (recipe instanceof RelicCraftingRecipe relicRecipe) {
                List<EmiIngredient> ingredients = relicRecipe.getIngredients().stream().map((ingredient) -> {
                    if (ingredient.test(new ItemStack(AArcanaItems.RELIC.get()))) {
                        ItemStack stack = relicRecipe.getOutput().copy();
                        RelicItem.writeRelicData(stack, RelicItem.getRelicType(stack), RelicItem.getRelicStrength(stack) - 1);
                        return EmiStack.of(stack);
                    }
                    else return EmiIngredient.of(ingredient);
                }).toList();
                emiRegistry.addRecipe(new EmiCraftingRecipe(ingredients, EmiStack.of(relicRecipe.getOutput()), relicRecipe.getId(), true));
            }
        }
        for (SmithingRecipe recipe : manager.getAllRecipesFor(RecipeType.SMITHING)) {
            if (recipe instanceof InfusionRecipe infusionRecipe) {
                for (int i = 0; i < Relics.values().length; i++) {
                    Relics relicType = Relics.fromId(i);
                    ItemStack stack = new ItemStack(AArcanaItems.RELIC.get());
                    RelicItem.writeRelicData(stack, relicType, 1);
                    emiRegistry.addRecipe(new EmiInfusionRecipe(infusionRecipe, stack));
                }
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            List<Enchantment> targetedEnchantments = new ArrayList<>();
            List<Enchantment> universalEnchantments = new ArrayList<>();
            for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT.stream().toList()) {
                try {
                    if (enchantment.canEnchant(ItemStack.EMPTY)) {
                        universalEnchantments.add(enchantment);
                        continue;
                    }
                } catch (Throwable ignored) {
                }
                targetedEnchantments.add(enchantment);
            }
            if (emiRegistry.isStackDisabled(EmiStack.of(item))) continue;
            if (item.getMaxDamage() > 0 && !item.getDefaultInstance().is(AArcanaTags.Items.RESTORINE_BLACKLIST)) {
                emiRegistry.addRecipe(new EmiRestorineRepairRecipe(EmiStack.of(item), new ResourceLocation(AscendantArcana.MOD_ID, "/repair/").withSuffix(BuiltInRegistries.ITEM.getKey(item).getPath())));
            }

            ItemStack defaultStack = item.getDefaultInstance();
            int acceptableEnchantments = 0;
            for (Enchantment e : targetedEnchantments) {
                if (e.canEnchant(defaultStack) && defaultStack.isEnchantable()
                        && defaultStack.getItem().isEnchantable(defaultStack)) {
                    acceptableEnchantments++;
                }
            }
            if (acceptableEnchantments > 0) {
                for (Enchantment e : universalEnchantments) {
                    if (e.canEnchant(defaultStack)) {
                        acceptableEnchantments++;
                    }
                }
                emiRegistry.addRecipe(new EmiGrindstoneScrapRecipe(item, new ResourceLocation(AscendantArcana.MOD_ID, "/grindstone/scrap/").withSuffix(BuiltInRegistries.ITEM.getKey(item).getPath())));
            }
        }

        emiRegistry.removeRecipes((t) -> {
            ResourceLocation id = t.getId();
            if (id == null) return false;
            return id.getPath().contains("grindstone/disenchanting");
        });

        for (Enchantment e : BuiltInRegistries.ENCHANTMENT.stream().toList()) {
            if (!e.isCurse()) {
                int max = Math.min(10, e.getMaxLevel());
                int min = e.getMinLevel();
                while (min <= max) {
                    int level = min;
                    min++;
                }
            }
        }
    }
}
