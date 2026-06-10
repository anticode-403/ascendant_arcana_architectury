package me.anticode.ascendant_arcana.client.mixin;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    protected abstract void renderHeart(GuiGraphics drawContext, Gui.HeartType heartType, int x, int y, int v, boolean blinking, boolean halfHeart);

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean doesNotHaveXpBar(MultiPlayerGameMode manager) {
        if (AscendantArcana.config.disable_xp) return false;
        return manager.hasExperience();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void doesNotHaveXpBar(GuiGraphics context, int x, CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) ci.cancel();
    }

    @Redirect(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V"))
    void moveHealthBarDown(Gui instance, GuiGraphics context, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart) {
        if (!AscendantArcana.config.disable_xp || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) {
            renderHeart(context, type, x, y, v, blinking, halfHeart);
            return;
        }

        renderHeart(context, type, x, y + 7, v, blinking, halfHeart);
    }

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    void moveHungerBarDown(GuiGraphics instance, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        if (!AscendantArcana.config.disable_xp || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) {
            instance.blit(texture, x, y, u, v, width, height);
            return;
        }

        instance.blit(texture, x, y + 7, u, v, width, height);
    }

    @Redirect(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    void moveAirBarDown(GuiGraphics instance, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        if (!AscendantArcana.config.disable_xp || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) {
            instance.blit(texture, x, y, u, v, width, height);
            return;
        }

        instance.blit(texture, x, y+7, u, v, width, height);
    }
}
