package me.anticode.ascendant_arcana.client.emi.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EmiGrindstoneScrapRecipe implements EmiRecipe {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/grindstone.png");
    private final int uniq = new Random().nextInt();
    private final Item tool;
    private final ResourceLocation id;

    public EmiGrindstoneScrapRecipe(Item tool, ResourceLocation id) {
        this.tool = tool;
        this.id = id;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.GRINDING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(tool));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(AArcanaItems.ENCHANTED_SCRAP.get()), EmiStack.of(Items.LAPIS_LAZULI));
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return 116;
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 116, 56, 30, 15);

        widgets.addGeneratedSlot(r -> getToolOrScrap(r, false), uniq, 18, 3).drawBack(false);
        widgets.addGeneratedSlot(r -> getToolOrScrap(r, true), uniq, 98, 18).drawBack(false).recipeContext(this);
    }

    private EmiStack getToolOrScrap(Random random, Boolean scrap) {
        ItemStack itemStack = new ItemStack(tool);
        int enchantments = 1 + Math.max(random.nextInt(5), random.nextInt(3));

        List<Enchantment> list = new ArrayList<>();

        outer:
        for (int i = 0; i < enchantments; i++) {
            Enchantment enchantment = getEnchantment(random);

            int maxLvl = enchantment.getMaxLevel();
            int minLvl = enchantment.getMinLevel();
            // Some enchantments are returning zero for max level? I don't want to think about it
            int lvl = maxLvl > 0 ? random.nextInt(maxLvl) + 1 : 0;

            if (lvl < minLvl) {
                lvl = minLvl;
            }

            for (Enchantment e : list) {
                if (e == enchantment || !e.isCompatibleWith(enchantment)) {
                    continue outer;
                }
            }
            list.add(enchantment);

            itemStack.enchant(enchantment, lvl);
        }
        Map<Enchantment, Integer> appliedEnchants = EnchantmentHelper.getEnchantments(itemStack);
        if (scrap) {
            return EmiStack.of(AArcanaEnchantmentHelper.convertEnchantmentsToScrap(appliedEnchants));
        }
        return EmiStack.of(itemStack);
    }

    private Enchantment getEnchantment(Random random){
        List<Enchantment> enchantments = BuiltInRegistries.ENCHANTMENT.stream().filter(i -> i.canEnchant(tool.getDefaultInstance())).toList();
        int enchantment = random.nextInt(enchantments.size());
        return enchantments.get(enchantment);
    }
}
