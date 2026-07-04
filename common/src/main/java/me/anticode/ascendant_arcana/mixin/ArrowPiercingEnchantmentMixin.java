package me.anticode.ascendant_arcana.mixin;

import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArrowPiercingEnchantment.class)
public class ArrowPiercingEnchantmentMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;<init>(Lnet/minecraft/world/item/enchantment/Enchantment$Rarity;Lnet/minecraft/world/item/enchantment/EnchantmentCategory;[Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private static EnchantmentCategory piercingBows(EnchantmentCategory category) {
        return EnchantmentCategory.BOW;
    }
}
