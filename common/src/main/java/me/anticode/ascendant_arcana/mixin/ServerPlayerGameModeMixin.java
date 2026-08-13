package me.anticode.ascendant_arcana.mixin;

import com.google.common.collect.Lists;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;
    @Shadow
    private int gameTicks;
    @Shadow
    private BlockPos delayedDestroyPos;

    @Shadow
    public abstract boolean destroyBlock(BlockPos arg);

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract void destroyAndAck(BlockPos arg, int i, String string);

    @Shadow
    private int destroyProgressStart;
    @Unique
    HashMap<BlockPos, Float> ascendant_arcana$excavatingBlockProgresses = new HashMap<>();

    @Unique
    private Direction ascendant_arcana$excavatingDirection = null;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private void destroyBlock(CallbackInfo ci) {
        List<BlockPos> excavatingTargets = ascendant_arcana$getExtractingBlockPositions(delayedDestroyPos, ascendant_arcana$excavatingDirection);
        if (excavatingTargets == null) return;
        for (BlockPos blockPos : excavatingTargets) {
            if (ascendant_arcana$excavatingBlockProgresses.containsKey(blockPos) && ascendant_arcana$excavatingBlockProgresses.get(blockPos) >= 1) {
                destroyBlock(blockPos);
            }
        }
    }

    @Inject(method = "incrementDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private void incrementExcavatingDestroyProgress(BlockState blockState, BlockPos blockPos, int i, CallbackInfoReturnable<Float> cir) {
        List<BlockPos> excavatingTargets = ascendant_arcana$getExtractingBlockPositions(blockPos, ascendant_arcana$excavatingDirection);
        if (excavatingTargets == null) return;
        for (BlockPos excavatingTarget : excavatingTargets) {
            float f = blockState.getDestroyProgress(this.player, this.player.level(), excavatingTarget) * (float)(gameTicks - i + 1);
            ascendant_arcana$excavatingBlockProgresses.put(excavatingTarget, f);
            int k = (int)(f * 10.0F);
            level.destroyBlockProgress(this.player.getId(), excavatingTarget, k);
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void trackDirection(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        ascendant_arcana$excavatingDirection = direction;
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyAndAck(Lnet/minecraft/core/BlockPos;ILjava/lang/String;)V"))
    private void excavatingTryDestroyMultiple(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        List<BlockPos> excavatingTargets = ascendant_arcana$getExtractingBlockPositions(blockPos, ascendant_arcana$excavatingDirection);
        if (excavatingTargets == null) return;
        for (BlockPos excavatingTarget : excavatingTargets) {
            BlockState blockState = level.getBlockState(excavatingTarget);
            if (isCreative()) {
                destroyAndAck(excavatingTarget, j, "excavating creative destroy");
                continue;
            }
            if (blockState.isAir()) continue;
            blockState.attack(this.level, excavatingTarget, this.player);
            float f = blockState.getDestroyProgress(this.player, this.player.level(), excavatingTarget);
            float g = f * (float)(gameTicks - destroyProgressStart + 1);
            ascendant_arcana$excavatingBlockProgresses.put(excavatingTarget, f);
            if (f >= 1F) {
                destroyAndAck(excavatingTarget, j, "excavating insta mine");
            } else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && g >= 0.7F) {
                destroyAndAck(excavatingTarget, j, "excavating destroyed");
            }
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void excavatingDestroyBlockProgress(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        List<BlockPos> excavatingTargets = ascendant_arcana$getExtractingBlockPositions(blockPos, ascendant_arcana$excavatingDirection);
        if (excavatingTargets == null) return;
        for (BlockPos excavatingTarget : excavatingTargets) {
            BlockState blockState = level.getBlockState(excavatingTarget);
            if (blockState.isAir()) continue;
            float f = blockState.getDestroyProgress(this.player, this.player.level(), excavatingTarget);
            if (action != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && action != ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                level.destroyBlockProgress(this.player.getId(), excavatingTarget, (int) (f * 10F));
            } else {
                level.destroyBlockProgress(this.player.getId(), excavatingTarget, -1);
            }
        }
    }


    @Unique
    private List<BlockPos> ascendant_arcana$getExtractingBlockPositions(BlockPos originalPos, Direction direction) {
        if (direction == null) return null;
        if (player.isCrouching()) return null;
        if (EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.EXCAVATING.get(), player.getMainHandItem()) <= 0) return null;

        // There is a cleaner way to do this algorithmically but this is faster.
        return switch (direction.getAxis()) {
            case X -> Lists.newArrayList(
                    originalPos.relative(Direction.UP),
                    originalPos.relative(Direction.DOWN),
                    originalPos.relative(Direction.NORTH),
                    originalPos.relative(Direction.SOUTH),
                    originalPos.relative(Direction.UP).relative(Direction.NORTH),
                    originalPos.relative(Direction.UP).relative(Direction.SOUTH),
                    originalPos.relative(Direction.DOWN).relative(Direction.NORTH),
                    originalPos.relative(Direction.DOWN).relative(Direction.SOUTH)
            );
            case Y -> Lists.newArrayList(
                    originalPos.relative(Direction.EAST),
                    originalPos.relative(Direction.WEST),
                    originalPos.relative(Direction.NORTH),
                    originalPos.relative(Direction.SOUTH),
                    originalPos.relative(Direction.EAST).relative(Direction.NORTH),
                    originalPos.relative(Direction.EAST).relative(Direction.SOUTH),
                    originalPos.relative(Direction.WEST).relative(Direction.NORTH),
                    originalPos.relative(Direction.WEST).relative(Direction.SOUTH)
            );
            case Z -> Lists.newArrayList(
                    originalPos.relative(Direction.EAST),
                    originalPos.relative(Direction.WEST),
                    originalPos.relative(Direction.UP),
                    originalPos.relative(Direction.DOWN),
                    originalPos.relative(Direction.EAST).relative(Direction.UP),
                    originalPos.relative(Direction.EAST).relative(Direction.DOWN),
                    originalPos.relative(Direction.WEST).relative(Direction.UP),
                    originalPos.relative(Direction.WEST).relative(Direction.DOWN));
        };
    }
}
