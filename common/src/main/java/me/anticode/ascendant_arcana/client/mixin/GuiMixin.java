package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.CrossbowAccess;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean doesNotHaveXpBar(MultiPlayerGameMode instance, Operation<Boolean> original) {
        if (AscendantArcana.config.disable_xp && AscendantArcana.config.hide_xp_bar) return false;
        return original.call(instance);
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void doesNotHaveXpBar(GuiGraphics context, int x, CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp && AscendantArcana.config.hide_xp_bar) ci.cancel();
    }

    @ModifyVariable(method = "renderExperienceBar", at = @At("STORE"), ordinal = 2)
    public int removeExperienceProgress(int amount) {
        return AscendantArcana.config.disable_xp ? 0 : amount;
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 0), cancellable = true)
    public void removeExperienceLevel(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) ci.cancel();
    }

    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V"))
    void moveHealthBarDown(Gui instance, GuiGraphics context, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart, Operation<Void> original) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) {
            original.call(instance, context, type, x, y, v, blinking, halfHeart);
            return;
        }

        original.call(instance, context, type, x, y + 6, v, blinking, halfHeart);
    }

    @ModifyArg(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    int moveHungerBarDown(int i) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) return i;
        return i + 6;
    }

    @ModifyArg(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    int moveAirBarDown(int i) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) return i;
        return i + 6;
    }

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", ordinal = 0, shift = At.Shift.AFTER))
    void renderCrossbowAmmoIndicator(GuiGraphics guiGraphics, CallbackInfo ci) {
        ItemStack crossbowItem = ItemStack.EMPTY;
        assert minecraft.player != null;
        if (minecraft.player.getMainHandItem().getItem() instanceof CrossbowItem) {
            crossbowItem = minecraft.player.getMainHandItem();
        }
        else if (minecraft.player.getOffhandItem().getItem() instanceof CrossbowItem) {
            crossbowItem = minecraft.player.getOffhandItem();
        }
        if (crossbowItem.isEmpty()) return;
        int maxProjectiles = ItemHelper.getCrossbowMaxArrows(crossbowItem);
        int loadedProjectiles = ((CrossbowAccess) crossbowItem.getItem()).ascendant_arcana$getChargedProjectilesCount(crossbowItem);
        int x = (screenWidth / 2) - (3 * maxProjectiles);
        int y = (screenHeight / 2) + 16;
        for (int i = 0; i < maxProjectiles; i++) {
            boolean projectileLoaded = i < loadedProjectiles;
            guiGraphics.blit(new ResourceLocation(AscendantArcana.MOD_ID, "textures/gui/hud/crossbow_indicator.png"), x + (i * 6), y, 0, projectileLoaded ? 4 : 0, 0, 4, 4, 16, 16);
        }
    }
}
