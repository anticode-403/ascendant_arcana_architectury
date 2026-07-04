package me.anticode.ascendant_arcana.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.anticode.ascendant_arcana.enchantment.HellWalker;
import me.anticode.ascendant_arcana.enchantment.TickableAttributeEnchantment;
import me.anticode.ascendant_arcana.enchantment.TurtleHeart;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaMobEffects;
import me.anticode.ascendant_arcana.logic.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Shadow
    public abstract AttributeMap getAttributes();

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    @Nullable
    public abstract MobEffectInstance getEffect(MobEffect effect);

    @Shadow
    public abstract void forceAddEffect(MobEffectInstance effect, @Nullable Entity source);

    @Shadow
    public abstract boolean removeEffect(MobEffect type);

    @Shadow
    public abstract int getTicksUsingItem();

    @Shadow
    public abstract boolean addEffect(MobEffectInstance effect, @Nullable Entity source);

    @Shadow
    protected int useItemRemaining;

    @Shadow
    protected ItemStack useItem;

    @Shadow
    public abstract LivingEntity getLastAttacker();

    @Unique
    private Map<AArcanaEnchantments.IndirectHeartDamageTypes, Integer> heartAttackers = new EnumMap<>(AArcanaEnchantments.IndirectHeartDamageTypes.class);

    @Unique
    private final Collection<Tuple<EquipmentSlot, ItemStack>> attributeStacks = new ArrayList<>();

    @Inject(method = "setLastHurtMob", at = @At("HEAD"))
    private void removeCrossCounterOnAttack(Entity entity, CallbackInfo ci) {
        if (getEffect(AArcanaMobEffects.CROSS_COUNTER.get()) != null) {
            removeEffect(AArcanaMobEffects.CROSS_COUNTER.get());
        }
    }

    @Inject(method = "blockUsingShield", at = @At("HEAD"))
    private void addCrossCounterOnParry(LivingEntity attacker, CallbackInfo ci) {
        int level = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.CROSS_COUNTER.get(), (LivingEntity)(Object)this);
        if (level <= 0) return;
        int useTime = getTicksUsingItem();
        if (useTime <= 0) return;
        if (useTime > 5 + 5 * level) return;
        MobEffectInstance crossCounter = new MobEffectInstance(AArcanaMobEffects.CROSS_COUNTER.get(), 15 * level, 0, false, false, true);
        addEffect(crossCounter, (LivingEntity)(Object)this);
    }

    @ModifyReturnValue(method = "getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", at = @At("RETURN"))
    private double implementSurefootMovementResistance(double original, @Local(argsOnly = true) Attribute attribute) {
        if (attribute != Attributes.MOVEMENT_SPEED) return original;
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.SUREFOOT.get(), livingEntity) <= 0) return original;
        double baseValue = livingEntity.getAttributeBaseValue(attribute);
        if (original >= baseValue) return original;
        return baseValue - (baseValue - original)/2;
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void createLivingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        if (!AArcanaAttributes.DAMAGE_TAKEN.isPresent()) AArcanaAttributes.initialize();
        cir.getReturnValue().add(AArcanaAttributes.PROTECTION.get()).add(AArcanaAttributes.DAMAGE_TAKEN.get()).add(AArcanaAttributes.DRAW_SPEED.get());
    }

    @ModifyReturnValue(method = "getJumpPower", at = @At("RETURN"))
    private float modifyJumpVelocity(float original) {
        MobEffectInstance hobbled = getEffect(AArcanaMobEffects.HOBBLED.get());
        if (hobbled != null) {
            return original * (1F - (0.1F * hobbled.getAmplifier()));
        }
        return original;
    }

    @Inject(method = "onChangedBlock", at = @At("HEAD"))
    private void applyMovementEffects(BlockPos pos, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.HELLWALKER.get(), livingEntity) > 0) {
            HellWalker.freezeLava(livingEntity, livingEntity.level(), pos);
        }
    }


    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean bypassCooldownSameAttacker(DamageSource instance, TagKey<DamageType> tag, Operation<Boolean> original) {
        if (tag.equals(DamageTypeTags.BYPASSES_COOLDOWN)) return original.call(instance, tag) || (instance.is(DamageTypes.ARROW) && Objects.equals(instance.getEntity(), getLastAttacker()));
        else return original.call(instance, tag);
    }

    @ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private float applyProtectionStat(float original, @Local(argsOnly = true) DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) return original;
        double protectionStrength = getAttributeValue(AArcanaAttributes.PROTECTION.get());
        float multiplier = 2F - (float) protectionStrength;
        return original * multiplier;
    }

    @Inject(method = "getUseItemRemainingTicks", at = @At("HEAD"), cancellable = true)
    private void getItemUseTimeLeft(CallbackInfoReturnable<Integer> info) {
        var value  = useItemRemaining;
        var entity = (LivingEntity)(Object)this;
        if (entity.isUsingItem())  {
            var useAction = useItem.getUseAnimation();
            if (useAction == UseAnim.BOW || useAction == UseAnim.CROSSBOW || useAction == UseAnim.SPEAR) {
                var progress = useItem.getUseDuration() - value;
                var haste = entity.getAttributeValue(AArcanaAttributes.DRAW_SPEED.get());
                var newProgress = (int) (progress * haste);
                info.setReturnValue(useItem.getUseDuration() - newProgress);
            }
        }
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MobEffectInstance injectMobEffectModifiers(MobEffectInstance effect) {
        MobEffectInstance newInstance = new MobEffectInstance(effect);
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.ALCHEMISTS_HEART.get(), (LivingEntity) (Object)this) >= 1
                && newInstance.getEffect().isBeneficial()) {
            newInstance = new MobEffectInstance(newInstance.getEffect(), newInstance.getDuration(), newInstance.getAmplifier() + 1, newInstance.isAmbient(), newInstance.isVisible());
        }
        return newInstance;
    }

    @ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("TAIL"))
    private float injectDamageModifiers(float damage, @Local(argsOnly = true) DamageSource source) {
        if (heartAttackers == null) {
            heartAttackers = new EnumMap<>(AArcanaEnchantments.IndirectHeartDamageTypes.class);
        }
        if (source.getEntity() != null && source.getEntity() instanceof LivingEntity attacker) {
            if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.NETHER_HEART.get(), attacker) > 0)
                heartAttackers.put(AArcanaEnchantments.IndirectHeartDamageTypes.NETHER, 0);
            else if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.COLDHEART.get(), attacker) > 0)
                heartAttackers.put(AArcanaEnchantments.IndirectHeartDamageTypes.COLD, 0);
            else if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.STORM_HEART.get(), attacker) > 0)
                heartAttackers.put(AArcanaEnchantments.IndirectHeartDamageTypes.STORM, 0);
            else if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.TURTLE_HEART.get(), attacker) > 0)
                damage *= 0.75F;
            else if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.WITCH_HEART.get(), attacker) > 0
                    && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)))
                damage *= 1.2F;
            else if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.BLADEHEART.get(), attacker) > 0
                    && (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypes.PLAYER_ATTACK)))
                damage *= 1.2F;
            if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.PINCUSHION.get(), attacker) > 0) {
                damage *= 0.9F + (0.1F * ((LivingEntity)(Object)this).getArrowCount());
            }
        }

        if (source.is(DamageTypeTags.BYPASSES_EFFECTS) || damage >= 1.1342745E38F) return damage;

        double damage_taken = getAttributes().getValue(AArcanaAttributes.DAMAGE_TAKEN.get());
        damage *= (float) damage_taken;

        if (source.is(DamageTypeTags.IS_FIRE)) {
            if (heartAttackers.containsKey(AArcanaEnchantments.IndirectHeartDamageTypes.NETHER)) {
                damage *= 2;
            }
        }
        if (source.is(DamageTypeTags.IS_LIGHTNING) && heartAttackers.containsKey(AArcanaEnchantments.IndirectHeartDamageTypes.STORM))
            damage *= 2;
        if (source.is(DamageTypeTags.IS_FREEZING) && heartAttackers.containsKey(AArcanaEnchantments.IndirectHeartDamageTypes.COLD))
            damage *= 2;
        return damage;
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"), cancellable = true)
    private void protectiveEcho(DamageSource source, float amount, CallbackInfo ci) {
        if (amount < 5) return;
        if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) || source.is(DamageTypeTags.BYPASSES_EFFECTS)) return;
        if (getEffect(AArcanaMobEffects.ECHOING_DAMAGE.get()) != null) return;
        if (EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.PROTECTIVE_ECHO.get(), (LivingEntity) (Object) this) == 0) return;
        forceAddEffect(new MobEffectInstance(AArcanaMobEffects.ECHOING_DAMAGE.get(), 5, (int)Math.floor(amount / 5)), (LivingEntity)(Object)this);
        ci.cancel();
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void onDeathEnchantments(DamageSource damageSource, CallbackInfo ci) {
        if (damageSource.getEntity() instanceof LivingEntity attackingEntity) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;

            int soulBurstLevel = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.SOUL_BURST.get(), attackingEntity);
            if (soulBurstLevel > 0) {
                float soulBurstDamage = livingEntity.getMaxHealth() * 0.2f * soulBurstLevel;
                float soulBurstRadius = 0.5f * soulBurstDamage;

                AreaEffectCloud areaEffectCloud = new AreaEffectCloud(livingEntity.level(), livingEntity.getX(), livingEntity.getRandomY(), livingEntity.getZ());
                areaEffectCloud.setOwner(attackingEntity);
                areaEffectCloud.setParticle(ParticleTypes.SCULK_SOUL);
                areaEffectCloud.setRadius(soulBurstRadius);
                areaEffectCloud.setDuration(0);
                livingEntity.level().addFreshEntity(areaEffectCloud);
                livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.EVOKER_CAST_SPELL, attackingEntity.getSoundSource(), 1.0F, 1.0F);

                List<LivingEntity> targets = livingEntity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, new AABB(livingEntity.blockPosition()).inflate(soulBurstRadius), (LivingEntity e) -> {
                    if (e == attackingEntity) return false;
                    else return !(e instanceof OwnableEntity tameableEntity) || tameableEntity.getOwner() != attackingEntity;
                });

                for (LivingEntity target : targets) {
                    target.hurt(attackingEntity.damageSources().explosion(livingEntity, attackingEntity), soulBurstDamage);
                }
            }

            int debilitatingChainLevel = EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.DEBILITATING_CHAIN.get(), attackingEntity);
            if (debilitatingChainLevel > 0) {
                float searchRadius = 2 + 2 * debilitatingChainLevel;

                List<LivingEntity> targets = livingEntity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, new AABB(livingEntity.blockPosition()).inflate(searchRadius), (LivingEntity e) -> {
                    if (e == attackingEntity) return false;
                    else if (e == livingEntity) return false;
                    else return !(e instanceof OwnableEntity tameableEntity) || tameableEntity.getOwner() != attackingEntity;
                });

                LivingEntity target = null;
                for (LivingEntity potentialTarget : targets) {
                    if (target == null) {
                        target = potentialTarget;
                        continue;
                    }
                    if (livingEntity.position().distanceTo(potentialTarget.position()) < livingEntity.position().distanceTo(target.position())) target = potentialTarget;
                }

                if (target != null) {
                    for (MobEffectInstance effect : livingEntity.getActiveEffects()) {
                        target.addEffect(effect, attackingEntity);
                    }
                    if (!livingEntity.getActiveEffects().isEmpty()) {
                        if (!livingEntity.level().isClientSide()) {
                            for (int i = 0; i < livingEntity.getEyePosition().distanceTo(target.getEyePosition()) * 4; i++) {
                                double delta = ((double)i) / (livingEntity.getEyePosition().distanceTo(target.getEyePosition()) * 4);
                                Vec3 particlePos = livingEntity.getEyePosition().lerp(target.getEyePosition(), delta);
                                ((ServerLevel)attackingEntity.level()).sendParticles(ParticleTypes.ENCHANTED_HIT, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
                            }
                            Vec3 soundPos = livingEntity.position().lerp(target.position(), 0.5D);
                            livingEntity.level().playSound(null, soundPos.x(), soundPos.y(), soundPos.z(), SoundEvents.CHORUS_FRUIT_TELEPORT, attackingEntity.getSoundSource(), 0.8F, 0.1F);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if(!livingEntity.level().isClientSide()) {
            if (heartAttackers != null) {
                for (AArcanaEnchantments.IndirectHeartDamageTypes damageType : heartAttackers.keySet()) {
                    if (heartAttackers.get(damageType) > 300) heartAttackers.remove(damageType);
                    else heartAttackers.put(damageType, heartAttackers.get(damageType) + 1);
                }
            } else {
                heartAttackers = new EnumMap<>(AArcanaEnchantments.IndirectHeartDamageTypes.class);
            }
            Iterator<Tuple<EquipmentSlot, ItemStack>> it = attributeStacks.iterator();
            while(it.hasNext()) {
                Tuple<EquipmentSlot, ItemStack> pair = it.next();
                ItemStack st = pair.getB();
                if(!ascendant_arcana$hasStackEquipInSlot(st, pair.getA())) {
                    ItemHelper.forEachEnchantment((en, stack, lvl)-> {
                        if(en instanceof TickableAttributeEnchantment) {
                            ((TickableAttributeEnchantment) en).removeAttributes(livingEntity, pair.getA());
                        }
                        else if (en instanceof TurtleHeart) {
                            ((TurtleHeart) en).removeAttributes(livingEntity, pair.getA());
                        }
                    }, st, true);
                    it.remove();
                }
            }

            for(EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = getItemBySlot(slot);
                if(!stack.isEmpty()) {
                    ItemHelper.forEachEnchantment((en, st, lvl)-> {
                        if(en instanceof TickableAttributeEnchantment) {
                            ((TickableAttributeEnchantment) en).onTick(livingEntity, st, lvl, slot);
                            if(ascendant_arcana$missingAttributeStack(st) && ((TickableAttributeEnchantment) en).addAttributes(livingEntity, st, slot, lvl)) {
                                attributeStacks.add(new Tuple<>(slot, st));
                            }
                        }
                        else if (en instanceof TurtleHeart) {
                            if(ascendant_arcana$missingAttributeStack(st) && ((TurtleHeart) en).addAttributes(livingEntity, st, slot, lvl)) {
                                attributeStacks.add(new Tuple<>(slot, st));
                            }
                        }
                    }, stack, false);
                }
            }

            if (livingEntity.level().getGameTime() % 20 == 0 && getEffect(AArcanaMobEffects.ECHOING_DAMAGE.get()) != null) {
                MobEffectInstance instance = getEffect(AArcanaMobEffects.ECHOING_DAMAGE.get());
                int damage = instance.getAmplifier();
                hurt(livingEntity.level().damageSources().magic(), damage);
            }
        }
    }

    @Unique
    private boolean ascendant_arcana$hasStackEquipInSlot(ItemStack stack, EquipmentSlot slot) {
        return getItemBySlot(slot).equals(stack);
    }

    @Unique
    public boolean ascendant_arcana$missingAttributeStack(ItemStack stack) {
        for(Tuple<EquipmentSlot, ItemStack> pair : attributeStacks) {
            if(pair.getA().equals(stack)) return false;
        }
        return true;
    }
}
