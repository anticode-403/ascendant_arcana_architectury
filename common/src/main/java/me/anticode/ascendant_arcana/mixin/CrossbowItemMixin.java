package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.api.EnchantedRocket;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CrossbowItem.class, priority = 1500)
public class CrossbowItemMixin {
    @Shadow
    private static float getPowerForTime(int i, ItemStack arg) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }


    @Shadow
    public static int getChargeDuration(ItemStack itemStack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public static boolean isCharged(ItemStack itemStack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static void shootProjectile(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, ItemStack itemStack2, float f, boolean bl, float g, float h, float i) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static List<ItemStack> getChargedProjectiles(ItemStack itemStack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static float getRandomShotPitch(boolean bl, RandomSource randomSource) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static float getShootingPower(ItemStack itemStack) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean loadMultiple(ItemStack itemStack, Operation<Boolean> original, @Local(argsOnly = true) Player player) {
        int repeatingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.REPEATING.get(), itemStack);
        int salvoLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SALVO.get(), itemStack);
        if (repeatingLevel + salvoLevel == 0) return original.call(itemStack);
        int maxProjectiles = salvoLevel != 0 ? 2 + salvoLevel * 2 : repeatingLevel * 2;
        if (getChargedProjectiles(itemStack).size() >= maxProjectiles) return original.call(itemStack);
        if (player.isSecondaryUseActive()) return false;
        return original.call(itemStack);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;setCharged(Lnet/minecraft/world/item/ItemStack;Z)V"))
    private void setCharged(ItemStack itemStack, boolean bl, Operation<Void> original) {
        if (getChargedProjectiles(itemStack).isEmpty()) {
            original.call(itemStack, bl);
        }
    }

    @WrapOperation(method = "performShooting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private static boolean onlyFireOnceWithoutMultishot(ItemStack instance, Operation<Boolean> original, @Local(argsOnly = true) ItemStack crossbowStack, @Local int i,
            @Local(argsOnly = true) Level level, @Local(argsOnly = true) LivingEntity livingEntity, @Local(argsOnly = true) InteractionHand interactionHand, @Local List<ItemStack> ammo) {
        if (i > 0 && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbowStack) == 0) {
            if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SALVO.get(), crossbowStack) > 0) {
                ItemStack projectileStack = ammo.get(i);
                boolean bl = livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild;
                if (!projectileStack.isEmpty()) {
                    shootProjectile(level, livingEntity, interactionHand, crossbowStack, projectileStack, getRandomShotPitch(livingEntity.getRandom().nextBoolean(), livingEntity.getRandom()), bl, getShootingPower(projectileStack), 1F, 0.0F);
                }
            }
            return true;
        }
        else return original.call(instance);
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void addAdditionalArrows(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.REPEATING.get(), itemStack) + EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SALVO.get(), itemStack) == 0) return;
        int j = getChargeDuration(itemStack) - i;
        float f = getPowerForTime(j, itemStack);
        if (f >= 1 && isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
            SoundSource soundSource = livingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            level.playSound((Player)null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundSource, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
            ci.cancel();
        }
    }

    @WrapOperation(method = "onCrossbowShot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;clearChargedProjectiles(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void modifyClearChargedProjectiles(ItemStack itemStack, Operation<Void> original) {
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.REPEATING.get(), itemStack) > 0) {
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag != null) {
                ListTag listTag = compoundTag.getList("ChargedProjectiles", 10);
                listTag.remove(0);
                compoundTag.put("ChargedProjectiles", listTag);
            }
            return;
        }
        original.call(itemStack);
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
            if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SALVO.get(), itemStack) > 0) inaccuracy += 4;
            if (inaccuracy > 0) {
                float base_yaw = -projectile.getYRot();
                float base_pitch = -projectile.getXRot();
                float rand_pitch = random.nextFloat() * inaccuracy * 2f;
                float rand_yaw = random.nextFloat() * inaccuracy * 2f;
                float pitch = base_pitch + (random.nextBoolean() ? rand_pitch : -rand_pitch);
                float yaw = base_yaw + (random.nextBoolean() ? rand_yaw : -rand_yaw);
                projectile.shootFromRotation(livingOwner, pitch, yaw, 0.0f, speed, divergence);
            }

            int rocketry = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.ROCKETRY.get(), itemStack);
            if (rocketry > 0) {
                if (projectile instanceof FireworkRocketEntity rocket) {
                    EnchantedRocket enchantedRocket = (EnchantedRocket) rocket;
                    enchantedRocket.ascendant_arcana$setRocketryLevel(rocketry);
                    double damageMultiplier = 1 + RelicHelper.getTooltipStrength(Relics.DAMAGE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.DAMAGE))*0.01;
                    enchantedRocket.asecndant_arcana$setDamageMultiplier((float) damageMultiplier);
                }
            }
        }
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;getPowerForTime(ILnet/minecraft/world/item/ItemStack;)F"))
    private float modifyGetPower(int i, ItemStack itemStack) {
        float hasteMultiplier = 1 + (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        float multiLoadMultiplier = isCharged(itemStack) ? 1.5F : 1F;
        return getPowerForTime(Mth.ceil(i * hasteMultiplier * multiLoadMultiplier), itemStack);
    }

    @ModifyReturnValue(method = "getChargeDuration", at = @At(value = "RETURN"))
    private static int modifyChargeDuration(int i, ItemStack itemStack) {
        float hasteMultiplier = 1 - (float) RelicHelper.getTooltipStrength(Relics.HASTE, RelicHelper.getValueFromNbt(itemStack.getOrCreateTag(), Relics.HASTE)) * 0.005F;
        float multiLoadMultiplier = isCharged(itemStack) ? 0.5F : 1F;
        return Mth.ceil(i * hasteMultiplier * multiLoadMultiplier);
    }
}
