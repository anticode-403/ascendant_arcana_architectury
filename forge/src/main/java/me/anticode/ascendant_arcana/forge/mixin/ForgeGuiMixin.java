package me.anticode.ascendant_arcana.forge.mixin;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin {
    @Shadow
    public abstract Minecraft getMinecraft();

    @ModifyArg(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    private int moveHungerBarDown(int i) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (getMinecraft().player != null && getMinecraft().player.jumpableVehicle() != null)) return i;
        return i + 6;
    }

    @ModifyArg(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    private int moveAirBarDown(int i) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (getMinecraft().player != null && getMinecraft().player.jumpableVehicle() != null)) return i;
        return i + 6;
    }

    @ModifyArg(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 2)
    private int moveArmorBarDown(int i) {
        if (!AscendantArcana.config.disable_xp || !AscendantArcana.config.hide_xp_bar || (getMinecraft().player != null && getMinecraft().player.jumpableVehicle() != null)) return i;
        return i + 6;
    }
}
