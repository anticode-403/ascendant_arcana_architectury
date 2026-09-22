package me.anticode.ascendant_arcana.client.emi.recipes;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.anticode.ascendant_arcana.init.AArcanaTags;
import me.anticode.ascendant_arcana.recipe.UniversalRepairRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmiUniversalRepairRecipe implements EmiRecipe {
    private final List<EmiStack> tools;
    private final EmiIngredient resource;
    private final ResourceLocation id;
    private final int uniq = new Random().nextInt();
    private final float repairAmount;
    private final boolean addition;

    public EmiUniversalRepairRecipe(UniversalRepairRecipe repairRecipe, EmiRegistry emiRegistry) {
        this.resource = EmiIngredient.of(repairRecipe.getIngredient());
        this.tools = BuiltInRegistries.ITEM.stream().filter(item -> item.getMaxDamage() > 0 && !item.getDefaultInstance().is(AArcanaTags.Items.RESTORINE_BLACKLIST) && !emiRegistry.isStackDisabled(EmiStack.of(item))).map(EmiStack::of).toList();
        this.id = repairRecipe.getId();
        this.repairAmount = repairRecipe.getRepairAmount();
        this.addition = repairRecipe.getAddition();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(resource);
        inputs.addAll(tools);
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return tools;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return 125;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.PLUS, 27, 3);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
        widgets.addGeneratedSlot(r -> getTool(r, false), uniq, 0, 0);
        widgets.addSlot(resource, 49, 0);
        widgets.addGeneratedSlot(r -> getTool(r, true), uniq, 107, 0).recipeContext(this);
    }

    private EmiStack getTool(Random r, boolean repaired) {
        ItemStack stack = tools.get(r.nextInt(0, tools.size() - 1)).getItemStack().copy();
        if (stack.getMaxDamage() <= 0) {
            return EmiStack.of(stack);
        }
        int d = r.nextInt(1, stack.getMaxDamage());
        if (repaired) {
            if (addition) {
                d -= Mth.floor(repairAmount);
            } else {
                d -= Mth.floor(stack.getMaxDamage() * repairAmount);
            }
            if (d <= 0) {
                return EmiStack.of(stack);
            }
        }
        stack.setDamageValue(d);
        return EmiStack.of(stack);
    }
}
