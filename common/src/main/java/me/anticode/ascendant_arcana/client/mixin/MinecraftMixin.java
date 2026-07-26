package me.anticode.ascendant_arcana.client.mixin;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.init.AArcanaEnchantments;
import me.anticode.ascendant_arcana.networking.ServerboundShieldBashPacket;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Final
    public Options options;

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0, shift = At.Shift.AFTER))
    private void injectShieldBashAction(CallbackInfo ci) {
        assert player != null;
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() > Items.SHIELD.getUseDuration(Items.SHIELD.getDefaultInstance()) - ShieldItem.EFFECTIVE_BLOCK_DELAY) return;
        int shieldBashLevel = EnchantmentHelper.getItemEnchantmentLevel(AArcanaEnchantments.BASHING.get(), player.getUseItem());
        if (shieldBashLevel > 0) {
            while (options.keyAttack.consumeClick()) {
                NetworkManager.sendToServer(ServerboundShieldBashPacket.Id, new ServerboundShieldBashPacket(true).write());
            }
        }
    }
}
