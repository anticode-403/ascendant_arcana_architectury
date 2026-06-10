package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void applyEnchantmentValues(
            ItemStack stack, Level world, LivingEntity user, int remainingUseTicks,
            CallbackInfo ci, @Local AbstractArrow projectile, @Local float f) {
        ItemHelper.applyPpeRelicsAndEnchantments(projectile, stack);

        RandomSource random = RandomSource.createNewThreadLocalInstance();
        int inaccuracy = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.INACCURACY_CURSE.get(), stack);
        float rand_pitch = random.nextFloat() * inaccuracy * 2f;
        float rand_yaw = random.nextFloat() * inaccuracy * 2f;
        float pitch = user.getXRot() + (random.nextBoolean() ? rand_pitch : -rand_pitch);
        float yaw = user.getYRot() + (random.nextBoolean() ? rand_yaw : -rand_yaw);
        projectile.shootFromRotation(user, pitch, yaw, 0.0f, f * 3.0f, 1.0f);
    }
}
