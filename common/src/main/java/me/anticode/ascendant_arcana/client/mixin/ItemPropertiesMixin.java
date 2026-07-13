package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemProperties.class)
public class ItemPropertiesMixin {
    @ModifyReturnValue(method = "method_27890", at = @At("RETURN"))
    private static float modifyBowPullView(float original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity) {
        return ascendant_arcana$applyHasteRelic(original, itemStack, livingEntity);
    }

    @ModifyReturnValue(method = "method_27888", at = @At("RETURN"))
    private static float modifyCrossbowPullView(float original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity) {
        return ascendant_arcana$applyHasteRelic(original, itemStack, livingEntity);
    }

    @Unique
    private static float ascendant_arcana$applyHasteRelic(float original, ItemStack itemStack, LivingEntity livingEntity) {
        if (original == 0 || original == 1) return original;
        float hasteMultiplier = (float) RelicHelper.getStrengthFromNbt(Relics.DAMAGE, itemStack.getTag()) / 2;
        float playerMultiplier = 0F;
        MobEffectInstance effectInstance = livingEntity.getEffect(AArcanaMobEffects.ARCHERS_GAMBIT.get());
        if (effectInstance != null) playerMultiplier = (float)(effectInstance.getAmplifier() + 1) * 0.3F;
        float newValue = (original * (1 + playerMultiplier)) * (1 + hasteMultiplier);
        if (newValue > 1) return 1;
        return newValue;
    }
}
