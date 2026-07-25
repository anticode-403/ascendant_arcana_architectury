package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @ModifyReturnValue(method = "getFieldOfViewModifier", at = @At(value = "RETURN"))
    private float modifyFOVForShieldBash(float original) {
        if (((AArcanaPlayer)(Object)this).ascendant_arcana$getShieldBashStatus()) return original * 1.2F;
        return original;
    }
}
