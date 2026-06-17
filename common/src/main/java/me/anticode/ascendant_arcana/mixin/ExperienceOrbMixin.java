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
    private static void discardAtAward(CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void discardAtTick(CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) {
            ((ExperienceOrb)(Object)this).discard();
            ci.cancel();
        }
    }
}
