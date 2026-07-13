package me.anticode.ascendant_arcana.mixin;

import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void cleanseShield(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!((Object) this instanceof ShieldItem && user instanceof Player player)) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.CLEANSE.get(), stack) > 0) {
            int usageTicks = 72000 - remainingUseTicks;
            if (usageTicks >= 20 && !user.getActiveEffects().isEmpty()) {
                boolean cleanse = false;
                for (MobEffectInstance statusEffect : user.getActiveEffects()) {
                    if (!statusEffect.getEffect().isBeneficial()) cleanse = true;
                }
                if (!cleanse) return;
                user.removeAllEffects();
                player.stopUsingItem();
                player.getCooldowns().addCooldown(stack.getItem(), 200);
                level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.7F, 2);
            }
        } else if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SONIC_BLAST.get(), stack) > 0) {
            int usageTicks = 72000 - remainingUseTicks;
            if (usageTicks == 20) {
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, player.getSoundSource(), 2F, 0.8F);
            } else if (usageTicks >= 54) {
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, player.getSoundSource(), 2F, 0.8F);
                Vec3 lookDir = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
                Vec3 startPos = player.getEyePosition().add(lookDir.scale(0.5));
                Vec3 endPos = startPos.add(lookDir.normalize().scale(12.5));
                for(int i = 1; i < 17; ++i) {
                    double delta = ((double)i) / 17;
                    Vec3 particlePos = startPos.lerp(endPos, delta);
                    for (Entity entity : level.getEntities(player, new AABB(particlePos.subtract(0.75, 0.75, 0.75), particlePos.add(0.75, 0.75, 0.75)), (entity) -> entity instanceof LivingEntity)) {
                        LivingEntity livingEntity = (LivingEntity)entity;
                        livingEntity.hurt(level.damageSources().sonicBoom(player), 4);
                    }
                    if (level instanceof ServerLevel serverWorld) serverWorld.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0.0F, 0.0F, 0.0F, 0.0F);
                }
                player.stopUsingItem();
                player.getCooldowns().addCooldown(stack.getItem(), 600);
            }
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void fixItemBarWithDurabilityRelic(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (RelicHelper.getValueFromNbt(stack.getTag(), Relics.DURABILITY) == 0) return;
        cir.setReturnValue(Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage()));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void fixItemBarColorWithDurabilityRelic(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (RelicHelper.getValueFromNbt(stack.getTag(), Relics.DURABILITY) == 0) return;
        float f = Math.max(0.0F, ((float)stack.getMaxDamage() - (float)stack.getDamageValue()) / (float)stack.getMaxDamage());
        cir.setReturnValue(Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F));
    }
}
