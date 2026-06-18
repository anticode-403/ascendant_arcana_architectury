package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import me.anticode.ascendant_arcana.logic.RemovedRegistryEntry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Unique
    private static ItemStack cachedStack = null;

    @ModifyVariable(method = "setEnchantments", at = @At(value = "HEAD"), argsOnly = true)
    private static Map<Enchantment, Integer> disableEnchantments(Map<Enchantment, Integer> value, Map<Enchantment, Integer> map, ItemStack stack) {
        Map<Enchantment, Integer> newMap = new LinkedHashMap<>();
        for (Enchantment enchantment : value.keySet()) {
            if (AArcanaEnchantmentHelper.isEnchantmentEnabled(enchantment)) {
                newMap.put(enchantment, value.get(enchantment));
            } else {
                Enchantment replacement = AArcanaEnchantmentHelper.getReplacement(enchantment, stack);
                if (replacement != null) {
                    newMap.put(replacement, Math.min(replacement.getMaxLevel(), value.get(enchantment)));
                }
            }
        }
        return newMap;
    }
    @ModifyReturnValue(method = "getBlockEfficiency", at = @At("RETURN"))
    private static int modifyBlockEfficiency(int original, @Local(argsOnly = true)LivingEntity livingEntity) {
        float multiplier = 1 + (RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(livingEntity.getMainHandItem().getOrCreateTag(), Relics.HASTE)) * 0.1F);
        return Mth.floor(original * multiplier);
    }

    @Inject(method = "selectEnchantment", at = @At(value = "RETURN", ordinal = 1))
    private static void enchantmentCapacity(RandomSource random, ItemStack stack, int level, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        for (int i = cir.getReturnValue().size() - 1; i >= 0; i--) {
            EnchantmentInstance entry = cir.getReturnValue().get(i);
            if (AArcanaEnchantmentHelper.getEnchantmentUsage(stack) + (AArcanaEnchantmentHelper.getEnchantmentCost(entry.enchantment) * entry.level) > AArcanaEnchantmentHelper.getEnchantmentCapacity(stack)) {
                cir.getReturnValue().remove(i);
            }
        }
    }

    @Inject(method = "setEnchantments", at = @At("HEAD"))
    private static void enchantmentCapacity(Map<Enchantment, Integer> enchantments, ItemStack stack, CallbackInfo ci) {
        cachedStack = stack;
    }

    @ModifyVariable(method = "setEnchantments", at = @At("HEAD"), argsOnly = true)
    private static Map<Enchantment, Integer> enchantmentCapacity(Map<Enchantment, Integer> value) {
        Map<Enchantment, Integer> newMap = new LinkedHashMap<>();
        int runningTotal = 0;
        for (Enchantment enchantment : value.keySet()) {
            int enchantCost = AArcanaEnchantmentHelper.getEnchantmentCost(enchantment) * value.get(enchantment);
            if (runningTotal + enchantCost < AArcanaEnchantmentHelper.getEnchantmentCapacity(cachedStack)) {
                newMap.put(enchantment, value.get(enchantment));
                runningTotal += enchantCost;
            }
        }
        cachedStack = null;
        return newMap;
    }
}
