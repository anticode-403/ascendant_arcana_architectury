package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import me.anticode.ascendant_arcana.api.CrossbowAccess;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private final ResourceLocation CROSSHAIR_INDICATORS = new ResourceLocation(AscendantArcana.MOD_ID, "textures/gui/hud/crosshair_indicators.png");

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
    private int removeExperienceProgress(int amount) {
        return AscendantArcana.config.disable_xp ? 0 : amount;
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 0), cancellable = true)
    private void removeExperienceLevel(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (AscendantArcana.config.disable_xp) ci.cancel();
    }

    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V"))
    private void moveHealthBarDown(Gui instance, GuiGraphics context, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart, Operation<Void> original) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (minecraft.player != null && minecraft.player.jumpableVehicle() != null)) {
            original.call(instance, context, type, x, y, v, blinking, halfHeart);
            return;
        }

        original.call(instance, context, type, x, y + 6, v, blinking, halfHeart);
    }

    @ModifyArg(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    private int moveHungerBarDown(int i) {
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
        if (!crossbowItem.isEmpty()) {
            int maxProjectiles = ItemHelper.getCrossbowMaxArrows(crossbowItem);
            int loadedProjectiles = ((CrossbowAccess) crossbowItem.getItem()).ascendant_arcana$getChargedProjectilesCount(crossbowItem);
            int x = (screenWidth / 2) - (3 * maxProjectiles);
            int y = (screenHeight / 2) + 16;
            for (int i = 0; i < maxProjectiles; i++) {
                boolean projectileLoaded = i < loadedProjectiles;
                guiGraphics.blit(CROSSHAIR_INDICATORS, x + (i * 6), y, 0, projectileLoaded ? 4 : 0, 0, 4, 4, 16, 16);
            }
        }
        boolean isChargingMovement;
        int progress;
        AArcanaPlayer aPlayer = (AArcanaPlayer) minecraft.player;
        if (minecraft.player.isFallFlying() && aPlayer.ascendant_arcana$isWhirlwindCharging()) {
            isChargingMovement = true;
            progress = aPlayer.ascendant_arcana$getWhirlwindCharge();
        } else {
            isChargingMovement = false;
            progress = 0;
        }
        if (isChargingMovement) {
            int x = (screenWidth / 2) - 20;
            int y = (screenHeight / 2) - 9;
            for (int i = 0; i < 3; i++) {
                guiGraphics.blit(CROSSHAIR_INDICATORS, x, y + (i * 7), 0, progress > (2 - i) ? 6 : 0, 4, 6, 5, 16, 16);
            }
        }
        float whirlwindCooldown = aPlayer.ascendant_arcana$getWhirlwindCooldown();
        if (whirlwindCooldown > 0) {
            int x = (screenWidth / 2) + 13;
            int y = (screenHeight / 2) - 2;
            int pixelCooldown = Mth.floor(whirlwindCooldown * 5);
            guiGraphics.blit(CROSSHAIR_INDICATORS, x, y, 0, 0, 9, 5, pixelCooldown, 16, 16);
            guiGraphics.blit(CROSSHAIR_INDICATORS, x, y + pixelCooldown, 0, 5, 9 + pixelCooldown, 5, 5 - pixelCooldown, 16, 16);
        }
    }
}
