package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean applyRestorineRepairs(Item instance, ItemStack stack, ItemStack ingredient) {
        if (ingredient.is(AArcanaItems.RESTORINE.get())) return true;
        return instance.isValidRepairItem(stack, ingredient);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int restorineRepairsPartially(int a, int b) {
        ItemStack itemStack = this.inputSlots.getItem(1);
        if (!itemStack.is(AArcanaItems.RESTORINE.get())) return Math.min(a, b*2); // Base material repairs 50% instead of 25%
        return Math.min(a, b/2); // Restorine repairs 12.5%
    }

    @ModifyArg(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private int anvilDoesNotCostLevels(int value) {
        return 0;
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
