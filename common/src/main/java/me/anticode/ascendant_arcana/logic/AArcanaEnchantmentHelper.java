package me.anticode.ascendant_arcana.logic;

import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.codecs.EntityListSource;
import me.anticode.ascendant_arcana.init.AArcanaDamage;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.particle.ChainingLightningParticleOption;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.*;

public class AArcanaEnchantmentHelper {
    public static String ENCHANTMENT_CAPACITY_KEY = "AArcanaEnchantmentCapacity";

    // DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING
    public static Map<Enchantment, Integer> getAllEnchantments(ItemStack item) {
        ListTag listTag = item.getEnchantmentTags();
        if (listTag.isEmpty()) return new HashMap<>();
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            ResourceLocation resourceLocation = EnchantmentHelper.getEnchantmentId(compoundTag);
            RemovedRegistryEntry removedEntry = RemovedRegistryEntry.getFromId(resourceLocation);
            if (removedEntry != null) {
                enchantments.put(removedEntry.enchantment(), compoundTag.getInt("lvl"));
            } else {
                Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(resourceLocation);
                enchantments.put(enchantment, compoundTag.getInt("lvl"));
            }
        }
        return enchantments;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments;
        if (itemStack.getItem() instanceof EnchantedBookItem) enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));
        else enchantments = EnchantmentHelper.getEnchantments(itemStack);
        return enchantments;
    }

    public static boolean testAnvilItems(ItemStack leftStack, ItemStack rightStack) {
        Map<Enchantment, Integer> rightEnchantments = getEnchantments(rightStack);
        int cost = 0;
        Map<Enchantment, Integer> leftEnchantments = getEnchantments(leftStack);
        for (Enchantment enchantment : rightEnchantments.keySet()) {
            if (leftStack.getItem() instanceof EnchantedBookItem) {
                if (EnchantmentHelper.isEnchantmentCompatible(leftEnchantments.keySet(), enchantment)) {
                    if (leftEnchantments.containsKey(enchantment)) cost += Math.max(0, (leftEnchantments.get(enchantment) * getEnchantmentCost(enchantment)) - (rightEnchantments.get(enchantment) * getEnchantmentCost(enchantment)));
                    else cost += Math.max(0, rightEnchantments.get(enchantment) * getEnchantmentCost(enchantment));
                }
            } else {
                if (enchantment.canEnchant(leftStack) && EnchantmentHelper.isEnchantmentCompatible(leftEnchantments.keySet(), enchantment)) {
                    if (leftEnchantments.containsKey(enchantment)) cost += Math.max(0, (leftEnchantments.get(enchantment) * getEnchantmentCost(enchantment)) - (rightEnchantments.get(enchantment) * getEnchantmentCost(enchantment)));
                    else cost += Math.max(0, rightEnchantments.get(enchantment) * getEnchantmentCost(enchantment));
                }
            }
        }
        return testEnchantmentCost(leftStack, cost);
    }

    public static int getTier(Enchantment enchantment) {
        return switch (enchantment.getRarity()) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case VERY_RARE -> 4;
        };
    }

    public static int getTier(int power) {
        if (power >= AscendantArcana.config.very_rare_enchanting_power) return 4;
        else if (power >= AscendantArcana.config.rare_enchanting_power) return 3;
        else if (power >= AscendantArcana.config.uncommon_enchanting_power) return 2;
        else if (power >= AscendantArcana.config.minimum_enchanting_power) return 1;
        else return 0;
    }

    public static int getEnchantmentCost(Enchantment enchantment) {
        if (enchantment.isCurse()) return 1;
        return switch (enchantment.getRarity()) {
            case VERY_RARE -> 5;
            case RARE -> 3;
            case UNCOMMON -> 2;
            case COMMON -> 1;
        };
    }

    public static int getEnchantmentCost(Enchantment enchantment, int level) {
        return getEnchantmentCost(enchantment) * level;
    }

    public static int getEnchantmentUsage(ItemStack stack) {
        if (!stack.isEnchanted() && !(stack.getItem() instanceof EnchantedBookItem)) return 0;
        int cost = 0;
        for (Map.Entry<Enchantment, Integer> enchantInstance : getEnchantments(stack).entrySet()) {
            cost += getEnchantmentCost(enchantInstance.getKey()) * enchantInstance.getValue();
        }
        return cost;
    }

    public static boolean testEnchantmentCost(ItemStack stack, int extra) {
//        if (stack.getItem())
        return getEnchantmentUsage(stack) + extra <= getEnchantmentCapacity(stack);
    }

    public static int getBaseEnchantmentCapacity(Item item) {
        int base_capacity = 10;
        if (item instanceof ArmorItem armorItem) {
            base_capacity = armorItem.getEnchantmentValue();
        } else if (item instanceof TieredItem toolItem) {
            base_capacity = toolItem.getEnchantmentValue();
        } else if (item.getEnchantmentValue() > base_capacity) {
            base_capacity = item.getEnchantmentValue();
        }
        if (AscendantArcana.config.base_enchantment_capacity_overrides.containsKey(BuiltInRegistries.ITEM.getKey(item).toString())) {
            base_capacity = AscendantArcana.config.base_enchantment_capacity_overrides.get(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        return Mth.floor(base_capacity * AscendantArcana.config.capacity_multiplier);
    }

    public static int getEnchantmentCapacity(ItemStack stack) {
        int bonus_capacity = Mth.floor(RelicHelper.getAllRawBonusesOfType(RelicTypes.ENCHANTMENT_CAPACITY, stack.getTag()));
        if (stack.hasTag() && stack.getTag().contains(ENCHANTMENT_CAPACITY_KEY)) {
            return stack.getTag().getInt(ENCHANTMENT_CAPACITY_KEY) + bonus_capacity;
        }
        return getBaseEnchantmentCapacity(stack.getItem()) + bonus_capacity;
    }

    public static int getRequiredEnchantmentPower(Enchantment enchantment) {
        return switch (enchantment.getRarity()) {
            case COMMON -> AscendantArcana.config.minimum_enchanting_power;
            case UNCOMMON -> AscendantArcana.config.uncommon_enchanting_power;
            case RARE -> AscendantArcana.config.rare_enchanting_power;
            case VERY_RARE -> AscendantArcana.config.very_rare_enchanting_power;
        };
    }

    public static void setEnchantmentCapacity(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(ENCHANTMENT_CAPACITY_KEY, value);
    }

    public static boolean isEnchantmentEnabled(ResourceLocation identifier) {
        AscendantArcana.initializeConfigIfNull();
        if (identifier == null) {
            return true;
        }
        return !AscendantArcana.config.disabled_enchantments.contains(identifier.toString());
    }

    public static boolean isEnchantmentEnabled(Enchantment enchantment) {
        return isEnchantmentEnabled(BuiltInRegistries.ENCHANTMENT.getKey(enchantment));
    }

    public static Enchantment getReplacement(Enchantment enchantment, ItemStack stack) {
        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment entry : BuiltInRegistries.ENCHANTMENT) {
            if (stack.is(Items.ENCHANTED_BOOK) || entry.canEnchant(stack)) {
                enchantments.add(entry);
            }
        }
        if (enchantments.isEmpty()) {
            return null;
        }
        int index = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).hashCode() % enchantments.size();
        if (index < 0) {
            index += enchantments.size();
        }
        return enchantments.get(index);
    }

    public static boolean doesEntityHitHaveDeflect(EntityHitResult entityHitResult, Projectile projectile) {
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity ? (LivingEntity) projectile.getOwner() : livingEntity;
            if (livingEntity.isDamageSourceBlocked(projectile.damageSources().mobProjectile(projectile, owner))) {
                return EnchantmentHelper.getEnchantmentLevel(AArcanaEnchantments.DEFLECT.get(), livingEntity) > 0;
            }
        }
        return false;
    }

    public static void removeEnchantmentAttributes(Map<Attribute, AttributeModifier> attributeModifiers, LivingEntity entity, EquipmentSlot slot) {
        for(Map.Entry<Attribute, AttributeModifier> attributeEntry : attributeModifiers.entrySet())
        {
            UUID slotID = getUUID(slot.toString());
            AttributeInstance entityAttributeInstance = entity.getAttributes().getInstance(attributeEntry.getKey());
            if(entityAttributeInstance != null)
            {
                AttributeModifier mod = entityAttributeInstance.getModifier(slotID);
                if(mod != null)
                    entityAttributeInstance.removeModifier(mod);
                else
                    System.out.println("RIP modifier: " + entityAttributeInstance.getAttribute().getDescriptionId());
            }
        }
    }

    public static boolean addEnchantmentAttributes(Enchantment enchantment, Map<Attribute, AttributeModifier> attributeModifiers, LivingEntity entity, ItemStack stack, EquipmentSlot slot, int level) {

        if(attributeModifiers.isEmpty() || stack.isEmpty()) return false;

        for(Map.Entry<Attribute, AttributeModifier> attributeEntry : attributeModifiers.entrySet())
        {
            AttributeInstance entityAttributeInstance = entity.getAttributes().getInstance(attributeEntry.getKey());
            if(entityAttributeInstance != null)
            {
                if (entity.getAttributes().hasModifier(attributeEntry.getKey(), getUUID(slot.toString()))) continue;
                AttributeModifier mod = attributeEntry.getValue();
                entityAttributeInstance.addTransientModifier(new AttributeModifier(getUUID(slot.toString()), enchantment.getDescriptionId() + " " + level, mod.getAmount() * (double) level, mod.getOperation()));

            }
        }
        return true;
    }

    public static ItemStack convertEnchantmentsToScrap(Map<Enchantment, Integer> appliedEnchants) {
        int runningTotal = 0;
        for (Map.Entry<Enchantment, Integer> entry : appliedEnchants.entrySet()) {
            int baseCount = 3;
            if (entry.getKey() != null && !entry.getKey().isCurse()) {
                baseCount = switch (entry.getKey().getRarity()) {
                    case COMMON -> 1;
                    case UNCOMMON -> 2;
                    case RARE -> 3;
                    case VERY_RARE -> 4;
                };
                if (entry.getKey().isTreasureOnly()) baseCount += 1;
            }
            if (entry.getKey() != null && entry.getKey().isCurse()) baseCount = 1;
            runningTotal += baseCount * entry.getValue();
        }

        ItemStack itemStack;
        if (runningTotal > 9) {
            itemStack = new ItemStack(AArcanaItems.ENCHANTED_SCRAP.get());
            runningTotal /= 2;
        }
        else itemStack = new ItemStack(Items.LAPIS_LAZULI);

        itemStack.setCount(runningTotal);
        if (itemStack.getCount() > itemStack.getMaxStackSize()) itemStack.setCount(itemStack.getMaxStackSize());
        return itemStack;
    }

    public static void joltTargets(LivingEntity victim, LivingEntity attacker, int chainLength) {
        joltTargets(victim, attacker, null, chainLength);
    }

    public static void joltTargets(LivingEntity victim, LivingEntity attacker, Entity indirectEntity, int chainLength) {
        ServerLevel serverLevel = (ServerLevel) victim.level();
        Entity lastLink = victim;
        List<EntityPositionSource> chain = new LinkedList<>();
        List<Entity> chainEntity = new LinkedList<>();
        if (indirectEntity != null) {
            chain.add(new EntityPositionSource(indirectEntity, 0.25F));
            chainEntity.add(indirectEntity);
        }
        chain.add(new EntityPositionSource(victim, (float)victim.getRandomY() - (float)victim.getY()));
        chainEntity.add(victim);
        if (indirectEntity != null) victim.hurt(AArcanaDamage.source(serverLevel, AArcanaDamage.JOLTED, indirectEntity, attacker), 4);
        else victim.hurt(AArcanaDamage.source(serverLevel, AArcanaDamage.JOLTED, attacker), 4);
        for (int i = 0; i < chainLength; i++) {
            List<Entity> linkTargets = lastLink.level().getEntities(lastLink, AABB.unitCubeFromLowerCorner(lastLink.position().subtract(0.5, 0.5, 0.5)).inflate(3 + chainLength), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and((entity) -> notAllyToEntity(attacker, entity)).and((entity) -> !chainEntity.contains(entity)));
            if (linkTargets.isEmpty()) break;
            Entity nextLink = linkTargets.get(serverLevel.getRandom().nextIntBetweenInclusive(0, linkTargets.size() - 1));
            if (indirectEntity != null) nextLink.hurt(AArcanaDamage.source(serverLevel, AArcanaDamage.JOLTED, indirectEntity, attacker), 4);
            else nextLink.hurt(AArcanaDamage.source(serverLevel, AArcanaDamage.JOLTED, attacker), 4);
            chain.add(new EntityPositionSource(nextLink, (float)nextLink.getRandomY() - (float)nextLink.getY()));
            chainEntity.add(nextLink);
            lastLink = nextLink;
        }
        serverLevel.sendParticles(new ChainingLightningParticleOption(new EntityListSource(chain)), victim.getX(), victim.getY(), victim.getZ(), 0, 0, 0, 0, 0);
    }

    public static boolean notAllyToEntity(Entity entity, Entity potentialAlly) {
        if (entity == null) return true;
        if (potentialAlly == entity) return false;
        else if (entity instanceof TraceableEntity traceableEntity && traceableEntity.getOwner() == potentialAlly) return false;
        else if (potentialAlly instanceof TraceableEntity traceableEntity && traceableEntity.getOwner() == entity) return false;
        else if (entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() == potentialAlly) return false;
        else if (potentialAlly instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() == entity) return false;
        else return entity.getTeam() == null || entity.getTeam().isAllowFriendlyFire() || entity.getTeam() != potentialAlly.getTeam();
    }

    public static UUID getUUID(String slotID) {
        Random random = new Random(slotID.hashCode());

        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;
        randomBytes[6] |= 0x40;
        randomBytes[8] &= 0x3f;
        randomBytes[8] |= (byte) 0x80;
        return UUID.nameUUIDFromBytes(randomBytes);
    }
}
