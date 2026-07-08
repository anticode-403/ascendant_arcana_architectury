package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean applyRestorineRepairs(Item instance, ItemStack stack, ItemStack ingredient, Operation<Boolean> original) {
        if (ingredient.is(AArcanaItems.RESTORINE.get())) return true;
        return original.call(instance, stack, ingredient);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int restorineRepairsPartially(int a, int b, Operation<Integer> original) {
        ItemStack itemStack = this.inputSlots.getItem(1);
        if (!itemStack.is(AArcanaItems.RESTORINE.get())) return Math.min(a, b*2); // Base material repairs 50% instead of 25%
        return original.call(a, b/2); // Restorine repairs 12.5%
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void anvilDoesNotCostLevels(DataSlot instance, int i) {
        // This space intentionally left blank
    }

    @ModifyReturnValue(method = "mayPickup", at = @At("RETURN"))
    private boolean canAlwaysTakeOutput(boolean value) {
        return true;
    }

    @ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;isCompatibleWith(Lnet/minecraft/world/item/enchantment/Enchantment;)Z"))
    private boolean enchantmentCapacity(boolean value) {
        ItemStack stack = inputSlots.getItem(0);
        ItemStack book = inputSlots.getItem(1);
        Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.getEnchantments(book);
        int cost = 0;
        for (Enchantment enchantment : bookEnchants.keySet()) {
            if (enchantment.canEnchant(stack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(stack).keySet(), enchantment)) {
                cost += bookEnchants.get(enchantment) * AArcanaEnchantmentHelper.getEnchantmentCost(enchantment);
            }
        }
        if (AArcanaEnchantmentHelper.getEnchantmentUsage(stack) + cost > AArcanaEnchantmentHelper.getEnchantmentCapacity(stack)) {
            return false;
        }
        return value;
    }
}
