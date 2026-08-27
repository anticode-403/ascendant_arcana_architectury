package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import me.anticode.ascendant_arcana.init.AArcanaTags;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.recipe.UniversalRepairRecipe;
import me.anticode.ascendant_arcana.relics.RelicEntry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean applyRestorineRepairs(Item instance, ItemStack stack, ItemStack ingredient, Operation<Boolean> original) {
        if (stack.is(AArcanaTags.Items.RESTORINE_BLACKLIST)) return original.call(instance, stack, ingredient);
        List<Ingredient> validIngredients = new ArrayList<>();
        for (UniversalRepairRecipe recipe : player.level().getRecipeManager().getAllRecipesFor(AArcanaRecipes.REPAIR_RECIPE_TYPE.get())) {
            validIngredients.add(recipe.getIngredient());
        }
        if (validIngredients.stream().anyMatch(ingr -> ingr.test(ingredient))) return true;
        return original.call(instance, stack, ingredient);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int restorineRepairsPartially(int a, int b, Operation<Integer> original) {
        int maxDamage = this.inputSlots.getItem(0).getMaxDamage();
        ItemStack repairIngredient = this.inputSlots.getItem(1);
        List<UniversalRepairRecipe> validIngredients = player.level().getRecipeManager().getAllRecipesFor(AArcanaRecipes.REPAIR_RECIPE_TYPE.get());
        Optional<UniversalRepairRecipe> matchedRecipe = validIngredients.stream().filter(ingr -> ingr.getIngredient().test(repairIngredient)).findFirst();
        if (matchedRecipe.isPresent()) {
            UniversalRepairRecipe recipe = matchedRecipe.get();
            if (recipe.getAddition()) return Math.min(a, Mth.floor(recipe.getRepairAmount()));
            else return Math.min(a, Mth.floor(maxDamage * recipe.getRepairAmount()));
        }
        if (!repairIngredient.is(AArcanaItems.RESTORINE.get())) return Math.min(a, b*2); // Base material repairs 50% instead of 25%
        return original.call(a, b/2); // Restorine repairs 12.5%
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0), cancellable = true)
    private void anvilRelics(CallbackInfo ci) {
        ItemStack relicStack = this.inputSlots.getItem(1);
        if (!relicStack.is(AArcanaItems.RELIC.get())) return;
        ItemStack itemStack = this.inputSlots.getItem(0);
        if (!RelicHelper.canApplyRelic(itemStack, relicStack)) return;
        ItemStack outputStack = itemStack.copy();
        int relicStrength = RelicItem.getRelicStrength(relicStack);
        RelicEntry relicType = RelicItem.getRelicType(relicStack);
        RelicHelper.infuseRelic(outputStack, relicType, relicStrength);
        this.resultSlots.setItem(0, outputStack);
        broadcastChanges();
        ci.cancel();
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void anvilDoesNotCostLevels(DataSlot instance, int i) {
        // This space intentionally left blank
    }

    @ModifyReturnValue(method = "mayPickup", at = @At("RETURN"))
    private boolean canAlwaysTakeOutput(boolean value) {
        return ascendant_arcana$testEnchantmentCost();
    }

    @ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;isCompatibleWith(Lnet/minecraft/world/item/enchantment/Enchantment;)Z"))
    private boolean enchantmentCapacity(boolean value) {
        if (!ascendant_arcana$testEnchantmentCost()) return false;
        return value;
    }

    @Unique
    private boolean ascendant_arcana$testEnchantmentCost() {
        ItemStack stack = inputSlots.getItem(0);
        ItemStack book = inputSlots.getItem(1);
        return AArcanaEnchantmentHelper.testAnvilItems(stack, book);
    }
}
