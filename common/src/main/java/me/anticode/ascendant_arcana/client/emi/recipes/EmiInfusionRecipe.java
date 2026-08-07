package me.anticode.ascendant_arcana.client.emi.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaTags;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.recipe.InfusionRecipe;
import me.anticode.ascendant_arcana.relics.RelicRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmiInfusionRecipe implements EmiRecipe {
    protected final InfusionRecipe recipe;
    protected final ResourceLocation id;
    protected final EmiIngredient template;
    protected final EmiIngredient input;
    protected final List<EmiStack> addition;
    private final int uniq = new Random().nextInt();

    public EmiInfusionRecipe(InfusionRecipe recipe, ItemStack relic) {
        // this is incredibly cursed
        this.recipe = recipe;
        this.id = recipe.getId().withPrefix("/").withSuffix("/" + RelicRegistry.getId(RelicItem.getRelicType(relic)).toString().toLowerCase());
        this.template = EmiStack.of(BuiltInRegistries.ITEM.get(recipe.templateId));
        this.input = EmiIngredient.of(Ingredient.of(BuiltInRegistries.ITEM.stream().filter((item) -> recipe.isBaseIngredient(new ItemStack(item)) && recipe.matches(new ItemStack(item), relic) && !item.getDefaultInstance().is(AArcanaTags.Items.INFUSION_BLACKLIST)).map(ItemStack::new)));
        List<EmiStack> relics = new ArrayList<>();
        relics.add(EmiStack.of(relic));
        for (int i = 0; i < recipe.maxTier - 1; i++) {
            ItemStack newRelic = relic.copy();
            RelicItem.writeRelicData(newRelic, RelicItem.getRelicType(newRelic), i + 2);
            relics.add(EmiStack.of(newRelic));
        }
        this.addition = relics;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.SMITHING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(template, input, EmiIngredient.of(addition));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return input.getEmiStacks();
    }

    @Override
    public int getDisplayWidth() {
        return 112;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
        widgets.addSlot(template, 0, 0);
        widgets.addGeneratedSlot(r -> getStack(r, 0), uniq, 18, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(input.getEmiStacks()));
        widgets.addGeneratedSlot(r -> getStack(r, 1), uniq, 36, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(addition));
        widgets.addGeneratedSlot(r -> getStack(r, 2), uniq, 94, 0).recipeContext(this);
    }

    private EmiStack getStack(Random r, int i) {
        EmiStack input = this.input.getEmiStacks().get(r.nextInt(this.input.getEmiStacks().size()));
        EmiStack addition = this.addition.get(r.nextInt(this.addition.size()));
        return new EmiStack[] {
                input,
                addition,
                EmiStack.of(recipe.getOutput(input.getItemStack(), addition.getItemStack()))
        }[i];
    }
}
