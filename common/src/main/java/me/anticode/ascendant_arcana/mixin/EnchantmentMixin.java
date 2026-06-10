package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @ModifyReturnValue(method = "getRarity", at = @At("RETURN"))
    private Enchantment.Rarity modifyEnchantmentRarities(Enchantment.Rarity original) {
        Enchantment enchantment = (Enchantment)(Object)this;
        ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (AscendantArcana.config.overwritten_rarities.containsKey(id.toString())) {
            return switch(AscendantArcana.config.overwritten_rarities.get(id.toString())) {
                case 2 -> Enchantment.Rarity.UNCOMMON;
                case 3 -> Enchantment.Rarity.RARE;
                case 4 -> Enchantment.Rarity.VERY_RARE;
                default -> Enchantment.Rarity.COMMON;
            };
        }
        return original;
    }
}
