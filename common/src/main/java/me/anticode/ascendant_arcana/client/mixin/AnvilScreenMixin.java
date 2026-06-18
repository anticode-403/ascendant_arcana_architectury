package me.anticode.ascendant_arcana.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void removeAnvilXP(GuiGraphics guiGraphics, int i, int j, CallbackInfo ci) {
        ci.cancel();
    }
}
