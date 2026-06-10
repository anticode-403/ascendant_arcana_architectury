package me.anticode.ascendant_arcana.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;

public class DepthsCurse extends TickableAttributeEnchantment {
    public DepthsCurse() {
        super(true, Rarity.VERY_RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public void onTick(LivingEntity entity, ItemStack stack, int level, EquipmentSlot slot)
    {
        if (!slot.isArmor()) return;
        if(entity instanceof Player player)
            if(player.getAbilities().flying)
                return;

        if(entity.isInWaterOrBubble())
        {
            Vec3 vel = entity.getDeltaMovement();
            if(vel.y > -1F)
            {
                double max = 0.05 * level;
                double yV = Math.max(-max, vel.y - max);
                entity.setDeltaMovement(vel.x, yV, vel.z);
                entity.hasImpulse = true;
            }
        }
    }
}