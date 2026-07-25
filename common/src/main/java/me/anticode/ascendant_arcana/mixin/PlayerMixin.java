package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.logic.Relics;
import me.anticode.ascendant_arcana.networking.ClientboundShieldBashPacket;
import me.anticode.ascendant_arcana.networking.ServerboundShieldBashPacket;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin implements AArcanaPlayer {
    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private Abilities abilities;

    @Unique
    private int shieldBashTicks = 0;

    @Unique
    private boolean shieldBashing = false;

    @Unique
    private Vec3 shieldBashDirection = Vec3.ZERO;

    @ModifyReturnValue(method = "getXpNeededForNextLevel", at = @At("RETURN"))
    private int xp(int original) {
        if (AscendantArcana.config.xp_per_level <= 0) return original;
        return AscendantArcana.config.xp_per_level;
    }

    @ModifyReturnValue(method = "getAttackStrengthScale", at = @At("RETURN"))
    private float modifyAttackCooldownProgress(float original) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        ItemStack mainStack = livingEntity.getMainHandItem();
        if (mainStack.getItem() instanceof TieredItem && mainStack.hasTag()) {
            Map<Relics, Integer> relics = RelicHelper.fromNbt(mainStack.getTag());
            if (relics.containsKey(Relics.HASTE)) {
                float hasteMultiplier = 1 + (float) RelicHelper.getStrengthFromNbt(Relics.HASTE, mainStack.getTag()) / 2;
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
            if (infinityLevel > 0) {
                ItemStack arrowStack = Items.ARROW.getDefaultInstance();
                if (weapon.getAllSupportedProjectiles().test(arrowStack) || weapon.getSupportedHeldProjectiles().test(arrowStack)) {
                    cir.setReturnValue(arrowStack);
                    return;
                }
            }
            if (itemStack.getItem() instanceof CrossbowItem) {
                Predicate<ItemStack> predicate = (item) -> item.is(ItemTags.ARROWS);
                ItemStack creativeItemStack = new ItemStack(Items.ARROW);

                int rocketryLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.ROCKETRY.get(), itemStack);
                if (rocketryLevel > 0) {
                    predicate = (item) -> item.is(Items.FIREWORK_ROCKET);
                    creativeItemStack = new ItemStack(Items.FIREWORK_ROCKET);
                }

                int blazeboltLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.BLAZEBOLT.get(), itemStack);
                if (blazeboltLevel > 0) {
                    predicate = (item) -> item.is(Items.BLAZE_ROD);
                    creativeItemStack = new ItemStack(Items.BLAZE_ROD);
                }

                int shattershotLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SHATTERSHOT.get(), itemStack);
                if (shattershotLevel > 0) {
                    predicate = (item) -> item.is(Items.AMETHYST_SHARD);
                    creativeItemStack = new ItemStack(Items.AMETHYST_SHARD);
                }

                ItemStack heldStack = ProjectileWeaponItem.getHeldProjectile((LivingEntity) (Object) this, predicate);
                if (!heldStack.isEmpty()) {
                    cir.setReturnValue(heldStack);
                } else {
                    for(int i = 0; i < inventory.getContainerSize(); ++i) {
                        ItemStack inventoryItem = inventory.getItem(i);
                        if (predicate.test(inventoryItem)) {
                            cir.setReturnValue(inventoryItem);
                            return;
                        }
                    }

                    cir.setReturnValue(abilities.instabuild ? creativeItemStack : ItemStack.EMPTY);
                }
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tick()V"))
    private void shieldBash(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (shieldBashing) {
            int shieldBashLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SHIELD_BASH.get(), player.getUseItem());
            if (shieldBashLevel < 0) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    NetworkManager.sendToPlayers(serverLevel.players(), ClientboundShieldBashPacket.Id, new ClientboundShieldBashPacket(player.getUUID(), false).write());
                    ascendant_arcana$setShieldBashStatus(false);
                } else {
                    NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(false).write());
                }
            } else {
                AABB shieldbashBox = player.getBoundingBox().move(shieldBashDirection.scale(player.getBoundingBox().getXsize())).inflate(0.75);
                int stepLength = shieldBashTicks >= 3 ? 2 : 1;
                for (int i = 0; i < stepLength; i++) {
                    boolean doBreak = false;
                    for (Entity entity : player.level().getEntities(player, shieldbashBox.move(shieldBashDirection.scale(i)))) {
                        if (entity == player) continue;
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.hurt(player.damageSources().playerAttack(player), 4);
                            livingEntity.knockback(0.8 * shieldBashLevel, -shieldBashDirection.x, -shieldBashDirection.z);
                        }
                        if (player.level() instanceof ServerLevel serverLevel) {
                            NetworkManager.sendToPlayers(serverLevel.players(), ClientboundShieldBashPacket.Id, new ClientboundShieldBashPacket(player.getUUID(), false).write());
                            ascendant_arcana$setShieldBashStatus(false);
                        } else {
                            NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(false).write());
                        }
                        doBreak = true;
                        if (!player.level().isClientSide()) {
                            player.getUseItem().hurtAndBreak(2, player, (livingEntity) -> livingEntity.broadcastBreakEvent(player.getUsedItemHand()));
                        }
                    }
                    if (doBreak) break;
                }
                player.move(MoverType.SELF, shieldBashDirection.scale(stepLength));
                player.hasImpulse = true;
                shieldBashTicks--;
                if (shieldBashTicks == 0) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        NetworkManager.sendToPlayers(serverLevel.players(), ClientboundShieldBashPacket.Id, new ClientboundShieldBashPacket(player.getUUID(), false).write());
                        ascendant_arcana$setShieldBashStatus(false);
                    } else {
                        NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(false).write());
                    }
                }
            }
        }
    }

    @Override
    public void ascendant_arcana$setShieldBashStatus(boolean status) {
        if (shieldBashing == status) return;
        Player player = (Player)(Object)this;
        shieldBashing = status;
        if (shieldBashing) {
            if (!(player.getUseItem().getItem() instanceof ShieldItem)) {
                shieldBashing = false;
                return;
            }
            int shieldBashLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.SHIELD_BASH.get(), player.getUseItem());
            shieldBashTicks = 1 + (shieldBashLevel < 3 ? shieldBashLevel * 2 : 5);
            shieldBashDirection = player.getLookAngle().with(Direction.Axis.Y, 0).normalize();
            if (!player.level().isClientSide()) {
                player.getUseItem().hurtAndBreak(3, player, (livingEntity) -> livingEntity.broadcastBreakEvent(player.getUsedItemHand()));
            }
        } else {
            player.getCooldowns().addCooldown(Items.SHIELD, 20);
            player.stopUsingItem();
            if (shieldBashTicks != 0) {
                player.move(MoverType.SELF, shieldBashDirection);
            }
            shieldBashTicks = 0;
            shieldBashDirection = Vec3.ZERO;
            player.setDeltaMovement(player.getDeltaMovement().multiply(0, 1, 0));
        }
    }

    @Override
    public boolean ascendant_arcana$getShieldBashStatus() {
        return shieldBashing;
    }

    @Override
    public int ascendant_arcana$getShieldBashTicks() {
        return shieldBashTicks;
    }

    @Override
    public Vec3 ascendant_arcana$getShieldBashDirection() {
        return shieldBashDirection;
    }
}
