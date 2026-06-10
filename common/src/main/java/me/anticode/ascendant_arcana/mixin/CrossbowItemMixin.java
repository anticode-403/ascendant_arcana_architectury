package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrossbowItem.class, priority = 1500)
public class CrossbowItemMixin {
    @Inject(method = "getArrow", at = @At(value = "RETURN"))
    private static void applyCrossbowEnchantmentLevels(
            Level level, LivingEntity livingEntity, ItemStack crossbow, ItemStack itemStack2, CallbackInfoReturnable<AbstractArrow> cir) {
        if (CrossbowItem.isCharged(crossbow)) {
            ItemHelper.applyPpeRelicsAndEnchantments(cir.getReturnValue(), crossbow);
        }
    }

    @Inject(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V", shift = At.Shift.AFTER))
    private static void applyCrossbowEnchantmentLevels(
            Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack crossbow, ItemStack itemStack2, float f, boolean bl, float g, float h, float i, CallbackInfo ci, @Local Projectile projectileEntity) {
        RandomSource random = RandomSource.createNewThreadLocalInstance();

        float base_yaw = -projectileEntity.getYRot();
        float base_pitch = -projectileEntity.getXRot();
        int inaccuracy = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.INACCURACY_CURSE.get(), crossbow);
        float rand_pitch = random.nextFloat() * inaccuracy * 2f;
        float rand_yaw = random.nextFloat() * inaccuracy * 2f;
        float pitch = base_pitch + (random.nextBoolean() ? rand_pitch : -rand_pitch);
        float yaw = base_yaw + (random.nextBoolean() ? rand_yaw : -rand_yaw);
        projectileEntity.shootFromRotation(livingEntity, pitch, yaw, 0.0f, g, h);
    }
}
