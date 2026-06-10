package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void applyEnchantmentValues(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci,
                                        @Local ThrownTrident tridentEntity) {
        int ambushLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.AMBUSH.get(), stack);
        int lifetideLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.LIFETIDE.get(), stack);
        int sunderingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SUNDERING.get(), stack);

        EnchantedTrident enchantedTrident = (EnchantedTrident) tridentEntity;
        enchantedTrident.ascendant_arcana$setAmbushLevel(ambushLevel);
        enchantedTrident.ascendant_arcana$setLifetideLevel(lifetideLevel);
        enchantedTrident.ascendant_arcana$setSunderingLevel(sunderingLevel);
    }
}
