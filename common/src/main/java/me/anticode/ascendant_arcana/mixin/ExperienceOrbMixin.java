package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {
    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private static void discardAtTick(CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) {
            ci.cancel();
        }
    }
}
