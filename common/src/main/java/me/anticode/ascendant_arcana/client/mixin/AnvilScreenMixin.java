package me.anticode.ascendant_arcana.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
    public AnvilScreenMixin(AnvilMenu itemCombinerMenu, Inventory inventory, Component component, ResourceLocation resourceLocation) {
        super(itemCombinerMenu, inventory, component, resourceLocation);
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/ItemCombinerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER), cancellable = true)
    private void removeAnvilXP(GuiGraphics guiGraphics, int i, int j, CallbackInfo ci) {
        if (!ascendant_arcana$testAnvilItems()) {
            Component component = Component.translatable("gui.anvil.max_capacity");
            int m = this.imageWidth - 8 - this.font.width(component) - 2;
            int l = 16736352;
            guiGraphics.fill(m - 2, 67, this.imageWidth - 8, 79, 1325400064);
            guiGraphics.drawString(this.font, component, m, 69, l);
        }
        ci.cancel();
    }

    @ModifyExpressionValue(method = "renderErrorIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z", ordinal = 2))
    private boolean drawErrorIconWhenOutOfCap(boolean original) {
        if (original) return ascendant_arcana$testAnvilItems();
        return false;
    }

    @Unique
    private boolean ascendant_arcana$testAnvilItems() {
        ItemStack stack = getMenu().getSlot(0).getItem();
        ItemStack book = getMenu().getSlot(1).getItem();
        return AArcanaEnchantmentHelper.testAnvilItems(stack, book);
    }
}
