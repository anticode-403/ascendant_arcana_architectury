package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {
    @Inject(method = "removeNonCurses", at = @At("HEAD"), cancellable = true)
    private void grind(ItemStack item, int damage, int amount, CallbackInfoReturnable<ItemStack> cir) {
        Map<Enchantment, Integer> enchantments = AArcanaEnchantmentHelper.getAllEnchantments(item);
        Map<Relics, Integer> relics = RelicHelper.fromNbt(item.getTag());
        if (enchantments.isEmpty() && relics.isEmpty()) {
            ItemStack itemStack = item.copyWithCount(amount);
            itemStack.removeTagKey("Damage");
            cir.setReturnValue(itemStack);
            cir.cancel();
        } else {
            cir.setReturnValue(AArcanaEnchantmentHelper.convertEnchantmentsToScrap(enchantments));
            cir.cancel();
        }
    }
}
