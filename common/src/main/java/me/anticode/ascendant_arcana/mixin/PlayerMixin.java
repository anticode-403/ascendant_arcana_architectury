package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.AArcanaPlayer;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.init.AArcanaSoundEvents;
import me.anticode.ascendant_arcana.logic.RelicHelper;
import me.anticode.ascendant_arcana.networking.ClientboundShieldBashPacket;
import me.anticode.ascendant_arcana.networking.ServerboundShieldBashPacket;
import me.anticode.ascendant_arcana.networking.ServerboundWhirlwindSync;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
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

import java.util.UUID;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements AArcanaPlayer {
    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private Abilities abilities;

    @Shadow
    public abstract float getCurrentItemAttackStrengthDelay();

    @Unique
    private int ascendant_arcana$shieldBashTicks = 0;

    @Unique
    private boolean ascendant_arcana$shieldBashing = false;

    @Unique
    private Vec3 ascendant_arcana$shieldBashDirection = Vec3.ZERO;

    @Unique
    private boolean ascendant_arcana$isWhirlwindCharging = false;

    @Unique
    private float ascendant_arcana$whirlwindCharge = 0;

    @Unique
    private boolean ascendant_arcana$isWhirlwinding = false;

    @Unique
    private int ascendant_arcana$whirlwindDuration = 0;

    @Unique
    private int ascendant_arcana$whirlwindCooldown = 0;

    @Unique
    private float ascendant_arcana$launchingCharge = 0;

    @Unique
    private static UUID LAUNCHING_BOOST = UUID.fromString("b85134d2-a828-4ec2-95ef-a044c8b12a6b");

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getXpNeededForNextLevel", at = @At("RETURN"))
    private int xp(int original) {
        if (AscendantArcana.config.xp_per_level <= 0) return original;
        return AscendantArcana.config.xp_per_level;
    }

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float applyHasteBonus(float original) {
        return (float) RelicHelper.applyAllRelicsOfType(RelicTypes.HASTE, original, ((Player)(Object)this).getMainHandItem().getTag());
    }

    @ModifyReturnValue(method = "getAttackStrengthScale", at = @At("RETURN"))
    private float modifyAttackCooldownProgress(float original) {
        ItemStack mainStack = getMainHandItem();
        if (mainStack.getItem() instanceof TieredItem && mainStack.hasTag()) {
            if (RelicHelper.containsAnyOfType(RelicTypes.HASTE, mainStack.getTag())) {
                float hasteMultiplier = 1 + ((float) RelicHelper.getAllRawBonusesOfType(RelicTypes.HASTE, mainStack.getTag()) / 2);
                if (original * hasteMultiplier > 1) return 1;
                return original * hasteMultiplier;
            }
        }
        return original;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"))
    private void giveMeganeuraEffect(Entity entity, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        int slayingTempoLevel = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.SLAYING_TEMPO.get(), player);
        int allegroLevel = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.ALLEGRO.get(), player);
        if (slayingTempoLevel != 0 || allegroLevel != 0) {
            float attackStrength = ((float)attackStrengthTicker + 0.5F) / getCurrentItemAttackStrengthDelay();
            float perfectWindow;
            if (allegroLevel != 0 && slayingTempoLevel != 0) {
                if (player.hasEffect(AArcanaMobEffects.ALLEGRO.get())) perfectWindow = 1.2F + ((player.getEffect(AArcanaMobEffects.ALLEGRO.get()).getAmplifier() + 1F) * 0.1F);
                else perfectWindow = 1.2F;
            }
            else if (player.hasEffect(AArcanaMobEffects.ALLEGRO.get())) perfectWindow = 1.1F + ((player.getEffect(AArcanaMobEffects.ALLEGRO.get()).getAmplifier() + 1F) * 0.1F);
            else perfectWindow = 1.1F;
            if (attackStrength < perfectWindow && attackStrength > 1F) {
                if (slayingTempoLevel != 0) {
                    int amplifier;
                    if (player.hasEffect(AArcanaMobEffects.MEGANEURA.get())) amplifier = player.getEffect(AArcanaMobEffects.MEGANEURA.get()).getAmplifier() + 1;
                    else amplifier = 0;
                    player.addEffect(new MobEffectInstance(AArcanaMobEffects.MEGANEURA.get(), 100, amplifier, false, false, true));
                }
                if (allegroLevel != 0) {
                    int amplifier;
                    if (player.hasEffect(AArcanaMobEffects.ALLEGRO.get())) amplifier = player.getEffect(AArcanaMobEffects.ALLEGRO.get()).getAmplifier() + 1;
                    else amplifier = 0;
                    player.addEffect(new MobEffectInstance(AArcanaMobEffects.ALLEGRO.get(), 20 + (allegroLevel * 20), amplifier, false, false, true));
                }
            }
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"), cancellable = true)
    private void protectiveEcho(DamageSource source, float amount, CallbackInfo ci) {
        if (amount < 5) return;
        if (getEffect(AArcanaMobEffects.ECHOING_DAMAGE.get()) != null) return;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.PROTECTIVE_ECHO.get(), (LivingEntity) (Object) this) == 0) return;
        int duration = 100 * Math.max((int)amount / 10, 1);
        int strength = Math.max((int)amount / (duration / 20), 1);
        forceAddEffect(new MobEffectInstance(AArcanaMobEffects.ECHOING_DAMAGE.get(), duration + 20, strength), (LivingEntity)(Object)this);
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
        if (ascendant_arcana$shieldBashing) {
            int shieldBashLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.BASHING.get(), player.getUseItem());
            if (shieldBashLevel < 0) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    NetworkManager.sendToPlayers(serverLevel.players(), ClientboundShieldBashPacket.Id, new ClientboundShieldBashPacket(player.getUUID(), false).write());
                    ascendant_arcana$setShieldBashStatus(false);
                } else {
                    NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(false).write());
                }
            } else {
                AABB shieldbashBox = player.getBoundingBox().move(ascendant_arcana$shieldBashDirection.scale(player.getBoundingBox().getXsize())).inflate(0.75);
                int stepLength = ascendant_arcana$shieldBashTicks >= 3 ? 2 : 1;
                for (int i = 0; i < stepLength; i++) {
                    boolean doBreak = false;
                    for (Entity entity : player.level().getEntities(player, shieldbashBox.move(ascendant_arcana$shieldBashDirection.scale(i)))) {
                        if (entity == player) continue;
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.hurt(player.damageSources().playerAttack(player), 4);
                            livingEntity.knockback(0.8 * shieldBashLevel, -ascendant_arcana$shieldBashDirection.x, -ascendant_arcana$shieldBashDirection.z);
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
                    if (doBreak) {
                        if (!player.level().isClientSide()) {
                            player.level().playSound(null, player.position().x, player.position().y, player.position().z, AArcanaSoundEvents.SHIELD_BASH_HIT.get(), SoundSource.PLAYERS, 1f, 1f);
                        }
                        break;
                    }
                }
                player.move(MoverType.SELF, ascendant_arcana$shieldBashDirection.scale(stepLength));
                player.hasImpulse = true;
                ascendant_arcana$shieldBashTicks--;
                if (ascendant_arcana$shieldBashTicks == 0) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        NetworkManager.sendToPlayers(serverLevel.players(), ClientboundShieldBashPacket.Id, new ClientboundShieldBashPacket(player.getUUID(), false).write());
                        ascendant_arcana$setShieldBashStatus(false);
                    } else {
                        NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(false).write());
                    }
                }
            }
        }
        if (hasEffect(AArcanaMobEffects.PREPARED.get()) && (!isUsingItem() || EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.PREPARED_SHOT.get(), getUseItem()) == 0)) {
            removeEffect(AArcanaMobEffects.PREPARED.get());
        }
        if (player.isCrouching() && player.onGround() && !player.isFallFlying() && !player.isSwimming() && EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.LAUNCHING.get(), player) > 0) {
            ascendant_arcana$launchingCharge += 0.025F + (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.LAUNCHING.get(), player) * 0.25F);
            if (ascendant_arcana$launchingCharge >= 3) ascendant_arcana$launchingCharge = 3;
        } else if (ascendant_arcana$launchingCharge != 0) ascendant_arcana$launchingCharge = 0;
    }

    @Inject(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V", shift = At.Shift.AFTER))
    private void applyLaunchingJumpBoost(CallbackInfo ci) {
        double multiplier = 1 + (Mth.floor(ascendant_arcana$launchingCharge) * 0.5);
        if (multiplier > 1) {
            Player player = (Player)(Object)this;
            player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y * multiplier, player.getDeltaMovement().z);
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.AFTER))
    private void whirlwindInject(Vec3 vec3, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (ascendant_arcana$whirlwindCooldown > 0) ascendant_arcana$whirlwindCooldown--;
        if (ascendant_arcana$isWhirlwinding) {
            ascendant_arcana$whirlwindDuration--;
            if (ascendant_arcana$whirlwindDuration <= 0) {
                ascendant_arcana$isWhirlwinding = false;
                NetworkManager.sendToServer(ServerboundWhirlwindSync.Id, new ServerboundWhirlwindSync(false, false).write());
            }
            player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().scale(-0.3)));
        }
        if (!player.isFallFlying() || !player.isCrouching() || EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.WHIRLWIND.get(), player) <= 0) {
            if (player.isFallFlying() && ascendant_arcana$isWhirlwindCharging && EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.WHIRLWIND.get(), player) > 0) {
                ascendant_arcana$isWhirlwindCharging = false;
                ascendant_arcana$isWhirlwinding = true;
                ascendant_arcana$whirlwindDuration = Mth.floor(ascendant_arcana$whirlwindCharge) * 4;
                ascendant_arcana$whirlwindCooldown = ascendant_arcana$whirlwindDuration + 100;
                player.setDeltaMovement(player.getLookAngle().scale(Mth.floor(ascendant_arcana$whirlwindCharge) * 2));
                NetworkManager.sendToServer(ServerboundWhirlwindSync.Id, new ServerboundWhirlwindSync(false, true).write());
            }
            ascendant_arcana$whirlwindCharge = 0;
            return;
        }
        if (ascendant_arcana$whirlwindCooldown > 0) return;
        ascendant_arcana$isWhirlwinding = false;
        ascendant_arcana$whirlwindDuration = 0;
        Vec3 oldMovement = player.getDeltaMovement();
        player.setDeltaMovement(oldMovement.x * 0.9, oldMovement.y * 0.5, oldMovement.z * 0.9);
        if (!ascendant_arcana$isWhirlwindCharging) {
            ascendant_arcana$isWhirlwindCharging = true;
            NetworkManager.sendToServer(ServerboundWhirlwindSync.Id, new ServerboundWhirlwindSync(true, false).write());
        }
        ascendant_arcana$whirlwindCharge += 0.05F;
        if (ascendant_arcana$whirlwindCharge >= 3) ascendant_arcana$whirlwindCharge = 3;

    }

    @Override
    public void ascendant_arcana$setShieldBashStatus(boolean status) {
        if (ascendant_arcana$shieldBashing == status) return;
        Player player = (Player)(Object)this;
        ascendant_arcana$shieldBashing = status;
        if (ascendant_arcana$shieldBashing) {
            if (!(player.getUseItem().getItem() instanceof ShieldItem)) {
                ascendant_arcana$shieldBashing = false;
                return;
            }
            int shieldBashLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.BASHING.get(), player.getUseItem());
            ascendant_arcana$shieldBashTicks = 1 + (shieldBashLevel < 3 ? shieldBashLevel * 2 : 5);
            ascendant_arcana$shieldBashDirection = player.getLookAngle().with(Direction.Axis.Y, 0).normalize();
            if (!player.level().isClientSide()) {
                player.level().playSound(null, player, AArcanaSoundEvents.SHIELD_BASH_START.get(), SoundSource.PLAYERS, 1f, 2f);
                player.getUseItem().hurtAndBreak(3, player, (livingEntity) -> livingEntity.broadcastBreakEvent(player.getUsedItemHand()));
            }
        } else {
            player.getCooldowns().addCooldown(Items.SHIELD, 400);
            player.stopUsingItem();
            if (ascendant_arcana$shieldBashTicks != 0) {
                player.move(MoverType.SELF, ascendant_arcana$shieldBashDirection);
            }
            ascendant_arcana$shieldBashTicks = 0;
            ascendant_arcana$shieldBashDirection = Vec3.ZERO;
            player.setDeltaMovement(player.getDeltaMovement().multiply(0, 1, 0));
        }
    }

    @Override
    public void ascendant_arcana$setWhirlwindCharge(boolean status) {
        ascendant_arcana$isWhirlwindCharging = status;
    }

    @Override
    public boolean ascendant_arcana$isWhirlwindCharging() {
        return ascendant_arcana$isWhirlwindCharging;
    }

    @Override
    public int ascendant_arcana$getWhirlwindCharge() {
        return Mth.floor(ascendant_arcana$whirlwindCharge);
    }

    @Override
    public void ascendant_arcana$setWhirlwinding(boolean status) {
        ascendant_arcana$isWhirlwinding = status;
    }

    @Override
    public boolean ascendant_arcana$isWhirlwinding() {
        return ascendant_arcana$isWhirlwinding;
    }

    @Override
    public float ascendant_arcana$getWhirlwindCooldown() {
        return Math.min(1F, ((float)ascendant_arcana$whirlwindCooldown / 100F));
    }

    @Override
    public int ascendant_arcana$getLaunchingCharge() {
        return Mth.floor(ascendant_arcana$launchingCharge);
    }

    @Override
    public boolean ascendant_arcana$isLaunching() {
        return ascendant_arcana$launchingCharge != 0;
    }

    @Override
    public boolean ascendant_arcana$getShieldBashStatus() {
        return ascendant_arcana$shieldBashing;
    }

    @Override
    public int ascendant_arcana$getShieldBashTicks() {
        return ascendant_arcana$shieldBashTicks;
    }

    @Override
    public Vec3 ascendant_arcana$getShieldBashDirection() {
        return ascendant_arcana$shieldBashDirection;
    }
}
