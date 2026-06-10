package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.util.RandomSource;
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
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V"))
    private static void applyCrossbowEnchantmentLevels(
            Projectile projectile, double d, double e, double f, float g, float h) {
        RandomSource random = RandomSource.createNewThreadLocalInstance();
        if (projectile.getOwner() instanceof LivingEntity livingOwner) {
            float base_yaw = -projectile.getYRot();
            float base_pitch = -projectile.getXRot();
            int inaccuracy = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.INACCURACY_CURSE.get(), livingOwner);
            float rand_pitch = random.nextFloat() * inaccuracy * 2f;
            float rand_yaw = random.nextFloat() * inaccuracy * 2f;
            float pitch = base_pitch + (random.nextBoolean() ? rand_pitch : -rand_pitch);
            float yaw = base_yaw + (random.nextBoolean() ? rand_yaw : -rand_yaw);
            projectile.shootFromRotation(livingOwner, pitch, yaw, 0.0f, g, h);
        }
    }
}
