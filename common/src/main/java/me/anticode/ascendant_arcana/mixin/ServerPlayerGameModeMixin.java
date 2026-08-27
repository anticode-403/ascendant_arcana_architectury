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

import java.util.ArrayList;
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
    @Shadow
    private boolean isDestroyingBlock;
    @Shadow
    private boolean hasDelayedDestroy;
    @Unique
    HashMap<BlockPos, Float> ascendant_arcana$excavatingBlockProgresses = new HashMap<>();

    @Unique
    private Direction ascendant_arcana$excavatingDirection = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void cleanupBlockProgresses(CallbackInfo ci) {
        if (!isDestroyingBlock && !hasDelayedDestroy) {
            ascendant_arcana$excavatingBlockProgresses.clear();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private void destroyBlock(CallbackInfo ci) {
        if (ascendant_arcana$excavatingBlockProgresses.isEmpty()) return;
        for (BlockPos blockPos : ascendant_arcana$excavatingBlockProgresses.keySet()) {
            if (ascendant_arcana$excavatingBlockProgresses.get(blockPos) >= 1) {
                destroyBlock(blockPos);
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void destroyBlockEarly(CallbackInfo ci) {
        if (ascendant_arcana$excavatingBlockProgresses.isEmpty()) return;
        for (BlockPos blockPos : ascendant_arcana$excavatingBlockProgresses.keySet()) {
            if (ascendant_arcana$excavatingBlockProgresses.get(blockPos) >= 1) {
                destroyBlock(blockPos);
            }
        }
    }

    @Inject(method = "incrementDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private void incrementExcavatingDestroyProgress(BlockState blockState, BlockPos blockPos, int i, CallbackInfoReturnable<Float> cir) {
        List<BlockPos> excavatingTargets = ascendant_arcana$getExtractingBlockPositions(blockPos, ascendant_arcana$excavatingDirection);
        if (excavatingTargets == null) return;
        for (BlockPos excavatingTarget : excavatingTargets) {
            BlockState targetBlockState = level.getBlockState(excavatingTarget);
            float f = targetBlockState.getDestroyProgress(this.player, this.player.level(), excavatingTarget) * (float)(gameTicks - i);
            ascendant_arcana$excavatingBlockProgresses.put(excavatingTarget, f);
            int k = (int)(f * 10.0F);
            level.destroyBlockProgress(ascendant_arcana$getDestroyProgressId(excavatingTargets, excavatingTarget, player.getId()), excavatingTarget, k);
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
            float g = f * (float)(gameTicks - destroyProgressStart);
            ascendant_arcana$excavatingBlockProgresses.put(excavatingTarget, f);
            if (f >= 1F) {
                destroyAndAck(excavatingTarget, j, "excavating insta mine");
                ascendant_arcana$excavatingBlockProgresses.remove(excavatingTarget);
            } else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && g >= 0.7F) {
                destroyAndAck(excavatingTarget, j, "excavating destroyed");
                ascendant_arcana$excavatingBlockProgresses.remove(excavatingTarget);
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
            blockState.attack(this.level, excavatingTarget, this.player);
            float f = blockState.getDestroyProgress(this.player, this.player.level(), excavatingTarget);
            if (action != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && action != ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                ascendant_arcana$excavatingBlockProgresses.put(excavatingTarget, f);
                if (f >= 1F) {
                    destroyAndAck(excavatingTarget, j, "excavating insta mine");
                    ascendant_arcana$excavatingBlockProgresses.remove(excavatingTarget);
                }
                level.destroyBlockProgress(ascendant_arcana$getDestroyProgressId(excavatingTargets, excavatingTarget, player.getId()), excavatingTarget, (int) (f * 10F));
            } else {
                level.destroyBlockProgress(ascendant_arcana$getDestroyProgressId(excavatingTargets, excavatingTarget, player.getId()), excavatingTarget, -1);
            }
        }
    }

    @Unique
    private int ascendant_arcana$getDestroyProgressId(List<BlockPos> blockPositions, BlockPos blockPos, int playerId) {
        int id = blockPositions.indexOf(blockPos);
        // Realistically there exists a universe in which this ID is already reserved by another player which could technically cause
        // minor visual issues but seeing as this is purely visual and not a gameplay effect
        try {
            return Integer.parseInt("69" + playerId + playerId + id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Unique
    private List<BlockPos> ascendant_arcana$getExtractingBlockPositions(BlockPos originalPos, Direction direction) {
        if (direction == null) return null;
        if (player.isCrouching()) return null;
        int excavatingLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.EXCAVATING.get(), player.getMainHandItem());
        if (excavatingLevel <= 0) return null;

        // There is a cleaner way to do this algorithmically but this is faster.
        ArrayList<BlockPos> blockPositions = new ArrayList<>();
        if (excavatingLevel == 1) {
            if (direction.getAxis() != Direction.Axis.Y) {
                blockPositions.add(originalPos.relative(Direction.DOWN));
            }
        }
        else switch (direction.getAxis()) {
            case X:
                blockPositions.add(originalPos.relative(Direction.UP).relative(Direction.NORTH));
                blockPositions.add(originalPos.relative(Direction.UP).relative(Direction.SOUTH));
                blockPositions.add(originalPos.relative(Direction.DOWN).relative(Direction.NORTH));
                blockPositions.add(originalPos.relative(Direction.DOWN).relative(Direction.SOUTH));
                break;
            case Y:
                blockPositions.add(originalPos.relative(Direction.EAST).relative(Direction.NORTH));
                blockPositions.add(originalPos.relative(Direction.EAST).relative(Direction.SOUTH));
                blockPositions.add(originalPos.relative(Direction.WEST).relative(Direction.NORTH));
                blockPositions.add(originalPos.relative(Direction.WEST).relative(Direction.SOUTH));
                break;
            case Z:
                blockPositions.add(originalPos.relative(Direction.EAST).relative(Direction.UP));
                blockPositions.add(originalPos.relative(Direction.EAST).relative(Direction.DOWN));
                blockPositions.add(originalPos.relative(Direction.WEST).relative(Direction.UP));
                blockPositions.add(originalPos.relative(Direction.WEST).relative(Direction.DOWN));
                break;
        }
        return blockPositions;
    }
}
