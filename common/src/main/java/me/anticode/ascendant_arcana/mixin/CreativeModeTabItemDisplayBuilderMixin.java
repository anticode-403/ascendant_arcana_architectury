package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(CreativeModeTab.ItemDisplayBuilder.class)
public class CreativeModeTabItemDisplayBuilderMixin {
    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void disableEnchantments(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        if (stack.isEmpty()) {
            ci.cancel();
        }
        Map<Enchantment, Integer> newMap = new LinkedHashMap<>();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Enchantment enchantment : enchantments.keySet()) {
            if (AArcanaEnchantmentHelper.isEnchantmentEnabled(enchantment)) {
                newMap.put(enchantment, enchantments.get(enchantment));
            }
        }
        EnchantmentHelper.setEnchantments(newMap, stack);
    }

    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V"), cancellable = true)
    private void avoidIllegalState(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        ci.cancel();
    }
}
