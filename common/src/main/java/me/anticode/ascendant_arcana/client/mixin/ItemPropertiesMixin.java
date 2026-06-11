package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemProperties.class)
public class ItemPropertiesMixin {
    @ModifyReturnValue(method = "method_27890", at = @At("RETURN"))
    private static float modifyBowPullView(float original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (original == 0 || original == 1) return original;
        float hasteMultiplier = (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        return original * hasteMultiplier;
    }

    @ModifyReturnValue(method = "method_27888", at = @At("RETURN"))
    private static float modifyCrossbowPullView(float original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (original == 0 || original == 1) return original;
        float hasteMultiplier = (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        return original * hasteMultiplier;
    }
}
