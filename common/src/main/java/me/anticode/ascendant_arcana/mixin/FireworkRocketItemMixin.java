package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z"))
    private boolean doNotRocketFlyWithoutRocketRiding(Player instance, Operation<Boolean> original) {
        return original.call(instance) && EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.ROCKET_RIDING.get(), instance) > 0;
    }
}
