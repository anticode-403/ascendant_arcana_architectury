package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyReturnValue(method = "getXpNeededForNextLevel", at = @At("RETURN"))
    private int xp(int original) {
        return Math.max(0, AscendantArcana.config.xp_per_level);
    }

    @ModifyReturnValue(method = "getAttackStrengthScale", at = @At("RETURN"))
    private float modifyAttackCooldownProgress(float original) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        ItemStack mainStack = livingEntity.getMainHandItem();
        if (mainStack.getItem() instanceof TieredItem) {
            Map<Relics, Integer> relics = RelicHelper.fromNbt(mainStack.getOrCreateTag());
            if (relics.containsKey(Relics.HASTE)) {
                float hasteMultiplier = 1 + ((float)RelicHelper.getTooltipStrength(Relics.HASTE, relics.get(Relics.HASTE)) * 0.005F);
                if (original * hasteMultiplier > 1) return 1;
                return original * hasteMultiplier;
            }
        }
        return original;
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"), cancellable = true)
    private void protectiveEcho(DamageSource source, float amount, CallbackInfo ci) {
        if (amount < 5) return;
        if (((LivingEntity)(Object)this).getEffect(AArcanaMobEffects.ECHOING_DAMAGE.get()) != null) return;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.PROTECTIVE_ECHO.get(), (LivingEntity) (Object) this) == 0) return;
        int duration = 100 * Math.max((int)amount / 10, 1);
        int strength = Math.max((int)amount / (duration / 20), 1);
        ((LivingEntity)(Object)this).forceAddEffect(new MobEffectInstance(AArcanaMobEffects.ECHOING_DAMAGE.get(), duration + 20, strength), (LivingEntity)(Object)this);
        ci.cancel();
    }

    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void getProjectileType(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (itemStack.getItem() instanceof ProjectileWeaponItem weapon) {
            int infinityLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack);
            if (infinityLevel == 0) return;

            ItemStack arrowStack = Items.ARROW.getDefaultInstance();
            if (weapon.getAllSupportedProjectiles().test(arrowStack) || weapon.getSupportedHeldProjectiles().test(arrowStack))
                cir.setReturnValue(arrowStack);
        }
    }
}
