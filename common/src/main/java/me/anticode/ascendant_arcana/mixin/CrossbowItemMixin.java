package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.util.Mth;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrossbowItem.class, priority = 1500)
public class CrossbowItemMixin {
    @Shadow
    private static float getPowerForTime(int i, ItemStack arg) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "getArrow", at = @At(value = "RETURN"))
    private static void applyCrossbowEnchantmentLevels(
            Level level, LivingEntity livingEntity, ItemStack crossbow, ItemStack itemStack2, CallbackInfoReturnable<AbstractArrow> cir) {
        if (CrossbowItem.isCharged(crossbow)) {
            ItemHelper.applyPpeRelicsAndEnchantments(cir.getReturnValue(), crossbow);
        }
    }

    @Inject(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V", shift = At.Shift.AFTER))
    private static void applyCrossbowEnchantmentLevels(
            Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack,
            ItemStack itemStack2, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, @Local Projectile projectile) {
        RandomSource random = RandomSource.createNewThreadLocalInstance();
        if (projectile.getOwner() instanceof LivingEntity livingOwner) {
            int inaccuracy = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.INACCURACY_CURSE.get(), itemStack);
            if (inaccuracy == 0) return;
            float base_yaw = -projectile.getYRot();
            float base_pitch = -projectile.getXRot();
            float rand_pitch = random.nextFloat() * inaccuracy * 2f;
            float rand_yaw = random.nextFloat() * inaccuracy * 2f;
            float pitch = base_pitch + (random.nextBoolean() ? rand_pitch : -rand_pitch);
            float yaw = base_yaw + (random.nextBoolean() ? rand_yaw : -rand_yaw);
            projectile.shootFromRotation(livingOwner, pitch, yaw, 0.0f, speed, divergence);
        }
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;getPowerForTime(ILnet/minecraft/world/item/ItemStack;)F"))
    private float modifyGetPower(int i, ItemStack itemStack) {
        float hasteMultiplier = 1 + (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        return getPowerForTime(Mth.ceil(i * hasteMultiplier), itemStack);
    }

    @ModifyReturnValue(method = "getChargeDuration", at = @At(value = "RETURN"))
    private static int modifyChargeDuration(int i, ItemStack itemStack) {
        float hasteMultiplier = 1 - (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        return Mth.ceil(i * hasteMultiplier);
    }
}
