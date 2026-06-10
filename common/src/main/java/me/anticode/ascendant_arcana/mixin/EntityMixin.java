package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "updateSwimming")
    private void updateSwimming(CallbackInfo cbi) {
        if((Entity) ((Object)this) instanceof LivingEntity living && living.isSwimming()) {
            if(EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.DEPTHS_CURSE.get(), living) > 0) {
                living.setSwimming(false);
            }
        }
    }
}
