package me.anticode.ascendant_arcana.enchantment;

import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.shapes.CollisionContext;

public class HellWalker extends Enchantment {
    public HellWalker() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getDamageProtection(int level, DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) ? 12 : 0;
    }

    public int getMinCost(int level) {
        return 20;
    }

    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    public boolean isTreasureOnly() {
        return true;
    }

    public static void freezeLava(LivingEntity entity, Level level, BlockPos blockPos) {
        if (entity.onGround()) {
            BlockState blockState = AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get().defaultBlockState();
            int i = Math.min(16, 4);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-i, -1, -i), blockPos.offset(i, -1, i))) {
                if (blockPos2.closerToCenterThan(entity.position(), i)) {
                    mutable.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
                    BlockState blockState2 = level.getBlockState(mutable);
                    if (blockState2.isAir()) {
                        BlockState blockState3 = level.getBlockState(blockPos2);
                        if (blockState3.getBlock() == Blocks.LAVA && blockState3.getValue(LavaFluid.LEVEL) == 0 && blockState.canSurvive(level, blockPos2) && level.isUnobstructed(blockState, blockPos2, CollisionContext.empty())) {
                            level.setBlockAndUpdate(blockPos2, blockState);
                            level.scheduleTick(blockPos2, AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get(), Mth.nextInt(entity.getRandom(), 60, 120));
                        }
                    }
                }
            }

        }
    }

    public boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != Enchantments.DEPTH_STRIDER && other != Enchantments.FROST_WALKER;
    }
}
