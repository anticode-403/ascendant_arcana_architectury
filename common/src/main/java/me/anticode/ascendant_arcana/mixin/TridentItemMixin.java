package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @ModifyVariable(method = "releaseUsing", at = @At("HEAD"), argsOnly = true)
    private int modifyUseTime(int i, @Local(argsOnly = true) ItemStack itemStack) {
        float hasteMultiplier = 1 + (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        int useDuration = 72000 - i;
        return 72000 - Mth.ceil(useDuration * hasteMultiplier);
    }
}
