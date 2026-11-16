package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantedBookItem.class)
public class EnchantedBookItemMixin {
    @Unique
    private static Enchantment ascendant_arcana$replacement = null;

    @Inject(method = "getEnchantments", at = @At("RETURN"), cancellable = true)
    private static void destroyExtraEnchantmentsIfConfig(ItemStack stack, CallbackInfoReturnable<ListTag> cir) {
        if (AscendantArcana.config.single_enchantment_books && AscendantArcana.config.update_old_books && cir.getReturnValue().size() > 1) {
            ListTag enchantments = new ListTag();
            enchantments.add(cir.getReturnValue().get(0));
            assert stack.getTag() != null; // If there is more than 1 enchantment, the NBT data should be full.
            stack.getTag().put("StoredEnchantments", enchantments);
            cir.setReturnValue(enchantments);
        }
    }

    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    private static void disableEnchantments(ItemStack stack, EnchantmentInstance entry, CallbackInfo ci) {
        if (!AArcanaEnchantmentHelper.isEnchantmentEnabled(entry.enchantment)) {
            ascendant_arcana$replacement = AArcanaEnchantmentHelper.getReplacement(entry.enchantment, stack);
            if (ascendant_arcana$replacement == null) {
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "addEnchantment", at = @At("HEAD"), argsOnly = true)
    private static EnchantmentInstance disableEnchantments(EnchantmentInstance value, ItemStack stack) {
        if (ascendant_arcana$replacement != null) {
            Enchantment temp = ascendant_arcana$replacement;
            ascendant_arcana$replacement = null;
            return new EnchantmentInstance(temp, Math.min(temp.getMaxLevel(), value.level));
        }
        return value;
    }

    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    private static void enchantmentCapacity(ItemStack stack, EnchantmentInstance entry, CallbackInfo ci) {
        if (AscendantArcana.config.single_enchantment_books) {
            if (!EnchantedBookItem.getEnchantments(stack).isEmpty() && !AArcanaEnchantmentHelper.getEnchantments(stack).containsKey(entry.enchantment)) ci.cancel();
        } else if (!AArcanaEnchantmentHelper.testEnchantmentCost(stack, AArcanaEnchantmentHelper.getEnchantmentCost(entry.enchantment, entry.level))) {
            ci.cancel();
        }
    }
}
